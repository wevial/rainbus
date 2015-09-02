import java.math.BigInteger;
import java.security.*;
import java.util.*;
import java.io.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Rainbow {
    private char[] hexSet = "0123456789ABCDEF".toCharArray();
    private HashMap<String, byte[]> table; // <Hash, word> (or vice versa??) Rainbow table
    //private HashMap<byte[], byte[]> table; // <Hash, word> (or vice versa??) Rainbow table
    private MessageDigest SHA; // 160 bits
    private int chainLen = 200;
    private int rows = 28000;

    public Rainbow() {
        table = new HashMap<String, byte[]>();
        //table = new HashMap<byte[], byte[]>();
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
//        byte last_byte = (byte) len;
        byte[] word = new byte[3];
        for (int i = 0; i < word.length; i++) {
//            word[i] = (byte) (digest[(len + i) % 20] + last_byte);
            word[i] = (byte) (digest[i] + len);
        }
        return word;
    }

    //---- GENERATE TABLE ---------------------------------
    public void generate() {
        byte[] plaintext, digest;
        long time1, time2;
        int success = 0;
        System.out.println("\nGenerating table...");
        time1 = System.currentTimeMillis();

        for (int i = 0; i < rows; i++) {
            plaintext = intToBytes(i);
            digest = generateChain(plaintext);
            String key = bytesToHex(digest);
            if (!table.containsKey(key)) {
                table.put(key, plaintext);
                success++;
//                System.out.println(bytesToHex(digest) + " | " + bytesToHex(plaintext));
            } else {
                //System.out.println("Collision @ key: " + key + " word: " + bytesToHex(plaintext));
            }
        }

        time2 = System.currentTimeMillis();
        System.out.println("Table generated in " + ((time2 - time1)/1000.0)  + " seconds");
        System.out.println("Number of successes: " + success + " of " + rows);
    }

    public byte[] generateChain(byte[] plaintext) {
        byte[] digest = new byte[20];
        byte[] word = plaintext;
        for (int len = 0; len < chainLen; len++) {
            digest = hash(word);
            word = reduce(digest, len);
        }
        byte[] key = new byte[3];
        for (int i = 0; i < key.length; i++) {
            key[i] = digest[i];
        }
        return key;
        //return digest;
    }

    //---- INVERTING --------------------------------------
    //public byte[] invert3(byte[] digest_to_match) {
    //    byte[] word_to_match, word;
    //    byte[] digest = digest_to_match;
    //    for (int i = 0; i < chainLen; i++) {
    //        if (table.containsKey(digest)) {
    //            word = invertChain(digest);
    //        }
    //    }
    //}

    public byte[] invert(byte[] digest_to_match) {
        byte[] word_to_match, word;
        for (int len = chainLen - 1; len >= 0; len--) {
        //for (int len = 0; len < chainLen; len++) {
            word_to_match = reduce(digest_to_match, len);

            if (table.containsKey(word_to_match)) {
                word = invertChain(word_to_match, digest_to_match);
                if (word != null) {
                    System.out.println("MATCH THEREs A MATCH");
                    return word;
                }
            }
            digest_to_match = hash(word_to_match); // MAYBE *****
        }
        return null;
    }

    public byte[] invert2(byte[] digest_to_match) {
        byte[] word_to_match = new byte[3];
        byte[] plaintext, digest;
        for (int i = chainLen - 1; i >= 0; i--) {
            digest = digest_to_match;
            for (int j = i; j < chainLen; j++) {
                word_to_match = reduce(digest, j);
                digest = hash(word_to_match);
            }

            if (table.containsKey(word_to_match)) {
                plaintext = invertChain(word_to_match, digest_to_match);
                if (plaintext != null) {
                    return plaintext;
                }
            }
            //digest_to_match = hash(word_to_match); // MAYBE *****
        }
        return null;
    }
    
    public byte[] invertChain(byte[] word_to_match, byte[] digest_to_match) {
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
    }

    //---- HELPER FUNCTIONS -------------------------------
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
