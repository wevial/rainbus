//import java.lang.Math.*;
//import java.security.*;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.io.*;

public class Program {
    public static Rainbow table = new Rainbow();

    public static void main(String[] args) {
        System.out.println("\n*********** T A B L E S  G E N E R A T E D ***********");
        table.writeToFile("RAINBOW_TABLE.ser");

        System.out.println("\n*********** T E S T S **********");
        int numWords = 1000;
        byte[][] testInputs = generateWords(numWords);

        System.out.println("\n* Inverting with 28k row table *");
//        invertWords(table.testInputs);
        invertWords(testInputs);

        System.out.println("\n*********** F I L E ************");
        fromFile();
        System.out.println("\n********************************");
    }

    public static void invertWords(byte[][] words) {
        byte[] digest, result;
        long time1, time2;
        int success = 0;
        time1 = System.currentTimeMillis();
        for (int i = 0; i < words.length; i++) {
            digest = words[i];
            result = table.invert(digest);
            if (result != null) {
                success++;
            }
        } 
        time2 = System.currentTimeMillis();
        System.out.println("Total time: " + ((time2-time1)/1000.0) + " seconds.");
        System.out.println("Success rate: " + success + " / " + words.length);
        System.out.println("Rejection rate: " + (words.length-success) + " / " + words.length);
        System.out.println("\n*******************************");
    }

    public static byte[][] generateWords(int n) {
        byte[][] words = new byte[n][20];
        for (int i = 0; i < words.length; i++) {
            words[i] = table.hash(table.intToBytes(i));
        }
        return words;
    }

    public static void fromFile() {
        String f = "SAMPLE_INPUT.data";
        try {
            BufferedReader B = new BufferedReader(new FileReader(f));
            FileWriter W = new FileWriter("output.data");
            String line;
            int success = 0;
            int rejects = 0;
            int l = 0;
            long start, end, time1, time2, t = 0;
            double avg = 0.0;

//            String digests[] = new String[1000];
            byte[][] digests = new byte[1000][20];
            byte[][] words = new byte[1000][3];
            // Read file and save digests to array
            while ((line = B.readLine()) != null) {
                String hex = line.substring(2, 10) + line.substring(12, 20);
                hex += line.substring(22, 30) + line.substring(32, 40);
                hex += line.substring(42, 50);
                hex = hex.replaceAll("\\s", "0");
                digests[l] = table.hexToBytes(hex);
                l++;
            }
            W.write("S T A R T\n");
            W.write("READ DONE\n");

            start = System.currentTimeMillis();
            byte[] d;
            byte[] result;
            for (int i = 0; i < words.length; i++) {
                d = digests[i];
                time1 = System.currentTimeMillis();
                result = table.invert(d);
                time2 = System.currentTimeMillis();
                words[i] = result;
                if (result != null) {
                    success++;
                }
                t = t + (time2-time1);
            }
            end = System.currentTimeMillis();
            avg = ((double) t / words.length)/1000.0;
            for (int i = 0; i < words.length; i++) {
                if (words[i] == null) {
                    W.write("\n       0");
                } else {
                    W.write("\n  " + table.bytesToHex(words[i]));
                }
            }
            W.write("\n\nThe total number of words found is: " + success + "\n");
            W.close();
            B.close();

            System.out.println("Total time: \t" + ((end - start)/1000.0));
            System.out.println("success: \t" + success);
            double s = ((double) success) / 1000.0;
            System.out.println("Accuracy: \t" + (s * 100.0) + "%\n");
            System.out.println("Avg t: \t" + avg);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    } 
}
