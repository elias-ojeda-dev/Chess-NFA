import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathFileWriter{
    private final Path outputDirectory;

    public PathFileWriter(){
        this("computing");
    }

    public PathFileWriter(String directoryName){
        this.outputDirectory = Paths.get(directoryName);
        try{
            Files.createDirectories(outputDirectory);
        }catch(IOException e){
            System.out.println("No se pudo crear el directorio de salida: " + outputDirectory.toAbsolutePath());
        }
    }

    public void writePaths(List<List<Integer>> paths, String fileName){
        Path filePath = outputDirectory.resolve(fileName);

        try(PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))){
            for(List<Integer> path : paths){
                for(int i=0; i<path.size(); i++){
                    writer.print(path.get(i));
                    if(i < path.size() - 1){
                        writer.print(" ");
                    }
                }
                writer.println();
            }
        }catch(IOException e){
            System.out.println("Error al guardar archivo: " + filePath.toAbsolutePath());
        }
    }
}
