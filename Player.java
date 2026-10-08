import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player{
    private final String name;
    private final int initialState;
    private final int finalState;
    private final Color tokenColor;

    private String input;
    private List<List<Integer>> paths = new ArrayList<>();
    private List<List<Integer>> winningPaths = new ArrayList<>();
    private List<Integer> currentPath = new ArrayList<>();
    private int currentPosition;

    public Player(String name, int initialState, int finalState, Color tokenColor){
        this.name = name;
        this.initialState = initialState;
        this.finalState = finalState;
        this.tokenColor = tokenColor;
        this.currentPosition = initialState;
    }

    public String getName(){
        return name;
    }

    public int getInitialState(){
        return initialState;
    }

    public int getFinalState(){
        return finalState;
    }

    public Color getTokenColor(){
        return tokenColor;
    }

    public String getInput(){
        return input;
    }

    public void setInput(String input){
        this.input = input;
    }

    public List<List<Integer>> getPaths(){
        return paths;
    }

    public List<List<Integer>> getWinningPaths(){
        return winningPaths;
    }

    public List<Integer> getCurrentPath(){
        return currentPath;
    }

    public void setCurrentPath(List<Integer> currentPath){
        this.currentPath = new ArrayList<>(currentPath);
    }

    public void setCurrentPosition(int currentPosition){
        this.currentPosition = currentPosition;
    }

    public int getCurrentPosition(){
        return currentPosition;
    }

    public void resetPosition(){
        this.currentPosition = initialState;
    }

    public void applySimulationResult(SimulationResult result){
        this.paths = result.getAllPaths();
        this.winningPaths = result.getWinningPaths();
    }

    public boolean chooseWinningPath(Random random){
        if(winningPaths.isEmpty()){
            currentPath = null;
            return false;
        }
        currentPath = new ArrayList<>(winningPaths.get(random.nextInt(winningPaths.size())));
        currentPosition = currentPath.get(0);
        return true;
    }

    public boolean chooseAnyPath(Random random){
        if(paths.isEmpty()){
            currentPath = null;
            return false;
        }
        currentPath = new ArrayList<>(paths.get(random.nextInt(paths.size())));
        currentPosition = currentPath.get(0);
        return true;
    }
}
