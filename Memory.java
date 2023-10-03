import java.util.*;

class Memory{
    int[] arr;
    int bound;

    public Memory(int size){
        // initialise an array of size (all zeroes)
        arr = new int[size];
        bound = size;
    }
        
    public int read(int loc){
        if(loc<0 || loc>bound){
            System.out.println("Invalid Physical Memory Location");
            return 0;
        }
        return arr[loc];
    }

    public void write(int loc, int data){
        if(loc<0 || loc>bound){
            System.out.println("Invalid Physical Memory Location");
            return;
        }
//        if(Global.debug)
//            System.out.println("Debug is ON");
        arr[loc] = data;
    }

    public int getSize(){
        return bound;
    }
}
