import java.io.*;
import java.util.*;
//import java.lang.instrument.Instrumentation;

public class Program {
    public static Rainbow table = new Rainbow();

    public static void main(String[] args) {
        System.out.println("\n**** TABLES GENERATED ****");
        table.writeToFile("RAINBOW_TABLE.ser");
        int success = 0;
        int rejects = 0;
        long time1, time2;
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
