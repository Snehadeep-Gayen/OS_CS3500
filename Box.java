import java.util.*;

class Box<T>{
    public T content;

    public Box(T t){
        this.content = t;
    }
    
    public Box(){
        this.content = null;
    }
}
