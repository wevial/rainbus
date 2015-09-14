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
    private int rows = 28000;

    public Rainbow() {
        table = new HashMap<String, byte[]>();
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
        byte[] len_bytes = intToBytes(len);
        byte[] word = new byte[3];
        for (int i = 0; i < word.length; i++) {
            word[i] = (byte) (digest[(len + i) % 20] + last_byte);
        }
        return word;
    }

    //---- GENERATE TABLE ---------------------------------
    public void generate() {
        long time1, time2;
        byte[] plaintext, word;
        String key;
        Random R = new Random();
        int success = 0, collisions = 0;
        System.out.println("\nGenerating table...");
        time1 = System.currentTimeMillis();

        int i = 0;
        while (table.size() < rows) {
            plaintext = intToBytes(i);
            word = generateChain(plaintext, i);
            key = bytesToHex(word);
            if (!table.containsKey(key)) {
                table.put(key, plaintext);
                success++;
            } else {
                collisions++;
            }
            i++;
        }

        time2 = System.currentTimeMillis();
        System.out.println("Table generated in " + ((time2 - time1)/1000.0)  + " seconds");
        System.out.println("Rows: " + rows);
        System.out.println("ChainLen: " + chainLen);
        System.out.println("Number of success: " + success + " of " + rows);
        System.out.println("Number of collisions: " + collisions + " of " + rows);
    }
    
    public byte[] generateChain(byte[] plaintext, int ti) {
        byte[] digest = new byte[20];
        byte[] word = plaintext;
        Random r = new Random();
        int di = r.nextInt(chainLen);
        for (int i = 0; i < chainLen; i++) {
            digest = hash(word);
            word = reduce(digest, i);
            if (i == di && ti < rows) {
                testInputs[ti] = digest;
            }
        }
        return word;
    }

    //---- INVERTING --------------------------------------
    public byte[] invert(byte[] digest_to_match) {
        byte[] result = new byte[3];
        String key = "";
        for (int i = chainLen - 1; i >= 0; i--) {
            key = invertHashReduce(digest_to_match, i);
            if (table.containsKey(key)) {
                result = invertChain(digest_to_match, table.get(key));
                if (result != null) {
                    return result;
                }
            }
        }
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
        for (int i = 0; i < chainLen; i++) {
            digest = hash(word);
            if (Arrays.equals(digest, digest_to_match)) {
                return word;
            }
            word = reduce(digest, i);
        }
        return null;
    }

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
