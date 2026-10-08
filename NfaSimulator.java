import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NfaSimulator{
    private final int boardSize;

    public NfaSimulator(int boardSize){
        this.boardSize = boardSize;
    }

    public SimulationResult simulate(String input, int initialState, int finalState){
        List<List<Integer>> allPaths = new ArrayList<>();
        List<List<Integer>> winningPaths = new ArrayList<>();

        List<Integer> initialPath = new ArrayList<>();
        initialPath.add(initialState);
        allPaths.add(initialPath);

        for(int i=0; i<input.length(); i++){
            char color = input.charAt(i);
            List<List<Integer>> newPaths = new ArrayList<>();

            for(List<Integer> path : allPaths){
                int lastState = path.get(path.size() - 1);
                Set<Integer> nextStates = nextStates(lastState, color);

                for(int nextState : nextStates){
                    List<Integer> pathCopy = new ArrayList<>(path);
                    pathCopy.add(nextState);
                    newPaths.add(pathCopy);
                }
            }

            allPaths = newPaths;
        }

        for(List<Integer> path : allPaths){
            if(path.get(path.size() - 1) == finalState){
                winningPaths.add(path);
            }
        }

        return new SimulationResult(allPaths, winningPaths);
    }

    private Set<Integer> nextStates(int currentState, char color){
        Set<Integer> nextStates = new HashSet<>();
        int row = (currentState - 1) / boardSize;
        int col = (currentState - 1) % boardSize;

        if(color == 'r'){
            if(row > 0){
                nextStates.add(currentState - boardSize);
            }
            if(row < boardSize - 1){
                nextStates.add(currentState + boardSize);
            }
            if(col > 0){
                nextStates.add(currentState - 1);
            }
            if(col < boardSize - 1){
                nextStates.add(currentState + 1);
            }
        }else{
            if(row > 0 && col > 0){
                nextStates.add(currentState - boardSize - 1);
            }
            if(row > 0 && col < boardSize - 1){
                nextStates.add(currentState - boardSize + 1);
            }
            if(row < boardSize - 1 && col > 0){
                nextStates.add(currentState + boardSize - 1);
            }
            if(row < boardSize - 1 && col < boardSize - 1){
                nextStates.add(currentState + boardSize + 1);
            }
        }

        return nextStates;
    }
}
