package Part2;

import javax.transaction.xa.Xid;
import java.io.File;
import java.util.*;

public class Naive_Bayes {
    private List<Instance> train_instances = new ArrayList<>();

    // this field contains all the attribute names.
    private List<String> attribute_names = new ArrayList<>();

    // this field holds all the possible value of each attribute
    private List<HashSet<String>> attribute_values = new ArrayList<>();

    // this field holds all the possible value of class labels
    private HashSet<String> distinct_labels = new HashSet<>();

    // this field holds all data for     count(Xi = x, Y=y). Eg: count(Age=40-50, Y= no-recurrence).
    // Each hashmap is 1 attribute
    private List<HashMap<String, Integer>> counts = new ArrayList<>();

    // this field holds all data for    count(Y=y).
    private HashMap<String, Integer> y_count = new HashMap<>();

    // this field holds all data for    total(Xi,Y=y). Eg: total(X=age,Y=no-recurrence)
    private HashMap<String, Integer> total = new HashMap<>();

    // this field contains all the probability of P(X=x|Y=y).
    private HashMap<String, Double> probability_table = new HashMap<>();

    // this field contains all the probability of P(Y=y).
    private HashMap<String, Double> probability_y = new HashMap<>();

    private int class_total = 0;


    public Naive_Bayes(){
        File train_file = new File("src/Part2/breast-cancer-training.csv");

        try{
            Scanner sc = new Scanner(train_file);

            String attributes_string = sc.next();
            String[] attributes = attributes_string.split(",");

            for(int i=2; i<attributes.length; i++){

                attribute_names.add(attributes[i]);
                attribute_values.add(new HashSet<>());

            }

            System.out.println("attributes: " + attribute_names.toString());

            for(int i=0; i<attribute_names.size(); i++){

                counts.add(new HashMap<>());

            }

            while(sc.hasNext()){

                String line = sc.next();
                String[] parts = line.split(",");

                distinct_labels.add(parts[1]);

                for(int i=2; i<parts.length; i++){

                    attribute_values.get(i-2).add(parts[i]);

                }

                train_instances.add(new Instance(parts));

            }

        }catch(Exception e){
            System.out.println("Something is wrong");
        }

        // -------------------------------Checking----------------------------------------------------------------------
        System.out.println("distinct labels: " + distinct_labels.toString());

        for(int i=0; i<attribute_names.size(); i++){

            System.out.println(attribute_names.get(i) + ": " + attribute_values.get(i).toString());

        }
        System.out.println("--------------------------------------------------------------------------------------------");
        // -------------------------------Checking----------------------------------------------------------------------



        // -------------------------------Training the Naive Bayes model------------------------------------------------
        this.trainNB();
        // -------------------------------Training the Naive Bayes model------------------------------------------------



        // -------------------------------Constructing test instances---------------------------------------------------
        File test_file  = new File("src/Part2/breast-cancer-test.csv");

        List<Instance> test_instances = new ArrayList<>();
        try{

            Scanner sc = new Scanner(test_file);

            // skipping the first line of the file (contains attribute names).
            sc.next();

            while(sc.hasNext()){

                String line = sc.next();
                String[] parts = line.split(",");

                test_instances.add(new Instance(parts));

            }

        }catch(Exception e){
            System.out.println("Something is wrong with test file");
        }
        // -------------------------------Constructing test instances---------------------------------------------------


        // -------------------------------Perform predictions-----------------------------------------------------------
        System.out.println("Perform predictions: \n");
        this.predict(test_instances);
        // -------------------------------Perform predictions-----------------------------------------------------------

    }

    public void trainNB(){

        this.initialiseCount();
        this.updateCount();
        this.updateTotal();
        this.calculateProbability();

    }

    public void initialiseCount(){

        // Initialising count(y) = 1.
        for(String label: distinct_labels){

            y_count.put(label, 1);

        }

        // Initialising count(Xi, xi, y) variables.
        for(int i=0; i<attribute_values.size(); i++){

            String attribute_name = attribute_names.get(i);

            // distinct value of each attribute column.
            HashSet<String> Xi = attribute_values.get(i);

            // initialise count(Xi, xi, y) = 1
            HashMap<String, Integer> record = counts.get(i);

            for(String value: Xi){

                for(String label: distinct_labels){

                    String key =  attribute_name + "=" + value + ",class=" + label;
                    record.put(key,1);

                }

            }

        }

    }

    public void updateCount(){

        for(Instance instance: train_instances){

            // update count(y).
            String label = instance.getLabel();
            y_count.put(label, y_count.get(label)+1);

            // update count(Xi, xi, y).
            List<String> values = instance.getValues();

            for(int i=0; i<values.size(); i++){

                String attribute_name = attribute_names.get(i);
                HashMap<String, Integer> current_count = counts.get(i);
                String key = attribute_name+ "=" + values.get(i) + ",class=" + label;

                current_count.put(key, current_count.get(key)+1);

            }

        }
//        System.out.println("Count of (Xi=x, Y=y) \n");
//        for(HashMap<String, Integer> hm: counts){
//            for(Map.Entry<String, Integer> entry: hm.entrySet()){
//                System.out.println(entry.toString());
//            }
//
//            System.out.println("\n");
//        }

        System.out.println("--------------------------------------------------------------------------------------------");

    }

    public void updateTotal(){

        for(String label: distinct_labels){

            class_total = class_total + y_count.get(label);

            // Iterating through all attributes to calculate total(Xi,y)
            for(int i=0; i<attribute_names.size(); i++){

                String attribute = attribute_names.get(i);
                HashMap<String, Integer> hm = counts.get(i);

                int count = 0;

                for(Map.Entry<String, Integer> entry: hm.entrySet()){

                    String[] parts = entry.getKey().split(",");

                    if(parts[1].equals("class=" + label)){

                        count = count + entry.getValue();

                    }

                }

                // key is of form (Xi,y)
                String key = attribute + ",class=" + label;
                total.put(key, count);

            }

        }

        //------------------This code is only for checking----------------------
//        for(Map.Entry<String, Integer> entry: total.entrySet()){
//            System.out.println(entry.toString());
//        }
//
//        System.out.println("\n");
//
//        for(Map.Entry<String, Integer> entry: y_count.entrySet()){
//            System.out.println(entry.toString());
//        }
        //------------------This code is only for checking----------------------

//        System.out.println("class total: " + class_total);
    }

    public void calculateProbability(){

        for(String label: distinct_labels){

            double y_probability = (double) y_count.get(label) / (double) class_total;
            probability_y.put(label, y_probability);

            for(int i=0; i<attribute_names.size(); i++){

                String attribute_name = attribute_names.get(i);

                for(String xi: attribute_values.get(i)){

                    String count_key = attribute_name + "=" + xi + ",class=" + label;
                    String total_key = attribute_name + ",class=" + label;
                    String prob_key  = attribute_name + "=" + xi + "|class=" + label;


                    int count = counts.get(i).get(count_key);

                    int total2 = total.get(total_key);

                    double probability = (double) count / (double) total2;

                    probability_table.put(prob_key, probability);

                }

            }

        }
//        System.out.println("-----------------------------------------------------------------------------------------");
//        System.out.println("Probability of y");
//        for(Map.Entry<String, Double> entry: probability_y.entrySet()){
//            System.out.println(entry.toString());
//        }
//
//        System.out.println("\n");
//
//        System.out.println("Conditional probability");
//        for(Map.Entry<String, Double> entry: probability_table.entrySet()){
//            System.out.println(entry.toString());
//        }
//
//        System.out.println("-----------------------------------------------------------------------------------------");

    }


    public double calculateClassScore(Instance instance, String label){
        double score = probability_y.get(label);

        List<String> values = instance.getValues();

        for(int i=0; i<values.size(); i++){

            String attribute_name  = attribute_names.get(i);
            String attribute_value = values.get(i);

            String probability_key = attribute_name + "=" + attribute_value + "|class=" + label;

            double probability = probability_table.get(probability_key);

            score = score * probability;

        }
        return score;
    }

    public void predict(List<Instance> test_instances){
        List<String> test_label = new ArrayList<>();
        List<String> predicted_label = new ArrayList<>();

        for(Instance instance: test_instances){
            test_label.add(instance.getLabel());
        }

        for(Instance instance: test_instances){

            double final_score = 0.0;
            String final_label = "";

            for(String label: distinct_labels){

                double score = calculateClassScore(instance, label);

                if(score > final_score){

                    final_score = score;
                    final_label = label;

                }

            }

            predicted_label.add(final_label);
        }

        System.out.println(test_label.toString());
        System.out.println(predicted_label.toString());

        int correct_prediction = 0;
        for(int i=0; i<test_label.size(); i++){

            if(test_label.get(i).equals(predicted_label.get(i))){
                correct_prediction++;
            }

        }

        double accuracy = (double)correct_prediction / (double) test_label.size();
        System.out.println("Accuracy: " + accuracy + " with " + correct_prediction + "/" + test_instances.size()
                            + " correct predictions.");


    }


    public static void main(String[] args){
        new Naive_Bayes();
    }
}
