import java.math.BigInteger;
import java.security.*;
import java.util.*;
import java.io.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Rainbow {
    private char[] hexSet = "0123456789ABCDEF".toCharArray();
    private HashMap<byte[], byte[]> table; // <Hash, word> (or vice versa??) Rainbow table
    private MessageDigest SHA; // 160 bits
    private int chainLen = 180;
    private int rows = 1000;

    public Rainbow() {
        table = new HashMap<byte[], byte[]>();
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
        byte[] reduction = new byte[3];
        for (int i = 0; i < reduction.length; i++) {
            reduction[i] = (byte) (digest[(len + i) % 20] + last_byte);
        }
        return reduction;
    }

    //---- GENERATE TABLE ---------------------------------
    public void generate() {
        byte[] plaintext, reduction;
        long time1, time2;
        System.out.println("\nGenerating table...");
        time1 = System.currentTimeMillis();

        for (int i = 0; i < rows; i++) {
            plaintext = intToBytes(i);
            reduction = generateChain(plaintext);
            table.put(reduction, plaintext);
        }

        time2 = System.currentTimeMillis();
        System.out.println("Table generated in " + ((time2 - time1)/1000.0)  + " seconds");
    }

    public byte[] generateChain(byte[] plaintext) {
        byte[] digest = new byte[20];
        byte[] reduction = plaintext;
        for (int len = 0; len < chainLen; len++) {
            digest = hash(reduction);
            reduction = reduce(digest, len);
        }
        return digest;
    }

    //---- INVERTING 2 ------------------------------------
    public byte[] invert(byte[] digest_to_match) {
        byte[] reduction_to_match, plaintext;
        for (int len = chainLen - 1; len >= 0; len--) {
            reduction_to_match = reduce(digest_to_match, len);
            if (table.containsKey(reduction_to_match)) {
                plaintext = invertChain(reduction_to_match, digest_to_match);
                if (plaintext != null) {
                    return plaintext;
                }
            }
            //digest_to_match = hash(reduction_to_match); // MAYBE *****
        }
        return null;
    }
    
    public byte[] invertChain(byte[] reduction_to_match, byte[] digest_to_match) {
        byte[] digest;
        byte[] plaintext = table.get(reduction_to_match);
        for (int len = 0; len < chainLen; len++) {
            digest = hash(plaintext);
            if (digest.equals(digest_to_match)) {
                return plaintext;
            }
            plaintext = reduce(digest, len);
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
        return java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
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
