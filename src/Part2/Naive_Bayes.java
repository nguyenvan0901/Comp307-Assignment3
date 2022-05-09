package Part2;

import javax.transaction.xa.Xid;
import java.io.File;
import java.util.*;

public class Naive_Bayes {

    // this field contains all the attribute names.
    private List<String> attribute_names = new ArrayList<>();
    private List<String> train_label = new ArrayList<>();
    private List<Instance> train_instances = new ArrayList<>();

    // this field holds all the possible value of class label of each attribute
    private List<HashSet<String>> attribute_values = new ArrayList<>();

    // this field holds all data for     count(Xi, xi, y).
    // each hashmap is 1 attribute.
    private List<HashMap<String, Integer>> counts = new ArrayList<>();

    // this field holds all data for    count(y).
    private HashMap<String, Integer> y_count = new HashMap<>();

    // this field holds all data for    total(Xi,y).
    private HashMap<String, Integer> total = new HashMap<>();



    public Naive_Bayes(){

        File file = new File("src/Part2/breast-cancer-test.csv");

        try{
            Scanner sc = new Scanner(file);

            String attributes_string = sc.next();
            String[] attributes = attributes_string.split(",");

            for(int i=1; i<attributes.length; i++){

                attribute_names.add(attributes[i]);
                attribute_values.add(new HashSet<>());

            }

            for(int i=0; i<attribute_names.size(); i++){

                counts.add(new HashMap<>());

            }

            while(sc.hasNext()){

                String line = sc.next();
                String[] parts = line.split(",");

                train_label.add(parts[1]);

                for(int i=1; i<parts.length; i++){

                    attribute_values.get(i-1).add(parts[i]);

                }

                train_instances.add(new Instance(parts));

            }

        }catch(Exception e){
            System.out.println("Something is wrong");
        }



        this.trainNB();

//        for(Map.Entry<String, Integer> entry: counts.entrySet()){
//            System.out.println(entry.toString());
//        }

    }

    public void trainNB(){

        this.initialiseCount();
        this.updateCount();
        //this.updateTotal();

    }

    public void initialiseCount(){

        // getting all the distinct class label.
        HashSet<String> class_labels = attribute_values.get(0);

        // Initialising count(y) variables.
        for(String label: class_labels){

            y_count.put(label, 1);

        }

        // Initialising count(Xi, xi, y) variables.
        for(int i=1; i<attribute_values.size(); i++){

            // distinct value of each attribute column.
            HashSet<String> Xi = attribute_values.get(i);

            // the map that will track count(Xi, xi, y)
            HashMap<String, Integer> record = counts.get(i);

            for(String value: Xi){

                for(String label: class_labels){

                    String key =  label + " / " + value;
                    record.put(key,1);

                }

            }

        }

    }

    public void updateCount(){

        for(Instance instance: train_instances){

            

        }

    }

    public void updateTotal(){



    }



    public static void main(String[] args){
        new Naive_Bayes();
    }
}
