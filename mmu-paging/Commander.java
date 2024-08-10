import java.util.*;
import java.io.*;

public class Commander{
    BufferedReader inputFile;
    BufferedWriter outputFile;
    OS os;

    public Commander(String inputFileName, String outputFileName, OS s) throws Exception{
        inputFile = new BufferedReader(new FileReader(inputFileName));
        outputFile = new BufferedWriter(new FileWriter(outputFileName));
        os = s;
    }

    private void loadCommand(String args){
        try{
	        String[] tokens = args.split("\\s+");
	        for(int i=1; i<tokens.length; i++){
	            Box<Integer> pidAssigned = new Box<Integer>(); 
	            int flag = os.load(tokens[i], pidAssigned);
	            if (flag == -1) { // Memory is full
	                outputFile.write(tokens[i] + " could not be loaded - memory is full\n");
	            }
	            else if (flag == -2) { // File does not exist
	                outputFile.write(tokens[i] + " could not be loaded - file does not exist\n");
	            }
	            else if (flag == 1) { // Loaded into PMem
	                outputFile.write(tokens[i] + " is loaded in physical memory and is assigned process id " + pidAssigned.content + "\n");
	            }
	            else if (flag == 2) { // Loaded into VMem
	                outputFile.write(tokens[i] + " is loaded in virtual memory and is assigned process id " + pidAssigned.content + "\n");
	            }
	        }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void pteAll(String args){
        String[] tokens = args.split("\\s+");
        if(tokens.length != 2){
            if(Global.debug)
                System.out.println("Pteall: Argument not provided to pteall");
            return;
        }
        int ret = os.printPTEall(tokens[1]);
        try{
            if(ret==-2){
                outputFile.write("Pteall: File Error\n");
            }
            else{
                outputFile.write("Pteall: done \n");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void pte(String args){
        String[] tokens = args.split("\\s+");
        int ret = os.printPTE(Integer.parseInt(tokens[1]), tokens[2]); 
        try{
            if(ret==-1){
                outputFile.write("Pte: Invalid PID\n");
            }
            else if(ret==-2){
                outputFile.write("Pte: File Error\n");
            }
            else{
                outputFile.write("Pte: done \n");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void swapin(String args){
        String[] tokens = args.split("\\s+");
        int ret = os.swapin(Integer.parseInt(tokens[1]));
        try{
            if(ret==-1){
                outputFile.write("Swapin: Invalid PID\n");
            }
            else if(ret==-2){
                outputFile.write("Swapin: Process not in Virtual Memory\n");
            }
            else if(ret==-3){
                outputFile.write("Swapin: Not Enough Space in Physical Memory\n");
            }
            else{
                outputFile.write("Swapin: Done\n");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void swapout(String args){
        String[] tokens = args.split("\\s+");
        int ret = os.swapout(Integer.parseInt(tokens[1]));
        try{
            if(ret==-1){
                outputFile.write("Swapout: Invalid PID\n");
            }
            else if(ret==-2){
                outputFile.write("Swapout: Process not in Physical Memory\n");
            }
            else if(ret==-3){
                outputFile.write("Swapout: Not Enough Space in Virtual Memory\n");
            }
            else{
                outputFile.write("Swapout: Done\n");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void kill(String args){
        String[] tokens = args.split("\\s+");
        int ret = os.kill(Integer.parseInt(tokens[1]));
        try{
            if(ret==-1){
                outputFile.write("Kill: invalid pid "+tokens[1]+"\n");
            }
            else{
                outputFile.write("Kill: killed process "+tokens[1]+"\n");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void run(String args){
        String[] tokens = args.split("\\s+");
        Box<String> msg = new Box<>();
        int pid = Integer.parseInt(tokens[1]);
        int ret = os.run(pid, msg);
        try{
            if(ret==-1)
                outputFile.write("Run: Invalid PID\n");
            else if(ret==-2)
                outputFile.write("Run: Cannot fit process in main memory\n");
            else if(ret == -3)
                outputFile.write("Running process "+tokens[1]+"\n"+msg.content+" specified for process id "+pid+"\n");
            else
                outputFile.write("Running process "+tokens[1]+"\n"+msg.content);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void print(String args){
        String[] tokens = args.split("\\s+");
        String ret = os.memprint(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
        try{
            outputFile.write(ret);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void listpr(){
        try{
            outputFile.write(os.listpr());
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void executeCommands(){
        String line;
        try{
            while((line = inputFile.readLine()) != null){
                line = line.trim();

                if(line.startsWith("load")){
                    loadCommand(line);
                }
                else if(line.startsWith("pteall")){
                    pteAll(line);
                }
                else if(line.startsWith("pte")){
                    pte(line);
                }
                else if(line.startsWith("swapin")){
                    swapin(line);
                }
                else if(line.startsWith("swapout")){
                    swapout(line);
                }
                else if(line.startsWith("listpr")){
                    listpr();    
                }   
                else if(line.startsWith("kill")){
                    kill(line);
                }
                else if(line.startsWith("run")){
                    run(line);
                }
                else if(line.startsWith("print")){
                    print(line);
                }
                else if(line.startsWith("exit"))
                    break;
            }

            // CLOSE THE FILES
            inputFile.close();
            outputFile.write("Exiting...\n");
            outputFile.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
