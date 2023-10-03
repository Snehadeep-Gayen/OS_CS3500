import java.util.*;
import java.io.*;

class Global{

    static boolean debug = false;
    protected static BufferedWriter outputFile;
    protected static BufferedReader inputFile;
    protected static Memory pmem;
    protected static Memory vmem;
    protected static int pageSize;

    public static void main(String[] args) throws Exception{

        // IMPORTANT VARIABLES //
        Memory pmem;
        Memory vmem;
        int pageSize = -1;
        ////////////////////////

        int memorySize = 0;
        int vmemSize = 0;
        String inputFileName = null;
        String outputFileName = null;

        // Loop through the command-line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-M")) {
                // Get the value following the -M option
                memorySize = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-V")) {
                // Get the value following the -V option
                vmemSize = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-P")) {
                // Get the value following the -P option
                pageSize = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-i")) {
                // Get the input file name
                inputFileName = args[++i];
            } else if (args[i].equals("-o")) {
                // Get the output file name
                outputFileName = args[++i];
            }
        } 
        
        if(debug){
	        System.out.println("Memory Size: " + memorySize);
	        System.out.println("Virtual Memory: " + vmemSize);
	        System.out.println("Process Count: " + pageSize);
	        System.out.println("Input File: " + inputFileName);
	        System.out.println("Output File: " + outputFileName);
        }
        
        // declare a Physical Memory of input size
        pmem = new Memory(memorySize*1024);

        // declare a Virtual Memory of input size
        vmem = new Memory(vmemSize*1024);

        OS s = new OS(pmem, vmem, pageSize);

        Commander comm = new Commander(inputFileName, outputFileName, s);

        comm.executeCommands();
    }
}
