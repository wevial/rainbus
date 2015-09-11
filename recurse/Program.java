//import java.lang.Math.*;
//import java.security.*;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.io.*;

public class Program {
    public static Rainbow table = new Rainbow();

    public static void main(String[] args) {
        int success = 0;
        int rejects = 0;
        long time1, time2;

        System.out.println("\n*********** T A B L E S  G E N E R A T E D ***********");
        //table.writeToFile("RAINBOW_TABLE.ser");

        /*System.out.println("\n*********** F I L E ***********");
        fromFile(); */
        System.out.println("\n*******************************");

        System.out.println("\n*********** T E S T S ***********");

        int numWords = 300;
//        byte[][] words = generateWords(numWords);
        byte[][] words = table.testInputs;

        System.out.println("\n**** Invert with ~28k row table");
        time1 = System.currentTimeMillis();
        byte[] digest, result;
//        for (int i = 0; i < numWords; i++) {
        for (int i = 0; i < words.length; i++) {
            digest = words[i];
//            System.out.println("Trying " + i + " (digest: " + table.bytesToHex(words[i]) + " )");
            //digest = table.hash(words[i]);
            result = table.invert(digest);
            //System.out.println("Trying " + i + " (word: " + table.bytesToHex(words[i]) + " )");
 //           System.out.println("Digest " + table.bytesToHex(digest));
 //           System.out.println("Result " + result + "\n");
            if (result != null) {
                success++;
//                System.out.println("Success! " + result);
            } else {
                rejects++;
            }
        } 
        time2 = System.currentTimeMillis();
        System.out.println("Total time: " + ((time2-time1)/1000.0) + " seconds.");
        System.out.println("Success rate: " + success + " / " + words.length);
        System.out.println("Rejection rate: " + rejects + " / " + words.length);

    }

    public static byte[][] generateWords(int n) {
        byte[][] words = new byte[n][3];
        for (int i = 0; i < words.length; i++) {
            words[i] = table.intToBytes(i);
        }
        return words;
    }

    public static void invertWords(byte[][] words) {
    }

    /*public static void fromFile() {
        String f = "SAMPLE_INPUT.data";
        try {
            BufferedReader B = new BufferedReader(new FileReader(f));
            FileWriter W = new FileWriter("output.data");
            String line;
            int successes = 0;
            int rejects = 0;
            int l = 0;
            long start, end, time1, time2, t = 0;
            double avg = 0.0;

            String digests[] = new String[1000];
            String pts[] = new String[1000];
            while ((line = B.readline()) != null) {
                String hex = line.substring(2, 10) + line.substring(12, 20);
                hex += line.substring(22, 30) + line.substring(32, 40);
                hex += line.substring(22, 50);
                hex = hex.replaceAll("\\s", "0");
                digests[l] = hex;
                l++;
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    } */
}
