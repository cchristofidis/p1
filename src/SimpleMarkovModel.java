import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.*;
import java.io.File;


/**
 *
 * @author Chris Christofidis
 * @version 0.9 , needs to be updated
 */


public class SimpleMarkovModel extends BaseMarkovModel  {

    @Override
    protected List<String> getFollows(List<String> context) {

        ArrayList <String> s = new ArrayList<> ();
        for(int i=0;i<myWordSequence.size()-context.size();i++){
            if(myWordSequence.subList(i, i+context.size()).equals(context))
                s.add(myWordSequence.get(i+context.size()));
        }
        
        return s;
    }

    public SimpleMarkovModel(int size){
        super(size);
    }

    public SimpleMarkovModel(){
        this(3);
    }

    public static void main(String[] args) throws IOException {
        SimpleMarkovModel mm = new SimpleMarkovModel(2);
        String dirName = "data/shakespeare";

        int size = 500;
        mm.trainDirectory(dirName);
        System.out.printf("seq size: %d\n",mm.getSequence().size());
        String text = mm.generate(size);

        System.out.printf("size of generated text = %d\n",text.length());
        System.out.printf("# different contexts possible = %d\n",mm.differentContexts());
    }
}
