package Part2;

import java.util.ArrayList;
import java.util.List;

public class Instance {

    private List<String> values = new ArrayList<>();

    private String label;

    public Instance(String[] parts){

        this.label = parts[1];
        for(int i=2; i<parts.length; i++){
            values.add(parts[i]);
        }

    }

    public String getAttribute(int index){
        return values.get(index);
    }

    public List<String> getValues(){
        return values;
    }

    public String getLabel(){
        return label;
    }

    public String toString(){
        return values.toString();
    }
}
