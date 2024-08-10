import java.util.*;
import java.io.*;
import java.time.*;

class OS{

    class LRU{
        HashMap<Integer, Integer> lastRun;
        int maxtime;

        public LRU(){
            maxtime = 0;
            lastRun = new HashMap<>();
        }

        public int getLRU(){
            int pid = -1;
            int lasttime = Integer.MAX_VALUE;
            for(Map.Entry<Integer, Integer> e : lastRun.entrySet())
                if(lasttime > e.getValue()){
                    lasttime = e.getValue();
                    pid = e.getKey();
                }
            return pid;
        }

        public void setMRU(int pid){
            lastRun.put(pid, maxtime++);
        }

        public void remove(int pid){
            lastRun.remove(pid);
        }

        public void add(int pid){
            lastRun.put(pid, Integer.MAX_VALUE);
        }

        public ArrayList<Integer> getList(){
            List<Map.Entry<Integer, Integer>> list = new ArrayList<>(lastRun.entrySet());
            list.sort(Map.Entry.comparingByValue());
            ArrayList<Integer> sortedKeys = new ArrayList<>();
            if(Global.debug){
                System.out.println("Printing LRU : ");
            }
            for(Map.Entry<Integer, Integer> e : list){
                if(Global.debug){
                    System.out.println(e.getKey()+" "+e.getValue());
                }
                sortedKeys.add(e.getKey());
            }
            // Collections.reverse(sortedKeys);
            return sortedKeys;
        }
    }

    ///// GLOBAL VARS /////
    int pidCounter;
    int vmemfree;
    int pmemfree;
    int[] pPageData;
    int[] vPageData;
    Memory pmem;
    Memory vmem;
    int pageSize;
    HashMap<Integer, ProcStruct> processMap;
    LRU lastRun;
    int currentPID;
    ///////////////////////
    
    public enum Location{
        NOWHERE,
        VMEM,
        PMEM
    };

    class ProcStruct{
        public int pid;
        public String fileName;
        public int size; // in bytes
        public int[] pgtable;
        public Location loc;
        public Commands com;
    }
    
    public OS(Memory _pmem, Memory _vmem, int _pgsize){
        pmem = _pmem;
        vmem = _vmem;
        pageSize = _pgsize;
        pidCounter = 1;
        vmemfree = vmem.getSize();
        pmemfree = pmem.getSize();
        pPageData = new int[pmemfree/pageSize];
        vPageData = new int[vmemfree/pageSize];
        processMap = new HashMap<>();
        lastRun = new LRU();
    }

    ////////////////// MEMORY API //////////////////////

    private void pAssignPage(int pno, int pid){
        pPageData[pno] = pid; 
        pmemfree -= pageSize;
    }

    private void vAssignPage(int vno, int pid){
        vPageData[vno] = pid;
        vmemfree -= pageSize;
    }

    private void pDeassignPage(int pno){
        for(int i=0; i<pageSize; i++)
            vmem.write(pno*pageSize+i, 0);
        pPageData[pno] = 0; 
        pmemfree += pageSize;
    }

    private void vDeassignPage(int vno){
        for(int i=0; i<pageSize; i++)
            vmem.write(vno*pageSize+i, 0);
        vPageData[vno] = 0; 
        vmemfree += pageSize;
    }

    private Integer[] pCutPage(int pno){
        pDeassignPage(pno);
        Integer[] page = new Integer[pageSize];
        for(int i=0; i < pageSize; i++){
            page[i] = pmem.read(pno*pageSize + i);
            pmem.write(pno*pageSize+i, 0);
        }
        return page;
    }

    private Integer[] vCutPage(Integer pno){
        vDeassignPage(pno);
        Integer[] page = new Integer[pageSize];
        for(Integer i=0; i < pageSize; i++){
            page[i] = vmem.read(pno*pageSize + i);
            vmem.write(pno*pageSize+i, 0);
        }
        return page;
    }
    
    // it is expected that you call Assign before this
    private void pPastePage(int pno, Integer[] page){
        for(int i=0; i<pageSize; i++)
            pmem.write(pno*pageSize+i, page[i]);
    }

    // it is expected that you call Assign before this
    private void vPastePage(int pno, Integer[] page){
        for(int i=0; i<pageSize; i++)
            vmem.write(pno*pageSize+i, page[i]);
    }

    private int pGetFreePage(){
        for(int i=0; i<pPageData.length; i++)
            if(pPageData[i]==0)
                return i;
        return -1;
    }

    private int vGetFreePage(){
        for(int i=0; i<vPageData.length; i++)
            if(vPageData[i]==0)
                return i;
        return -1;
    }

    ////////////////////////////////////////////////////

    ////////////////// PROCESS API ////////////////////

    private int RoundUp(int num, int den){
        return (num+den-1)/den;
    }

    private ProcStruct CreateProc(String fileName){
        ProcStruct newProcess = new ProcStruct();
        newProcess.pid = pidCounter++;
        newProcess.fileName = fileName;

        // get the first integer from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            newProcess.size = Integer.parseInt(reader.readLine().trim());
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        newProcess.size *= 1024;

        newProcess.com = new Commands(fileName);
        newProcess.pgtable = new int[RoundUp(newProcess.size, pageSize)];
        newProcess.loc = Location.NOWHERE;

        // Add it to the processMap
        processMap.put(newProcess.pid, newProcess);
        
        // Add it to the lastRun 
        lastRun.add(newProcess.pid);

        return newProcess;
    }

    // doesn't handle page table of the process
    private boolean pCutFirstPage(int pid, Box<Integer[]> page){
        int i=0;
        while(pPageData[i]!=pid && i<pPageData.length)
            i++;
        if(i==pPageData.length)
            return false;
        page.content = pCutPage(i);
        return true;
    }

    // doesn't handle page table of the process
    private boolean vCutFirstPage(int pid, Box<Integer[]> page){
        int i=0;
        while(vPageData[i]!=pid && i<vPageData.length)
            i++;
        if(i==vPageData.length)
            return false;
        page.content = vCutPage(i);
        return true;
    }

    // assumes that pid is present in the map
    private void removeProc(int pid){
        ProcStruct myproc = processMap.get(pid);
        // deallocate all the pages
        if(myproc.loc == Location.PMEM){
            for(int i=0; i<myproc.pgtable.length; i++)
                pDeassignPage(myproc.pgtable[i]);
        }
        else if(myproc.loc == Location.VMEM){
            for(int i=0; i<myproc.pgtable.length; i++)
                vDeassignPage(myproc.pgtable[i]);
        }
        // remove process from map
        processMap.remove(pid);
        // remove from LRU
        lastRun.remove(pid);
    }

    private int pMakeSpace(int exceptionPID){
        // assume process exists
        ProcStruct myproc = processMap.get(exceptionPID);
        int procsize = RoundUp(myproc.size, pageSize)*pageSize;
        if(procsize > pmem.getSize())
            return -1; // Program cannot fit in memory
        ArrayList<Integer> allpid = lastRun.getList();
        for(int i=0; i<allpid.size(); i++){
            if(pmemfree < procsize && allpid.get(i) != exceptionPID){
                int ret = swapout(allpid.get(i));
                if(ret>=0 && Global.debug){
                    System.out.println("Swapping out "+allpid.get(i));
                }
            }
        }
        if(pmemfree < procsize)
            return -2; // could not get enough space
        return 0;
    }

    ////////////////////// RUNNING PROCESS ////////////////////////////

    public int run(int pid, Box<String> box){
        if(processMap.containsKey(pid)==false)
            return -1; // invalid pid
        if(Global.debug){
            System.out.println("Trying to run "+pid);
        }
        // bring the process to pmem
        int possible = swapin(pid);
        if(possible < 0 && possible != -2){
            return -2; // no space
        }
        lastRun.setMRU(pid);
        currentPID = pid;
        int ret = (new Cpu(this, processMap.get(pid).com).run(box));
        if(Global.debug){
            System.out.println("From OS: running "+box.content+"endOS\n");
        }
        if(ret == -1)
            ret = -3;
        return ret;
    }

    public int read(int vloc){
        // Assume its correct
        int vPageNumber = vloc/pageSize;
        int pageOffset = vloc-pageSize*vPageNumber;
        // now convert it to Physical Page Number
        ProcStruct curProc = processMap.get(currentPID);
        int pPageNumber = curProc.pgtable[vPageNumber];
        return pmem.read(pPageNumber*pageSize+pageOffset);
    }
    
    public void write(int vloc, int data){
        // Assume its correct
        int vPageNumber = vloc/pageSize;
        int pageOffset = vloc-pageSize*vPageNumber;
        // now convert it to Physical Page Number
        ProcStruct curProc = processMap.get(currentPID);
        int pPageNumber = curProc.pgtable[vPageNumber];
        pmem.write(pPageNumber*pageSize+pageOffset, data);
    }

    /////////////////// USER ORIENTED FUNCTIONS ///////////////////////

    public String memprint(int start, int len){
        String ret = "Printing memory locations from "+ start+":\n";
        for(int i=0; i<len; i++){
            ret = ret + pmem.read(start+i) + "\n";
        }
        return ret;
    }

    public int kill(int pid){
        if(processMap.containsKey(pid)==false)
            return -1; // invalid pid
        removeProc(pid);
        return 0;
    }


	public String listpr(){
	    String ret = "Processes in Main Memory\n";
	    for(Map.Entry<Integer, ProcStruct> entry : processMap.entrySet())
	        if(entry.getValue().loc == Location.PMEM)
	            ret += entry.getKey() + "\n";
	
	    ret += "Processes in Virtual Memory\n";
	    for(Map.Entry<Integer, ProcStruct> entry : processMap.entrySet())
	        if(entry.getValue().loc == Location.VMEM)
	            ret += entry.getKey() + "\n";
	
	    ret += "**********************************\n";
	    
	    return ret;
	}

    public int swapout(int pid){
        if(processMap.containsKey(pid)==false){
            return -1; // invalid pid
        }
        ProcStruct myproc = processMap.get(pid);
        if(myproc.loc != Location.PMEM){
            if(Global.debug){
                System.out.println("DB: "+myproc.loc);
            }
            return -2; // not in physical memory
        }
        if(vmemfree < RoundUp(myproc.size, pageSize)*pageSize){
            return -3; // not enough space in virtual memory
        }

        for(int i=0; i<myproc.pgtable.length; i++){
            Integer[] page = pCutPage(myproc.pgtable[i]);
            int freePgNo = vGetFreePage();
            vAssignPage(freePgNo, pid);
            myproc.pgtable[i] = freePgNo;
            vPastePage(freePgNo, page);
        }
        if(Global.debug){
            System.out.println("Switching "+myproc.pid+" to Vmem");
        }
        myproc.loc = Location.VMEM;

        return 0;
    }

    public int swapin(int pid){
        if(processMap.containsKey(pid)==false){
            return -1; // invalid pid
        }
        ProcStruct myproc = processMap.get(pid);
        if(myproc.loc != Location.VMEM){
            return -2; // not in virtual memory
        }
        int possible = pMakeSpace(pid);
        if(possible<0){
            return -3; // error in swapin (either process to large to fit or internal error)
        }

        for(int i=0; i<myproc.pgtable.length; i++){
            Integer[] page = vCutPage(myproc.pgtable[i]);
            int freePgNo = pGetFreePage();
            pAssignPage(freePgNo, pid);
            myproc.pgtable[i] = freePgNo;
            pPastePage(freePgNo, page);
        }

        myproc.loc = Location.PMEM;

        return 0;
    }

    public int printPTE(int pid, String fileName){
        BufferedWriter outfile; 
        try{
            outfile = new BufferedWriter(new FileWriter(fileName, true));

	        if(processMap.containsKey(pid)){
	            ProcStruct proc = processMap.get(pid);
	            if(proc.loc==Location.NOWHERE){
                    outfile.close();
	                return -1;
                }
	            else 
	                outfile.write("Time: "+LocalDateTime.now()+"\n");       
	            if(proc.loc==Location.VMEM)
	                outfile.write("Page numbers refer to Virtual Memory\n");
	            else
	                outfile.write("Page numbers refer to Physical Memory\n");
	            for(int i=0; i<proc.pgtable.length; i++)
	                outfile.write(i+" : "+proc.pgtable[i]+"\n");
                outfile.close();
	            return 0;
	        }
	        else{
                outfile.close();
	            return -1;
            }
        }
        catch(IOException e){
            e.printStackTrace();
            return -2;
        }
    }

    public int printPTEall(String filename){
        BufferedWriter outfile; 
        try{
            outfile = new BufferedWriter(new FileWriter(filename, true));

	        outfile.write("Time: "+LocalDateTime.now()+"\n");       
	
	        for(Map.Entry<Integer, ProcStruct> entry : processMap.entrySet()){
	            ProcStruct p = entry.getValue();
	            if(p.loc != Location.NOWHERE){
	                outfile.write("PID: "+entry.getKey()+"\n");
                    if(p.loc==Location.VMEM)
                        outfile.write("Page Frame Numbers refers to Virtual Memory\n");
                    else if(p.loc==Location.PMEM)
                        outfile.write("Page Frame Numbers refer to Physical Memory\n");
	                for(int i=0; i<p.pgtable.length; i++)
	                    outfile.write(i+" : "+p.pgtable[i]+"\n");
	            }
	        }

            outfile.close();
	        return 0;
        }
        catch(IOException e){
            return -2;
        }
    }

    public int load(String fileName, Box<Integer> pid){
        ProcStruct newp = CreateProc(fileName);
        pid.content = newp.pid;
        if(RoundUp(newp.size, pageSize)*pageSize < pmemfree){
            // accomodate in physical memory
            for(int i=0; i<RoundUp(newp.size,pageSize); i++){
                int freePageNumber = pGetFreePage();
                newp.pgtable[i] = freePageNumber;
                pAssignPage(freePageNumber, newp.pid);
            }
            newp.loc = Location.PMEM;
            return 1;
        }
        else if(RoundUp(newp.size, pageSize)*pageSize < vmemfree){
            if(Global.debug){
                System.out.println("Can't fit process "+fileName+" in Physical Memory.");
                System.out.println("Required size = "+newp.size+", Available size = "+pmemfree);
            }
            for(int i=0; i<RoundUp(newp.size,pageSize); i++){
                int freePageNumber = vGetFreePage();
                newp.pgtable[i] = freePageNumber;
                vAssignPage(freePageNumber, newp.pid);
            }
            newp.loc = Location.VMEM;
            return 2;
        }
        else{
            removeProc(newp.pid);
            if(Global.debug){
                System.out.println("Can't fit process "+fileName+" in Virtual Memory.");
                System.out.println("Required size = "+newp.size+", Available size = "+vmemfree);
            }
            return -1;
        }
    }
}
