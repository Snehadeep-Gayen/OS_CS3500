import java.util.*;
import java.io.*;

public class Commands {
    private List<String> commandsList;
    int commandNumber;

    public Commands(String filename) {
        commandsList = new ArrayList<>();
        readCommandsFromFile(filename);
        commandNumber = 1;
    }

    private void readCommandsFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                commandsList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getSize(){
        return Integer.parseInt(commandsList.get(0));
    }

    public String getNextCommand(){
        if(commandNumber>=commandsList.size())
            return null;
        return commandsList.get(commandNumber++);
    }

    public List<String> getCommandsList() {
        return commandsList;
    }
}