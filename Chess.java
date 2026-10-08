import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Random;

public class Chess implements BoardStateProvider{
    private static final int PLAYER1_START_STATE = 1;
    private static final int PLAYER1_GOAL_STATE = 16;
    private static final int PLAYER2_START_STATE = 4;
    private static final int PLAYER2_GOAL_STATE = 13;
    private static final int MAX_INPUT_LENGTH = GameConfig.MAX_INPUT_LENGTH;
    private final Random random = new Random();
    private final NfaSimulator simulator = new NfaSimulator(GameConfig.BOARD_SIZE);
    private final PathFileWriter pathWriter = new PathFileWriter();

    private Player player1;
    private Player player2;
    private ChessPanel chessPanel;
    private InfoPanel infoPanel;
    private LogPanel logPanel;
    private ControlPanel controlPanel;
    private JFrame frame;
    private Timer simulationTimer;
    private Timer winnerTimer;
    private String modeText = "Manual";

    private boolean isPlayer1Turn;
    private boolean gameRunning;
    private boolean automaticMode;
    private boolean collisionDetected;
    private boolean showingWinnerPosition;
    private boolean winnerIsPlayer1;
    private int moveIndex;

    public Chess(){
        initializeGame();
        setupUI();
    }

    private void initializeGame(){
        player1 = new Player("Jugador 1", PLAYER1_START_STATE, PLAYER1_GOAL_STATE, Color.BLUE);
        player2 = new Player("Jugador 2", PLAYER2_START_STATE, PLAYER2_GOAL_STATE, Color.GREEN);
        resetGameState();
    }

    private void resetGameState(){
        moveIndex = 0;
        gameRunning = false;
        collisionDetected = false;
        showingWinnerPosition = false;
        winnerIsPlayer1 = false;
        isPlayer1Turn = random.nextBoolean();
        player1.resetPosition();
        player2.resetPosition();
        updateInfoPanel("En juego");
    }

    private void setupUI(){
        frame = new JFrame("Tablero");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GameConfig.WINDOW_SIZE + 420, GameConfig.WINDOW_SIZE + 200);
        frame.setLayout(new BorderLayout());

        chessPanel = new ChessPanel(player1, player2, this);
        frame.add(chessPanel, BorderLayout.CENTER);
        logPanel = new LogPanel();
        frame.add(logPanel, BorderLayout.EAST);
        infoPanel = new InfoPanel();
        frame.add(infoPanel, BorderLayout.SOUTH);
        controlPanel = new ControlPanel();
        controlPanel.setListener(new ControlPanel.ControlListener(){
            @Override
            public void onStart(boolean automaticModeSelected, String input1, String input2, int autoLength){
                startFromPanel(automaticModeSelected, input1, input2, autoLength);
            }

            @Override
            public void onStop(){
                stopSimulation();
            }
        });
        frame.add(controlPanel, BorderLayout.NORTH);
        controlPanel.setControlsEnabled(true);
        updateInfoPanel("En juego");

        frame.setVisible(true);
    }

    private void startFromPanel(boolean automaticModeSelected, String input1, String input2, int autoLength){
        if(gameRunning){
            log("Ya hay una simulacion en curso. Detenla antes de iniciar otra.");
            return;
        }

        automaticMode = automaticModeSelected;
        if(automaticModeSelected){
            modeText = "Automatico";
        }else{
            modeText = "Manual";
        }

        isPlayer1Turn = random.nextBoolean();
        if(isPlayer1Turn){
            log("Jugador 1 comienza");
        }else{
            log("Jugador 2 comienza");
        }

        if(automaticModeSelected){
            int length = Math.max(3, Math.min(autoLength, MAX_INPUT_LENGTH));
            player1.setInput(generateRandomString(length));
            player2.setInput(generateRandomString(length));
            log("Cadena Jugador 1: " + player1.getInput());
            log("Cadena Jugador 2: " + player2.getInput());
        }else{
            player1.setInput(input1.toLowerCase());
            player2.setInput(input2.toLowerCase());
            if(!inputsAreValid()){
                log("Entradas invalidas. Ajuste las cadenas e intente de nuevo.");
                return;
            }
        }

        controlPanel.setControlsEnabled(false);
        startSimulation();
    }

    private void startSimulation(){
        moveIndex = 0;

        player1.resetPosition();
        player2.resetPosition();
        if(chessPanel != null){
            chessPanel.repaint();
        }

        SimulationResult result1 = simulator.simulate(player1.getInput(), player1.getInitialState(), player1.getFinalState());
        SimulationResult result2 = simulator.simulate(player2.getInput(), player2.getInitialState(), player2.getFinalState());

        player1.applySimulationResult(result1);
        player2.applySimulationResult(result2);

        logSimulation("Jugador 1", result1);
        logSimulation("Jugador 2", result2);

        pathWriter.writePaths(result1.getAllPaths(), "caminost1.txt");
        pathWriter.writePaths(result1.getWinningPaths(), "caminosg1.txt");
        pathWriter.writePaths(result2.getAllPaths(), "caminost2.txt");
        pathWriter.writePaths(result2.getWinningPaths(), "caminosg2.txt");

        boolean player1HasWinningPath = player1.chooseWinningPath(random);
        boolean player2HasWinningPath = player2.chooseWinningPath(random);

        if(!player1HasWinningPath){
            player1.chooseAnyPath(random);
        }
        if(!player2HasWinningPath){
            player2.chooseAnyPath(random);
        }

        if(player1.getCurrentPath() == null || player2.getCurrentPath() == null){
            reportDefaultWinner(player1HasWinningPath, player2HasWinningPath);
            return;
        }

        gameRunning = true;
        chessPanel.repaint();
        updateInfoPanel("En juego");

        startSimulationTimer();
    }

    private void logSimulation(String playerLabel, SimulationResult result){
        log(playerLabel + " - Total caminos: " + result.getAllPaths().size() +
                ", Caminos ganadores: " + result.getWinningPaths().size());
    }

    private void updateInfoPanel(String winnerText){
        if(infoPanel == null){
            return;
        }
        String turnText;
        if(!gameRunning){
            turnText = "Listo";
        }else if(isPlayer1Turn){
            turnText = "Jugador 1";
        }else{
            turnText = "Jugador 2";
        }

        String winner;
        if(winnerText != null){
            winner = winnerText;
        }else{
            winner = "En juego";
        }
        infoPanel.updateStatus(turnText, moveIndex, modeText, winner);
    }

    private void log(String message){
        System.out.println(message);
        if(logPanel != null){
            logPanel.append(message);
        }
    }

    private void stopSimulation(){
        gameRunning = false;
        stopSimulationTimer();
        log("Simulacion detenida por el usuario");
        controlPanel.setControlsEnabled(true);
        resetGameState();
        if(chessPanel != null){
            chessPanel.repaint();
        }
        updateInfoPanel("Sin ganador");
    }

    private void startSimulationTimer(){
        stopSimulationTimer();
        simulationTimer = new Timer(2000, this::tickSimulation);
        simulationTimer.start();
    }

    private void stopSimulationTimer(){
        if(simulationTimer != null && simulationTimer.isRunning()){
            simulationTimer.stop();
        }
    }

    private void tickSimulation(ActionEvent event){
        if(!gameRunning){
            stopSimulationTimer();
            return;
        }
        nextMove();
    }

    private void nextMove(){
        if(!gameRunning){
            return;
        }

        if(player1.getCurrentPath() == null || player2.getCurrentPath() == null){
            gameRunning = false;
            finishGame(false, false);
            return;
        }

        int prevJ1 = player1.getCurrentPosition();
        int prevJ2 = player2.getCurrentPosition();
        moveIndex++;

        if(moveIndex >= player1.getCurrentPath().size() || moveIndex >= player2.getCurrentPath().size()){
            gameRunning = false;
            log("Juego terminado - No hay ganador");
            finishGame(false, false);
            return;
        }

        int nextP1 = player1.getCurrentPath().get(moveIndex);
        int nextP2 = player2.getCurrentPath().get(moveIndex);

        if(nextP1 == nextP2){
            collisionDetected = true;
            log("Colision detectada en casilla " + nextP1 + "!");

            if(isPlayer1Turn){
                if(redirectPlayer(player2, moveIndex)){
                    nextP2 = player2.getCurrentPath().get(moveIndex);
                    log("Jugador 2 redirigido a nueva ruta");
                }else{
                    log("Jugador 2 no puede moverse, cede el paso");
                    nextP2 = player2.getCurrentPosition();
                }
            }else{
                if(redirectPlayer(player1, moveIndex)){
                    nextP1 = player1.getCurrentPath().get(moveIndex);
                    log("Jugador 1 redirigido a nueva ruta");
                }else{
                    log("Jugador 1 no puede moverse, cede el paso");
                    nextP1 = player1.getCurrentPosition();
                }
            }
        }else{
            collisionDetected = false;
        }

        player1.setCurrentPosition(nextP1);
        player2.setCurrentPosition(nextP2);

        log("Movimiento " + moveIndex + ": J1 en " + player1.getCurrentPosition() +
                ", J2 en " + player2.getCurrentPosition());

        int collisionCell;
        if(collisionDetected){
            collisionCell = nextP1;
        }else{
            collisionCell = -1;
        }
        chessPanel.animateMove(prevJ1, player1.getCurrentPosition(), prevJ2, player2.getCurrentPosition(),
                collisionCell);

        if(player1.getCurrentPosition() == player1.getFinalState()){
            gameRunning = false;
            log("Jugador 1 ha ganado!");
            finishGame(true, true);
        }else if(player2.getCurrentPosition() == player2.getFinalState()){
            gameRunning = false;
            log("Jugador 2 ha ganado!");
            finishGame(true, false);
        }

        if(!collisionDetected){
            isPlayer1Turn = !isPlayer1Turn;
        }

        chessPanel.repaint();
        updateInfoPanel("En juego");
    }

    private boolean redirectPlayer(Player player, int collisionIndex){
        if(player.getCurrentPath() == null){
            return false;
        }

        for(List<Integer> path : player.getWinningPaths()){
            boolean valid = true;
            for(int i=0; i<collisionIndex; i++){
                if(!path.get(i).equals(player.getCurrentPath().get(i))){
                    valid = false;
                    break;
                }
            }

            if(valid && path.size() > collisionIndex &&
                    !path.get(collisionIndex).equals(player.getCurrentPath().get(collisionIndex))){
                player.setCurrentPath(path);
                return true;
            }
        }

        return false;
    }

    private void finishGame(boolean hasWinner, boolean winnerIsPlayer1Flag){
        stopSimulationTimer();
        controlPanel.setControlsEnabled(true);
        if(hasWinner){
            winnerIsPlayer1 = winnerIsPlayer1Flag;
            String winnerName;
            if(winnerIsPlayer1Flag){
                winnerName = "Jugador 1";
            }else{
                winnerName = "Jugador 2";
            }
            log(winnerName + " ha llegado a su posicion final!");

            showingWinnerPosition = true;
            updateInfoPanel(winnerName);

            if(winnerTimer != null && winnerTimer.isRunning()){
                winnerTimer.stop();
            }
            winnerTimer = new Timer(3000, e -> {
                showingWinnerPosition = false;
                resetGame();
            });
            winnerTimer.setRepeats(false);
            winnerTimer.start();
        }else{
            showingWinnerPosition = false;
            updateInfoPanel("Sin ganador");
            resetGame();
        }
    }

    private void resetGame(){
        stopSimulationTimer();
        gameRunning = false;
        resetGameState();
        log("Jugadores regresados a posiciones iniciales");
        SwingUtilities.invokeLater(() -> chessPanel.repaint());
        controlPanel.setControlsEnabled(true);
    }

    private boolean inputsAreValid(){
        if(player1.getInput().isEmpty() || player2.getInput().isEmpty()){
            log("Las cadenas no pueden estar vacias");
            return false;
        }
        if(player1.getInput().length() > MAX_INPUT_LENGTH || player2.getInput().length() > MAX_INPUT_LENGTH){
            log("Longitud maxima permitida: " + MAX_INPUT_LENGTH + " para evitar explosion de caminos");
            return false;
        }
        if(!player1.getInput().matches("[rb]+") || !player2.getInput().matches("[rb]+")){
            log("Solo se permiten caracteres 'r' y 'b'");
            return false;
        }
        if(player1.getInput().length() != player2.getInput().length()){
            log("Las cadenas deben tener la misma longitud");
            return false;
        }
        return true;
    }

    private void reportDefaultWinner(boolean player1HasWinningPath, boolean player2HasWinningPath){
        if(!player1HasWinningPath && !player2HasWinningPath){
            log("No hay caminos ganadores para ninguno de los jugadores. Juego terminado.");
        }else if(!player1HasWinningPath){
            log("Jugador 2 gana por defecto (Jugador 1 no tiene caminos ganadores)");
        }else{
            log("Jugador 1 gana por defecto (Jugador 2 no tiene caminos ganadores)");
        }
        gameRunning = false;
        finishGame(false, false);
    }

    private String generateRandomString(int length){
        int safeLength = Math.min(length, MAX_INPUT_LENGTH);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<safeLength; i++){
            if(random.nextBoolean()){
                sb.append('r');
            }else{
                sb.append('b');
            }
        }
        return sb.toString();
    }

    @Override
    public int getPlayer1Position(){
        return player1.getCurrentPosition();
    }

    @Override
    public int getPlayer2Position(){
        return player2.getCurrentPosition();
    }

    @Override
    public boolean isShowingWinner(){
        return showingWinnerPosition;
    }

    @Override
    public boolean isWinnerPlayer1(){
        return winnerIsPlayer1;
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(Chess::new);
    }
}
