import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.security.SecureRandom;

class rd {
   public static final SecureRandom rand = new SecureRandom();
   public static final double random() {
      return rand.nextDouble();
   }
}

public class takeyourage {
   public static void main(String args[]) {
      // Goal:
      // make a neural network that learns to do nothing
      
      // cross your fingers everyone
      Generation g;
      Generation ng = new Generation();
      double bestFitness = 256;
      
      long currentTime = System.currentTimeMillis();
      long iterations  = 0;
      while (bestFitness > Hyper.TARGET_FITNESS) {
         g = ng;
         ng = new Generation(g);
         bestFitness = ng.nets.get(0).fitness;
         iterations++;
         if (-currentTime + System.currentTimeMillis() >= Hyper.INFO_INTERVAL*1000) {
            System.out.println("best = "+bestFitness);
            System.out.println("iter = "+iterations+"\n");
            currentTime = System.currentTimeMillis();
         }
      }
      
      // get best network
      Net n = ng.nets.get(0).net;
      
      while (true) {
         System.out.println("Give me your age: ");
         Scanner s = new Scanner(System.in);
         int age = (int)s.nextDouble();
         String binaryAge = Integer.toBinaryString(age);
         while (binaryAge.length() < 8) binaryAge = "0"+binaryAge;
         if (binaryAge.length() > 8) continue;
         //System.out.println(binaryAge);
         double[] input = new double[8];
         for (int i=0; i<8; i++) {
            if (binaryAge.charAt(i) == '0') input[i] = -1;
            else if (binaryAge.charAt(i) == '1') input[i] = 1;
         }
         System.out.println(Hyper.parseCalculate(n.calculate(input)));
      }
   }
   
}



class Generation {
   public ArrayList<FitNet> nets;
   
   public Generation() {
      // generate GENSIZE generations
      nets = new ArrayList<FitNet>(0);
      for (int i=0; i<Hyper.GENSIZE; i++) {
         nets.add(new FitNet(new Net(Hyper.INPUTS, Hyper.OUTPUTS, Hyper.HIDDENS)));
      }
      
      // score nets
      for (FitNet fn: nets) fn.score();
      
      // sort nets
      Collections.sort(nets);
      
   }
   
   public Generation(Generation past) {
      // remove latter (aka higher) half of nets
      nets = past.nets;
      for (int i=1; i<Hyper.GENSIZE/2+1; i++) {
         int index = Hyper.GENSIZE-i;
         nets.remove(index);
      }
      
      // duplicate each net
      ArrayList<FitNet> netsToAdd = new ArrayList<FitNet>(0);
      for (FitNet fn: nets) {
         netsToAdd.add(new FitNet(new Net(fn.net)));
      }
      
      nets.addAll(netsToAdd);
      
      // do "geneflow"
      for (int i=0; i<Hyper.GENEFLOW_ERS; i++) {
         if ((int)(rd.random()*Hyper.GENEFLOW_CHANCE)+1 != Hyper.GENEFLOW_CHANCE) continue;
         nets.set((int)(rd.random()*nets.size()), new FitNet(new Net(Hyper.INPUTS, Hyper.OUTPUTS, Hyper.HIDDENS)));
      }
      
      // score nets
      for (FitNet fn: nets) fn.score();
      
      // sort nets
      Collections.sort(nets);
   }
   
   public String toString() {
      String output = "";
      for (FitNet fn: nets) output += " "+fn;
      return output;
   }
   
}



class FitNet implements Comparable {
   Net net;
   double fitness;
   boolean scored;
   
   public FitNet(Net inputNet) {
      scored = false;
      net = inputNet;
      
   }
   
   public void score() {
      if (scored) 
         return;
      
      // generate (TEST_NUM) test input sets
      double[][] tests = new double[Hyper.TEST_NUM][Hyper.INPUTS];
      for (int i=0; i<tests.length; i++) {
               
         int testnumber = (int)(((double)Hyper.TEST_NUM-i)/Hyper.TEST_NUM*100);
         
         String binaryAge = Integer.toBinaryString(testnumber);
         while (binaryAge.length() < 8) binaryAge = "0"+binaryAge;
         if (binaryAge.length() > 8) continue;
         double[] input = new double[8];
         for (int k=0; k<8; k++) {
            if (binaryAge.charAt(k) == '0') input[k] = -1;
            else if (binaryAge.charAt(k) == '1') input[k] = 1;
         }
         
         tests[i] = input;
      }
      
      // override
      //double[][] tests = {{-1.0}, {-0.5}, {0.0}, {0.5}, {1.0}};
      
      // calculate each test
      for (double[] input: tests) {
         int inputi  = Hyper.parseCalculate(input);
         int output = Hyper.parseCalculate(net.calculate(input));
         fitness += Math.abs(inputi-output);
      }
            
   }
   
   @Override
   public int compareTo(Object o) {
      if (fitness < ((FitNet)o).fitness) 
         return -1;
      if (fitness > ((FitNet)o).fitness) 
         return  1;
      return 0;
   }
   
   public String toString() {
      return ""+Math.round(fitness * 100.0) / 100.0;  
   }
}



class Net {
   ArrayList<Connection> cons;
   ArrayList<Node> nodes;
   int inputs;
   int outputs;
   int hiddens;
   
   public Net(int i, int o, int h) {
      cons = new ArrayList<Connection>(0);
   
      inputs = i;
      outputs = o;
      hiddens = h;
      
      initializeNodes();
   
      generateRandomCons();
      
   }
   
   // mutationater
   public Net(Net n) {
      inputs = n.inputs;
      outputs = n.outputs;
      hiddens = n.hiddens;   
      
      initializeNodes();
      
      cons = new ArrayList<Connection>(0);
      for (Connection c: n.cons) {
         cons.add(new Connection(c.input, c.output, c.weight, c.bias));
      }
      
      // mutate connections
      for (Connection c: cons) {
         c.mutate(nodes.size());
      }
      // add or remove based on randomness
      if ((int)(rd.random()*Hyper.SIZECHANCE)+1 == Hyper.SIZECHANCE) {
         if (rd.random() > 0.5) {
            Connection cToAdd = new Connection(nodes.size());
            boolean notADuplicate = true;
            for (Connection co: cons) {
               if (cToAdd.input == co.input && cToAdd.output == co.output) {
                  notADuplicate = false;
                  break;
               }
            }
            if (notADuplicate) cons.add(cToAdd);
         }
         else {
            cons.remove(rd.random()*nodes.size());
         }
      }
      
   }
   
   public static final double sigmoid(double x) {
      return (2/(1+Math.exp(-x)))-1;
   }
   
   public double[] calculate(double[] invals) {
      if (invals.length != inputs) 
         return null;
      double[] output = new double[outputs];
      
      // reset all nodes
      for (Node n:nodes) {
         n.value = 0;
         n.calculated = false;
      }
      
      // set inputs
      int i = 0;
      for (double d: invals) {
         nodes.get(i).calculated = true;
         nodes.get(i).value = d;
         i++;
      }
      
      // sort nodes by input value
      Collections.sort(cons);
      //System.out.println("CONNNS = "+cons);
      
      // calculate values
      for (Connection c: cons) {
         
         if (nodes.get(c.output).input) 
            continue;
         
         Node n = nodes.get(c.input);
         double v = sigmoid(n.value);
         nodes.get(c.output).value += c.weight*(v)+c.bias;
         
      }
      
      // get the outputs
      int indexx = 0;
      for (Node n: nodes) {
         if (!n.output) 
            continue;
         output[indexx] = sigmoid(n.value);
         indexx++;
      }
      
      return output;
      
     
   }
   public double[] idiotcalculate(double[] invals) {
      if (invals.length != inputs) 
         return null;
      double[] output = new double[outputs];
      
      // reset all nodes
      for (Node n:nodes) {
         n.value = 0;
         n.calculated = false;
      }
      
      // set inputs
      int i = 0;
      for (double d: invals) {
         nodes.get(i).calculated = true;
         nodes.get(i).value = d;
         i++;
      }
      
      // sort nodes by input value
      Collections.sort(cons);
      //System.out.println("CONNNS = "+cons);
      
      // calculate values
      for (Connection c: cons) {
         
         if (nodes.get(c.output).input) 
            continue;
         
         Node n = nodes.get(c.input);
         double v = sigmoid(n.value);
         nodes.get(c.output).value += c.weight*(v)+c.bias;
         
      }
      
      // get the outputs
      int indexx = 0;
      for (Node n: nodes) {
         if (!n.output) 
            continue;
         output[indexx] = n.value;
         indexx++;
      }
      
      return output;
      
     
   }

   private void initializeNodes() {
      nodes = new ArrayList<Node>(0);
      // generate input nodes
      int i;
      for (i=0; i<inputs; i++) {
         nodes.add(new Node(true, false, i));
      }      
      // generate hidden nodes
      for (i=inputs; i<inputs+hiddens; i++) {
         nodes.add(new Node(false, false, i));
      }
      // generate output nodes
      for (i=inputs+hiddens; i<inputs+outputs+hiddens; i++) {
         nodes.add(new Node(false, true, i));
      }
   }
   
   private void generateRandomCons() {
      int conCount = (int)(rd.random()*nodes.size()*nodes.size()) + (nodes.size()*nodes.size())/2;
      while (conCount > 0) {
         // make a random connection
         Connection c = new Connection(nodes.size());
         
         // check to make sure it isnt a duplicate
         boolean notADuplicate = true;
         for (Connection co: cons) {
            if (c.input == co.input && c.output == co.output) {
               notADuplicate = false;
               break;
            }
         }
         
         if (notADuplicate) {
            cons.add(c);
         }
         
         conCount--;
      }
      
   }
   
   private boolean nodesUnfinished() {
      for (Node n: nodes) {
         if (!n.calculated && !n.output) 
            return true;
      }
      return false;
   }
   
   public String toString() {
      String output = "connections:";
      for (Connection c: cons) {
         output += "\n"+c;
      }
      output += "\n\nnodes:";
      for (Node n: nodes) {
         output += "\n"+n;
      }
      return output;
      
   }
   
}



class Connection implements Comparable {
   int input;
   int output;
   double weight; // from -2 to 2
   double bias;   // from -2 to 2
   
   public Connection(int input, int output, double weight, double bias) {
      this.input = input;
      this.output = output;
      this.weight = weight;
      this.bias = bias;
   }
   
   public Connection(int nodesLen) {
      int i = (int)(rd.random()*(nodesLen-1));
      int o = -1;
      double w = rd.random()*4-2;
      double b = rd.random()*4-2;
      
      while (o<=i) {
         o = (int)(rd.random()*nodesLen);
      }
      
      this.input = i;
      this.output = o;
      this.weight = w;
      this.bias = b;
   }
   
   public int compareTo(Object o) {
      Connection c = (Connection)o;
      return input-c.input;
   }
   
   public String toString() {
      return "i"+input+" o"+output+" w"+weight+" b"+bias;
   }
   
   public void mutate(int nodesLen) {
      // mutate values
      if (((int)(rd.random()*Hyper.MUTATECHANCE)+1 == Hyper.MUTATECHANCE)) {
         do {
            output = (int)(rd.random()*nodesLen);
         } while (output<=input);
      }
      
      if (((int)(rd.random()*Hyper.MUTATECHANCE)+1 == Hyper.MUTATECHANCE)) {
         do {
            input = (int)(rd.random()*nodesLen);
         } while (input>=output);        
      }
      
      if (((int)(rd.random()*Hyper.MUTATECHANCE)+1 == Hyper.MUTATECHANCE)) {
         bias += rd.random();
         bias = (4/(1+Math.exp(-weight)))-2;
      }
      
      if (((int)(rd.random()*Hyper.MUTATECHANCE)+1 == Hyper.MUTATECHANCE)) {
         weight += rd.random();
         weight = (4/(1+Math.exp(-weight)))-2;
      }
   }
   
}



class Node {
   boolean input;
   boolean output;
   boolean calculated;   
   
   // a number from 1 to negative 1 (due to sig)
   
   // during calculation, this value will be large
   // due to no sigmoid
   double value = 0;
   
   public Node (boolean iinput, boolean ooutput, int iid) {
   
      input = iinput;
      output = ooutput;
      
      if (input||output) calculated = true;
   
   }    
   
}