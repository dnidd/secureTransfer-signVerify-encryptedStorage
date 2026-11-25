import java.util.Arrays;

/**
 * Client side Note class holding encrypted data.
 * Initialization Vector (iv) belongs to encryption/decryption.
 */
public class NoteStore {

    private final byte[] encryptedData;
    private final byte[] iv;

    public NoteStore(byte[] encryptedData, byte[] iv) {
        this.encryptedData = encryptedData;
        this.iv = iv;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public byte[] getIv() {
        return iv;
    }


    @Override
    public String toString() {
        return "NoteStore{" +
                "encryptedData=" + Arrays.toString(encryptedData) +
                ", iv=" + Arrays.toString(iv) +
                '}';
    }
}
