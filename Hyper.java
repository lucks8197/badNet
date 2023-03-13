public class Hyper {
   public static final int TEST_NUM = 50;
   public static final int INPUTS   = 8;
   public static final int HIDDENS  = 0;
   public static final int OUTPUTS  = 8;
   
   public static final boolean LAYERED = false;
   public static final int[] SIGNATURE = {8,10,8};

   public static final int GENSIZE  = 50; // must be even
   public static final int GENEFLOW_ERS = 0;
   public static final int GENEFLOW_CHANCE = 100000000;
   public static final int MUTATECHANCE = 1000;
   public static final int SIZECHANCE   = 10000;
   
   public static final double INFO_INTERVAL = 3; // in seconds
   public static final double TARGET_FITNESS = 10;
   ;
   
   public static final int parseCalculate(double[] output) {
      String binaryString = "";
      for (double d: output) {
         if (d>0) binaryString += "1";
         else binaryString += "0";
      }
      return Integer.parseInt(binaryString,2);
   }
}