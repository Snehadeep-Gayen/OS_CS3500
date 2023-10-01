import java.util.*;

class Memory{
    byte[] arr;
    int bound;

    public Memory(int size){
        // initialise an array of size (all zeroes)
        arr = new byte[size];
        bound = size;
    }
        
    public byte read(int loc){
        if(loc<0 || loc>bound){
            System.out.println("Invalid Physical Memory Location");
            return 0;
        }
        return arr[loc];
    }

    public void write(int loc, byte data){
        if(loc<0 || loc>bound){
            System.out.println("Invalid Physical Memory Location");
            return;
        }
        if(Global.debug)
            System.out.println("Debug is ON");
        arr[loc] = data;
    }

    public int getSize(){
        return bound;
    }
}
