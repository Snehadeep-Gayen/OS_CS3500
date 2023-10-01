import java.util.*;
import java.io.*;

class OS extends Global{
    int pidCounter;
    int vmemfree;
    int pmemfree;
    ArrayList<Integer> pPageData;
    ArrayList<Integer> vPageData;
    
    public enum Location{
        NOWHERE,
        VMEM,
        PMEM
    };

    class ProcStruct{
        public int pid;
        public String fileName;
        public int size; // in bytes
        public HashMap<Integer, Integer> pgtable;
        public Location loc;
        public Commands com;
    }
    
    public OS(){
        pidCounter = 1;
        vmemfree = vmem.getSize();
        pmemfree = pmem.getSize();
        pPageData = new ArrayList<Integer>();
        for(int i=0; i< pmemfree/Global.pageSize; i++)
            pPageData.add(Integer(0));
    }

    private ProcStruct createProc(String fileName){
        ProcStruct newProcess = new ProcStruct();
        newProcess.pid = pidCounter++;
        newProcess.fileName = fileName;
        newProcess.com = new Commands(fileName);
        newProcess.pgtable = new HashMap<>();
        newProcess.loc = Location.NOWHERE;
        return newProcess;
    }

    public Integer load(String fileName){
        ProcStruct newp = createProc(fileName);
        if(newp.size<pmemfree){
            // accomodate in physical memory
        }
        else if(newp.size<vmemfree){
            if(Global.debug){
                System.out.println("Can't fit process "+fileName+" in Physical Memory.");
                System.out.println("Required size = "+newp.size+", Available size = "+pmemfree);
            }

        }
        else{
            if(Global.debug){
                System.out.println("Can't fit process "+fileName+" in Virtual Memory.");
                System.out.println("Required size = "+newp.size+", Available size = "+vmemfree);
            }

        }
    }
}
