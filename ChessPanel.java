import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

public class ChessPanel extends JPanel{
    private static final int ANIMATION_DURATION_MS = 250;
    private static final int ANIMATION_STEP_MS = 16;
    private static final Color TEXT_COLOR = GameConfig.COLOR_TEXT;
    private static final Font CELL_FONT = new Font("Consolas", Font.BOLD, 14);
    private static final Font WINNER_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font COLLISION_FONT = new Font("Arial", Font.BOLD, 14);
    private final Player player1;
    private final Player player2;
    private final BoardStateProvider stateProvider;
    private final int goalStateP1;
    private final int goalStateP2;

    private boolean animating;
    private long animationStart;
    private double p1StartRow;
    private double p1StartCol;
    private double p1EndRow;
    private double p1EndCol;
    private double p2StartRow;
    private double p2StartCol;
    private double p2EndRow;
    private double p2EndCol;
    private int collisionHighlightCell = -1;
    private long collisionHighlightUntil = 0L;
    private Timer animationTimer;

    public ChessPanel(Player player1, Player player2, BoardStateProvider stateProvider){
        this.player1 = player1;
        this.player2 = player2;
        this.stateProvider = stateProvider;
        this.goalStateP1 = player1.getFinalState();
        this.goalStateP2 = player2.getFinalState();
        setPreferredSize(new Dimension(GameConfig.WINDOW_SIZE, GameConfig.WINDOW_SIZE));
        setBackground(GameConfig.COLOR_BACKGROUND);
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int tileSize = computeTileSize();
        int boardPixels = tileSize * GameConfig.BOARD_SIZE;
        int offsetX = (getWidth() - boardPixels) / 2;
        int offsetY = (getHeight() - boardPixels) / 2;

        drawBoard(g2d, tileSize, offsetX, offsetY);
        drawGoals(g2d, tileSize, offsetX, offsetY);
        drawPlayers(g2d, tileSize, offsetX, offsetY);
        drawWinnerHighlight(g2d, tileSize, offsetX, offsetY);
        drawCollisionHighlight(g2d, tileSize, offsetX, offsetY);
        g2d.dispose();
    }

    private int computeTileSize(){
        int usable = Math.min(getWidth(), getHeight());
        int size = usable / GameConfig.BOARD_SIZE;
        if(size > 0){
            return size;
        }
        return GameConfig.TILE_SIZE;
    }

    private void drawBoard(Graphics2D g, int tileSize, int offsetX, int offsetY){
        for(int row=0; row<GameConfig.BOARD_SIZE; row++){
            for(int col=0; col<GameConfig.BOARD_SIZE; col++){
                Color squareColor;
                if((row + col) % 2 == 0){
                    squareColor = GameConfig.COLOR_DARK_SQUARE;
                }else{
                    squareColor = GameConfig.COLOR_LIGHT_SQUARE;
                }
                int x = offsetX + col * tileSize;
                int y = offsetY + row * tileSize;
                g.setColor(squareColor);
                g.fillRect(x, y, tileSize, tileSize);

                g.setColor(TEXT_COLOR);
                g.setFont(CELL_FONT);
                int cellNumber = row * GameConfig.BOARD_SIZE + col + 1;
                g.drawString(String.valueOf(cellNumber), x + 8, y + 18);
            }
        }
    }

    private void drawGoals(Graphics2D g, int tileSize, int offsetX, int offsetY){
        Stroke original = g.getStroke();
        int stroke = Math.max(3, tileSize / 25);
        g.setStroke(new BasicStroke(stroke));

        int p1Row = (goalStateP1 - 1) / GameConfig.BOARD_SIZE;
        int p1Col = (goalStateP1 - 1) % GameConfig.BOARD_SIZE;
        g.setColor(GameConfig.COLOR_GOAL_P1);
        g.drawRoundRect(offsetX + p1Col * tileSize + stroke, offsetY + p1Row * tileSize + stroke,
                tileSize - stroke * 2, tileSize - stroke * 2, tileSize / 6, tileSize / 6);

        int p2Row = (goalStateP2 - 1) / GameConfig.BOARD_SIZE;
        int p2Col = (goalStateP2 - 1) % GameConfig.BOARD_SIZE;
        g.setColor(GameConfig.COLOR_GOAL_P2);
        g.drawRoundRect(offsetX + p2Col * tileSize + stroke, offsetY + p2Row * tileSize + stroke,
                tileSize - stroke * 2, tileSize - stroke * 2, tileSize / 6, tileSize / 6);

        g.setStroke(original);
    }

    private void drawPlayers(Graphics2D g, int tileSize, int offsetX, int offsetY){
        double p1Row = getInterpolatedRow(player1);
        double p1Col = getInterpolatedCol(player1);
        double p2Row = getInterpolatedRow(player2);
        double p2Col = getInterpolatedCol(player2);

        int tokenDiameter = (int) Math.round(tileSize * 0.58);
        int tokenRadius = tokenDiameter / 2;

        int p1CenterX = cellCenterX(p1Col, tileSize, offsetX);
        int p1CenterY = cellCenterY(p1Row, tileSize, offsetY);
        g.setColor(player1.getTokenColor());
        g.fillOval(p1CenterX - tokenRadius, p1CenterY - tokenRadius, tokenDiameter, tokenDiameter);

        int p2CenterX = cellCenterX(p2Col, tileSize, offsetX);
        int p2CenterY = cellCenterY(p2Row, tileSize, offsetY);
        g.setColor(player2.getTokenColor());
        g.fillOval(p2CenterX - tokenRadius, p2CenterY - tokenRadius, tokenDiameter, tokenDiameter);
    }

    private int cellCenterX(double col, int tileSize, int offsetX){
        return (int) Math.round(offsetX + (col + 0.5) * tileSize);
    }

    private int cellCenterY(double row, int tileSize, int offsetY){
        return (int) Math.round(offsetY + (row + 0.5) * tileSize);
    }

    private void drawWinnerHighlight(Graphics2D g, int tileSize, int offsetX, int offsetY){
        if(!stateProvider.isShowingWinner()){
            return;
        }

        boolean winnerIsPlayer1 = stateProvider.isWinnerPlayer1();
        int winnerPosition;
        Color winnerColor;
        if(winnerIsPlayer1){
            winnerPosition = stateProvider.getPlayer1Position();
            winnerColor = player1.getTokenColor();
        }else{
            winnerPosition = stateProvider.getPlayer2Position();
            winnerColor = player2.getTokenColor();
        }
        int winnerRow = (winnerPosition - 1) / GameConfig.BOARD_SIZE;
        int winnerCol = (winnerPosition - 1) % GameConfig.BOARD_SIZE;

        g.setColor(winnerColor);
        Stroke originalStroke = g.getStroke();
        int stroke = Math.max(5, tileSize / 18);
        g.setStroke(new BasicStroke(stroke));

        int centerX = offsetX + winnerCol * tileSize + tileSize / 2;
        int centerY = offsetY + winnerRow * tileSize + tileSize / 2;
        int diameter = (int) Math.round(tileSize * 0.68);

        g.drawOval(centerX - diameter / 2, centerY - diameter / 2, diameter, diameter);
        g.setStroke(originalStroke);

        g.setFont(WINNER_FONT);
        g.drawString("GANADOR", centerX - tileSize / 3, centerY - tileSize / 2);
    }

    private void drawCollisionHighlight(Graphics2D g, int tileSize, int offsetX, int offsetY){
        if(collisionHighlightCell <= 0 || System.currentTimeMillis() > collisionHighlightUntil){
            return;
        }
        int row = (collisionHighlightCell - 1) / GameConfig.BOARD_SIZE;
        int col = (collisionHighlightCell - 1) % GameConfig.BOARD_SIZE;
        int x = offsetX + col * tileSize;
        int y = offsetY + row * tileSize;

        Stroke original = g.getStroke();
        int stroke = Math.max(4, tileSize / 20);
        int arc = Math.max(12, tileSize / 6);

        Color base = GameConfig.COLOR_COLLISION;
        Color fill = new Color(base.getRed(), base.getGreen(), base.getBlue(), 90);

        g.setColor(fill);
        g.fillRoundRect(x + 3, y + 3, tileSize - 6, tileSize - 6, arc, arc);

        g.setColor(base);
        g.setStroke(new BasicStroke(stroke));
        g.drawRoundRect(x + 3, y + 3, tileSize - 6, tileSize - 6, arc, arc);

        g.setFont(COLLISION_FONT.deriveFont((float) Math.max(12, tileSize / 8)));
        String text = "Choque";
        int textWidth = g.getFontMetrics().stringWidth(text);
        int textX = x + (tileSize - textWidth) / 2;
        int textY = y + tileSize / 2 + g.getFontMetrics().getAscent() / 3;
        g.drawString(text, textX, textY);

        g.setStroke(original);
    }

    public void animateMove(int p1From, int p1To, int p2From, int p2To, int collisionCell){
        this.p1StartRow = (p1From - 1) / GameConfig.BOARD_SIZE;
        this.p1StartCol = (p1From - 1) % GameConfig.BOARD_SIZE;
        this.p1EndRow = (p1To - 1) / GameConfig.BOARD_SIZE;
        this.p1EndCol = (p1To - 1) % GameConfig.BOARD_SIZE;

        this.p2StartRow = (p2From - 1) / GameConfig.BOARD_SIZE;
        this.p2StartCol = (p2From - 1) % GameConfig.BOARD_SIZE;
        this.p2EndRow = (p2To - 1) / GameConfig.BOARD_SIZE;
        this.p2EndCol = (p2To - 1) % GameConfig.BOARD_SIZE;
        this.animationStart = System.currentTimeMillis();
        this.animating = true;
        if(collisionCell > 0){
            this.collisionHighlightCell = collisionCell;
            this.collisionHighlightUntil = System.currentTimeMillis() + 1000;
        }
        if(animationTimer != null && animationTimer.isRunning()){
            animationTimer.stop();
        }
        animationTimer = new Timer(ANIMATION_STEP_MS, e -> {
            long elapsed = System.currentTimeMillis() - animationStart;
            if(elapsed >= ANIMATION_DURATION_MS){
                animating = false;
                animationTimer.stop();
                repaint();
                return;
            }
            repaint();
        });
        animationTimer.start();
    }

    private double getInterpolatedRow(Player player){
        if(!animating){
            return (player.getCurrentPosition() - 1) / GameConfig.BOARD_SIZE;
        }
        double progress = Math.min(1.0, (System.currentTimeMillis() - animationStart) / (double) ANIMATION_DURATION_MS);

        double start;
        double end;
        if(player == player1){
            start = p1StartRow;
            end = p1EndRow;
        }else{
            start = p2StartRow;
            end = p2EndRow;
        }
        return start + (end - start) * progress;
    }

    private double getInterpolatedCol(Player player){
        if(!animating){
            return (player.getCurrentPosition() - 1) % GameConfig.BOARD_SIZE;
        }
        double progress = Math.min(1.0, (System.currentTimeMillis() - animationStart) / (double) ANIMATION_DURATION_MS);

        double start;
        double end;
        if(player == player1){
            start = p1StartCol;
            end = p1EndCol;
        }else{
            start = p2StartCol;
            end = p2EndCol;
        }
        return start + (end - start) * progress;
    }
}
