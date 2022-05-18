package Part2;

import javax.transaction.xa.Xid;
import java.io.File;
import java.sql.SQLOutput;
import java.util.*;

public class Naive_Bayes {
    private List<Instance> train_instances = new ArrayList<>();

    // this field contains all the attribute names.
    private List<String> attribute_names = new ArrayList<>();

    // this field holds all the possible value of each attribute
    private List<List<String>> attribute_values = new ArrayList<>();

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

    private boolean report_probs = false;


    public Naive_Bayes(String train_file_name, String test_file_name, String report){

        if(report.equals("yes")){
            this.report_probs = true;
        }

        File train_file = new File(train_file_name);

        try{
            Scanner sc = new Scanner(train_file);

            String attributes_string = sc.next();
            String[] attributes = attributes_string.split(",");

            for(int i=2; i<attributes.length; i++){

                attribute_names.add(attributes[i]);

            }

            System.out.println("attributes: " + attribute_names.toString());

            for(int i=0; i<attribute_names.size(); i++){

                counts.add(new HashMap<>());

            }

            ArrayList<String> age_values = new ArrayList<>(Arrays.asList("10-19", "20-29", "30-39", "40-49", "50-59",
                                                                        "60-69", "70-79", "80-89", "90-99"));

            ArrayList<String> menopause_values = new ArrayList<>(Arrays.asList("lt40", "ge40", "premeno"));

            ArrayList<String> tumor_size_values = new ArrayList<>(Arrays.asList("0-4", "5-9", "10-14", "15-19","20-24",
                                                        "25-29","30-34", "35-39", "40-44", "45-49", "50-54", "55-59"));

            ArrayList<String> inv_nodes_values = new ArrayList<>(Arrays.asList( "0-2", "3-5", "6-8", "9-11", "12-14",
                    "15-17", "18-20", "21-23", "24-26", "27-29", "30-32","33-35", "36-39"));

            ArrayList<String> node_caps_values = new ArrayList<>(Arrays.asList("yes", "no"));

            ArrayList<String> deg_malig_values = new ArrayList<>(Arrays.asList("1", "2", "3"));

            ArrayList<String> breast_values = new ArrayList<>(Arrays.asList("left", "right"));

            ArrayList<String> breast_quad_values = new ArrayList<>(Arrays.asList("left_up", "left_low", "right_up",
                                                                                "right_low", "central"));

            ArrayList<String> irradiat_values = new ArrayList<>(Arrays.asList("yes", "no"));

            attribute_values.add(age_values);
            attribute_values.add(menopause_values);
            attribute_values.add(tumor_size_values);
            attribute_values.add(inv_nodes_values);
            attribute_values.add(node_caps_values);
            attribute_values.add(deg_malig_values);
            attribute_values.add(breast_values);
            attribute_values.add(breast_quad_values);
            attribute_values.add(irradiat_values);

            while(sc.hasNext()){

                String line = sc.next();
                String[] parts = line.split(",");

                distinct_labels.add(parts[1]);
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
        File test_file  = new File(test_file_name);

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
            List<String> Xi = attribute_values.get(i);

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

        // this method is to calculate all the conditional probability.
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
        //-------------Uncomment this section if you need to print out probability of Y and conditional probs-----------

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
//        System.out.println("\n");
//
//        System.out.println("-----------------------------------------------------------------------------------------");

        //--------------------------------------------------------------------------------------------------------------
    }

    // this method is used to check all the conditional probabilities and
    public void report(){
        if(report_probs){
            System.out.println("Probability of y");
            for(Map.Entry<String, Double> entry: probability_y.entrySet()){
                System.out.println(entry.toString());
            }

            System.out.println("\n");

            System.out.println("Conditional probability");
            for(Map.Entry<String, Double> entry: probability_table.entrySet()){
                System.out.println(entry.toString());
            }

            System.out.println("\n");
        }
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

        int index=1;

        for(Instance instance: test_instances){

            double final_score = 0.0;
            String final_label = "";

            for(String label: distinct_labels){

                double score = calculateClassScore(instance, label);
                System.out.println("Instance " + index + " " + label + " score: " + score);
                if(score > final_score){

                    final_score = score;
                    final_label = label;

                }

            }
            System.out.println("Final label prediction: " + " " + final_label + " | Correct label: " + instance.getLabel() + "\n");
            predicted_label.add(final_label);
            index++;
        }

        System.out.println(test_label);
        System.out.println(predicted_label);

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
        if(args.length == 2){
            String train_file = args[0];
            String test_file = args[1];
            String report = "no";
            new Naive_Bayes(train_file, test_file, report);
        }

        else if(args.length == 3) {
            String train_file = args[0];
            String test_file = args[1];
            String report = args[2];
            Naive_Bayes nb = new Naive_Bayes(train_file, test_file, report);
            System.out.println("\n");
            nb.report();
        }
    }
}
