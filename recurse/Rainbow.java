import java.math.BigInteger;
import java.security.*;
import java.util.*;
import java.io.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Rainbow {
    private char[] hexSet = "0123456789ABCDEF".toCharArray();
    private HashMap<String, byte[]> table; // <word, plaintext> ORIGINAL
    public byte[][] testInputs;
    private MessageDigest SHA; // 160 bits
    private int chainLen = 300;
    private int rows = 30000;

    public Rainbow() {
        table = new HashMap<String, byte[]>(); // ORIGINAL
        testInputs = new byte[rows][20];
        try {
            SHA = MessageDigest.getInstance("SHA1");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        generate();
    }

    //---- HASHING ----------------------------------------
    public byte[] hash(byte[] plaintext) {
        byte digest[] = new byte[20];
        try {
            digest = SHA.digest(plaintext);
            SHA.reset();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return digest;
    }

    //---- REDUCE FUNCTION --------------------------------
    public byte[] reduce(byte[] digest, int len) {
        byte last_byte = (byte) len;
        byte[] word = new byte[3];
        for (int i = 0; i < word.length; i++) {
            word[i] = (byte) (digest[(len + i) % 20] + last_byte);
            //word[i] = (byte) (digest[i] + last_byte);
        }
        return word;
    }

    //---- GENERATE TABLE ---------------------------------
    public void generate() {
        long time1, time2;
        byte[] plaintext, word;
        String key;
        int success = 0, collisions = 0;
        System.out.println("\nGenerating table...");
        time1 = System.currentTimeMillis();

        //while (table.size() < rows) { // FROM CRYPTO FILE
        for (int i = 0; i < rows; i++) { // ORIGINAL
            plaintext = intToBytes(i);
            word = generateChain(plaintext, i);
        //    System.out.println("\n");
            key = bytesToHex(word);
            if (!table.containsKey(key)) {
                table.put(key, plaintext);
                success++;
//                System.out.println(key + " | " + bytesToHex(plaintext));
            } else {
                //System.out.println("Collision @ key: " + key + " word: " + bytesToHex(plaintext));
                collisions++;
            }
        }

        time2 = System.currentTimeMillis();
        System.out.println("Table generated in " + ((time2 - time1)/1000.0)  + " seconds");
        System.out.println("Rows: " + rows);
        System.out.println("ChainLen: " + chainLen);
        System.out.println("Number of successes: " + success + " of " + rows);
        System.out.println("Number of collisions: " + collisions + " of " + rows);
    }
    
    public byte[] generateChain(byte[] plaintext, int ti) {
    //public String generateChain(byte[] plaintext) { // ORIGINAL
        byte[] digest = new byte[20];
        byte[] word = plaintext;
        Random r = new Random();
        int di = r.nextInt(chainLen);
        //System.out.println("Chain for " + bytesToHex(plaintext));
        for (int i = 0; i < chainLen; i++) {
            digest = hash(word);
            word = reduce(digest, i);
            if (i == di) {
                testInputs[ti] = digest;
            }
            //System.out.println("Digest (" + i + ") \t" + bytesToHex(digest));
            //System.out.println("Word (" + i + ")\t" + bytesToHex(word));
        }
        return word;
        // return digestToKey(digest); // ORIGINAL
    }

    //---- INVERTING --------------------------------------
    public byte[] invert(byte[] digest_to_match) {
        byte[] result = new byte[3];
        String key = "";// = digestToKey(digest_to_match);
//        System.out.println("Attempting to invert " + bytesToHex(digest_to_match));
        for (int i = chainLen - 1; i >= 0; i--) {
            key = invertHashReduce(digest_to_match, i);
//            System.out.println("Inverting... key: " + key);
            if (table.containsKey(key)) {
/*                System.out.println("\nKey exists!");
                System.out.println("Inverting... key: " + key);
                System.out.println("Word: " + bytesToHex(table.get(key))); */
                result = invertChain(digest_to_match, table.get(key));
                if (result != null) {
 //                   System.out.println("MATCH!!! " + bytesToHex(result));
                    return result;
                }
            }
        }
//        System.out.println("");
        return null;
    }

    public String invertHashReduce(byte[] digest, int start) {
        byte[] word = new byte[3];
        for (int i = start; i < chainLen; i++) {
            word = reduce(digest, i);
            digest = hash(word);
        }
        return bytesToHex(word);
    }

    public byte[] invertChain(byte[] digest_to_match, byte[] word) {
        byte[] digest;
//        System.out.println("INVERTING CHAIN");
        for (int i = 0; i < chainLen; i++) {
            digest = hash(word);
//            System.out.println("(Digest: " + bytesToHex(digest) + ")");
//            System.out.println("(Digest to match: " + bytesToHex(digest_to_match) + ")");
//            System.out.println("(DO THEY MATCH: " + digest.equals(digest_to_match) + ")");
            if (Arrays.equals(digest, digest_to_match)) {
                return word;
            }
            word = reduce(digest, i);
    //        System.out.println("Digest (" + i + ") \t" + bytesToHex(digest));
     //       System.out.println("Word (" + i + ")\t" + bytesToHex(word));
        }
      //  System.out.println("END INVERTING CHAIN\n");
        return null;
    }

                /*System.out.println("Key match: " + key);
                System.out.println("D: " + bytesToHex(digest));
                System.out.println("DTM: " + bytesToHex(digest_to_match)); */
                //word = invertChain(digest, key); // ORIGINAL
        /*System.out.println("\nFIRST TIME");
        System.out.println("KEY: " + key);
        System.out.println("D: " + bytesToHex(digest));
        System.out.println("DTM: " + bytesToHex(digest_to_match));
        System.out.println("END FIRST TIME\n");*/
    /*public byte[] invertOLD(byte[] digest_to_match) {
        byte[] word_to_match, word;
        for (int len = chainLen - 1; len >= 0; len--) {
        //for (int len = 0; len < chainLen; len++) {
            word_to_match = reduce(digest_to_match, len);

            if (table.containsKey(word_to_match)) {
                word = invertChainOLD(word_to_match, digest_to_match);
                if (word != null) {
                    System.out.println("MATCH THEREs A MATCH");
                    return word;
                }
            }
            digest_to_match = hash(word_to_match); // MAYBE *****
        }
        return null;
    }

    public byte[] invert2OLD(byte[] digest_to_match) {
        byte[] word_to_match = new byte[3];
        byte[] plaintext, digest;
        for (int i = chainLen - 1; i >= 0; i--) {
            digest = digest_to_match;
            for (int j = i; j < chainLen; j++) {
                word_to_match = reduce(digest, j);
                digest = hash(word_to_match);
            }

            if (table.containsKey(word_to_match)) {
                plaintext = invertChainOLD(word_to_match, digest_to_match);
                if (plaintext != null) {
                    return plaintext;
                }
            }
            //digest_to_match = hash(word_to_match); // MAYBE *****
        }
        return null;
    }
    
    public byte[] invertChainOLD(byte[] word_to_match, byte[] digest_to_match) {
        byte[] word = table.get(word_to_match);
        byte[] digest = hash(word);
        for (int len = 0; len < chainLen; len++) {
            if (digest.equals(digest_to_match)) {
                return word;
            }
            digest = hash(word);
            word = reduce(digest, len);
        }
        return null;
    }*/

    //---- HELPER FUNCTIONS -------------------------------
    public String digestToKey(byte[] digest) {
        byte[] key = new byte[3];
        for (int i = 0; i < key.length; i++) {
            key[i] = digest[i];
        }
        return bytesToHex(key);
    }

    public byte[] hexToBytes(String hexString) {
        HexBinaryAdapter adapter = new HexBinaryAdapter();
        byte[] bytes = adapter.unmarshal(hexString);
        return bytes;
    }

    public String bytesToHex(byte[] bytes) {
        HexBinaryAdapter adapter = new HexBinaryAdapter();
        String str = adapter.marshal(bytes);
        return str;
    }

    public byte[] intToBytes(int n) {
        byte plaintext[] = new byte[3];
        plaintext[0] = (byte) ((n >> 16) & 0xFF);
        plaintext[1] = (byte) ((n >> 8) & 0xFF);
        plaintext[2] = (byte) n;
        return plaintext;
    }

    public int bytesToInt(byte[] bytes) {
        return java.nio.ByteBuffer.wrap(bytes).getInt();
    }

    public void writeToFile(String file) {
        ObjectOutputStream O;
        try {
            O = new ObjectOutputStream(new FileOutputStream(file));
            O.writeObject(table);
            O.close();
            System.out.println("Rainbow table has been written to file " + file);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}

/* EOF */
