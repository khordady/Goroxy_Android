package arteh.world.goroxy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {
    static Cipher cipheren;
    static Cipher cipherde;

    public static void initialize() {
        if (cipheren == null) {
            try {
                SecretKeySpec keySpec = new SecretKeySpec(Config.encryption_key.getBytes(StandardCharsets.UTF_8), "AES");
                cipheren = Cipher.getInstance("AES/ECB/NoPadding");
                cipheren.init(1, keySpec);

                cipherde = Cipher.getInstance("AES/GCM/NoPadding");
                cipherde.init(1, keySpec);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] encryptAES(byte[] message, int length) {
        byte[] finalbuffer = new byte[length + 4];
        try {
            System.arraycopy(intToArray(length), 0, finalbuffer, 0, 4);
            System.arraycopy(message, 0, finalbuffer, 4, length);

            return cipheren.doFinal(finalbuffer);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptAES(byte[] data, int length) {
        try {
            byte[] plainBytes = cipherde.doFinal(data, 0, length);

            byte[] btlength = new byte[4];
            System.arraycopy(plainBytes, 0, btlength, 0, 4);
            int size = ByteBuffer.wrap(btlength).getInt();

            byte[] finalBuffer = new byte[size];
            System.arraycopy(plainBytes, 4, finalBuffer, 0, size);

            return finalBuffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] processToBrowsder(byte[] data, int length) {
        byte[] finalBuffer;
        if (Config.encryption == 0) {
            finalBuffer = new byte[length];
            System.arraycopy(data, 0, finalBuffer, 0, length);
        } else {
            finalBuffer = decryptAES(data, length);
        }

        return finalBuffer;
    }

    public static byte[] processToServer(byte[] data, int length) {
        byte[] finalBuffer;
        if (Config.encryption == 0) {
            finalBuffer = new byte[length];
            System.arraycopy(data, 0, finalBuffer, 0, length);
        } else {
            finalBuffer = encryptAES(data, length);
        }
        return finalBuffer;
    }

    public static byte[] intToArray(int size) {
        return ByteBuffer.allocate(4).putInt(size).array();
    }
}
