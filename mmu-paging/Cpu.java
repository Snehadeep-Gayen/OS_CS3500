class Cpu{
    OS os;
    int bound;
    Commands com;

    public Cpu(OS os, Commands com){
        this.os = os;
        this.com = com;
        this.bound = com.getSize()*1024;
    }

    public int run(Box<String> msg){
        String ret = "";
        try{
            String command = com.getFirstCommand();
            while(command!=null){
                ret = ret + interpret(command) + "\n";
                command = com.getNextCommand();
            }
            msg.content = ret;
            if(Global.debug){
                System.out.println("From CPU  : "+msg.content+"**\n"+ret+"***\n");
            }
            return 0;
        }
        catch(Exception e){
            ret = ret + e.getMessage();
            msg.content = ret;
            if(Global.debug){
                System.out.println("From CPU exc : "+msg.content);
            }
            if(Global.debug)
                e.printStackTrace();
            return -1;
        }
    }
    
    private int read(int loc) throws ArithmeticException {
        if(loc<0 || loc>=bound)
            throw new ArithmeticException("Invalid Memory Address "+ loc);
        return os.read(loc);
    }

    private void write(int loc, int val) throws ArithmeticException {
        if(loc<0 || loc>=bound)
            throw new ArithmeticException("Invalid Memory Address "+loc);
        os.write(loc, val);
    }

    private String interpret(String command) throws ArithmeticException { 
        String ret = "";
        command = command.trim();
        String[] tokens = command.split("[,\\s]+");
        if(Global.debug){
            System.out.println("Comm: "+command);
            System.out.println("tokens ");
            for(int i=0; i<tokens.length; i++)
                System.out.println(tokens[i]);
            System.out.println("end tokens");
        }
        if(tokens[0].equals("add")){
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            int z = Integer.parseInt(tokens[3]);
            write(z, read(x)+read(y));
            ret = ret + "Command: add ";
            ret = ret + x + " ";
            ret = ret + y + " ";
            ret = ret + z + " ";
            ret = ret + "Result: Value in addr ";
            ret = ret + "x = " + read(x) + ", ";
            ret = ret + "y = " + read(y) + ", ";
            ret = ret + "z = " + read(z);
        }
        else if(tokens[0].equals("sub")){
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            int z = Integer.parseInt(tokens[3]);
            write(z, read(x)-read(y));
            ret = ret + "Command: sub ";
            ret = ret + x + ", ";
            ret = ret + y + ", ";
            ret = ret + z + " ";
            ret = ret + "Result: Value in addr ";
            ret = ret + "x = " + read(x) + ", ";
            ret = ret + "y = " + read(y) + ", ";
            ret = ret + "z = " + read(z);
        }
        else if(tokens[0].equals("print")){
            int x = Integer.parseInt(tokens[1]);
            ret = ret + "Command: print "+x;
            ret += "; Result: Value in addr "+ x + " = " + read(x);
        }
        else if(tokens[0].equals("load")){
            int a = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            write(y, a);
            ret = ret + "Command: load "+tokens[1]+" "+tokens[2];
            ret = ret +"; Result: Value of "+read(y)+" is stored in addr "+tokens[2];
        }
        if(Global.debug){
            System.out.println("From INTERPRETER : "+ret+"Iend\n");
        }
        return ret;

    }
}
