package arteh.world.goroxy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {
    static SecretKeySpec keySpec;
    static IvParameterSpec ivSpec;

    public static void initialize() {
        try {
            keySpec = new SecretKeySpec(Config.encryption_key.getBytes(StandardCharsets.UTF_8), "AES");
            ivSpec = new IvParameterSpec(Config.encryption_key.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encryptAES(byte[] message, int length) {
        try {
            Cipher cipheren = Cipher.getInstance("AES/CBC/NoPadding");
            cipheren.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            int final_length = length + 4;
            int key_length = Config.encryption_key.getBytes(StandardCharsets.UTF_8).length;
            int plus = (length + 4) % key_length;
            if (plus > 0) {
                final_length = length + 4 + (key_length - plus);
            }

            byte[] finalbuffer = new byte[final_length];

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
            Cipher cipherde = Cipher.getInstance("AES/CBC/NoPadding");
            cipherde.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

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