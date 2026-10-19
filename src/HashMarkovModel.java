import java.util.*;

public class HashMarkovModel extends BaseMarkovModel {

    private HashMap < List <String>, List <String> > hm = new HashMap<>();

    public HashMarkovModel(int size){
        super(size);
    }

    public HashMarkovModel(){
        this(3);
    }
    @Override 
    public List<String> getFollows(List<String> context){
        List<String> s = new ArrayList<>();
        if(hm.containsKey(context)){
            s= hm.get(context);
            return s;
        }
        else{
            return new ArrayList<String>(0);
        }

    }

    public void processTraining(){
        hm.clear();
        for(int i=0; i<myWordSequence.size()- myModelSize;i++){
            List<String> context = (myWordSequence.subList(i, i + myModelSize));
            if(!hm.containsKey(context)){
                hm.put(context, new ArrayList<>() );
            }
            hm.get(context).add(myWordSequence.get(i + myModelSize));
            
        }
    } 
}
