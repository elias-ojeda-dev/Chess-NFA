import java.util.List;

public class SimulationResult{
    private final List<List<Integer>> allPaths;
    private final List<List<Integer>> winningPaths;

    public SimulationResult(List<List<Integer>> allPaths, List<List<Integer>> winningPaths){
        this.allPaths = allPaths;
        this.winningPaths = winningPaths;
    }

    public List<List<Integer>> getAllPaths(){
        return allPaths;
    }

    public List<List<Integer>> getWinningPaths(){
        return winningPaths;
    }
}
