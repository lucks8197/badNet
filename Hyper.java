public class Hyper {
   public static final int TEST_NUM = 50;
   public static final int INPUTS   = 8;
   public static final int OUTPUTS  = 8;
   public static final int HIDDENS  = 15;
   public static final int GENSIZE  = 100; // must be even
   public static final int GENEFLOW_ERS = 2;
   public static final int GENEFLOW_CHANCE = 1000;
   public static final int MUTATECHANCE = 1000;
   public static final int SIZECHANCE   = 10000;
   
   public static final double INFO_INTERVAL = 1; // in seconds
   public static final double TARGET_FITNESS = 500;
   
   public static final int parseCalculate(double[] output) {
      String binaryString = "";
      for (double d: output) {
         if (d>0) binaryString += "1";
         else binaryString += "0";
      }
      return Integer.parseInt(binaryString,2);
   }
}