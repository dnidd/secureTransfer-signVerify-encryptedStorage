import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * Class responsible for creating secret key,
 * saving secret key locally on file, read secret key,
 * encrypt and decrypt content.
 */
public class EncryptionHandler {

    private final SecretKeyGenerator secretKeyGenerator;


    /**
     * Constructor.
     * @throws NoSuchAlgorithmException e.
     */
    public EncryptionHandler() throws NoSuchAlgorithmException {
        secretKeyGenerator = new SecretKeyGenerator();
    }


    /**
     * Generate secret key.
     * @return SecretKey
     * @throws NoSuchAlgorithmException e.
     */
    public SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        return secretKeyGenerator.initKey();
    }


    /**
     * Converts SecretKey to bytes, write key to file named with the title of the note.
     * @param title = file to store secret key, the title of the note to be saved.
     */
    public void generateSecretKeyWriteSecretKeyToFile(String title) throws NoSuchAlgorithmException {
        SecretKey key = generateSecretKey();

        byte[] keyBytes = key.getEncoded();

        try (FileOutputStream out = new FileOutputStream(title)) {
            out.write(keyBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Read secret key from file - convert bytes to Secret Key.
     * @param file = file of secret key in bytes.
     * @return SecretKey.
     * @throws IOException e.
     */
    public SecretKey readSecretKeyFromFile(String file) throws IOException {
        byte[] secretKey;

        try (FileInputStream in = new FileInputStream(file)) {
            secretKey = in.readAllBytes();
        }
        SecretKey originalKey = new SecretKeySpec(secretKey, 0, secretKey.length, "AES");
        return originalKey;
    }


    /**
     *
     * @param content = note content.
     * @return content byte[].
     */
    public byte[] convertContentToBytes(String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        return contentBytes;
    }


    /**
     * Encrypt note content using AES algorithm and Initialization Vector.
     * @param byteContent = note content as bytes.
     * @param title = note title.
     * @return noteStore object.
     * @throws IOException e.
     * @throws NoSuchPaddingException e.
     * @throws NoSuchAlgorithmException e.
     * @throws InvalidAlgorithmParameterException e.
     * @throws InvalidKeyException e.
     * @throws IllegalBlockSizeException e.
     * @throws BadPaddingException e.
     */
    public NoteStore encryptContent(byte[] byteContent, String title) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKey key = readSecretKeyFromFile(title);

        GCMParameterSpec iv = generateIv();

        Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encryptedTextBytes = encryptionCipher.doFinal(byteContent);

        NoteStore noteStore = new NoteStore(encryptedTextBytes, iv.getIV());

        return noteStore;
    }


    /**
     * Decrypt note content using secret key, AES algorithm and Initialization Vector.
     * @param content = note content.
     * @param iv = Initialization Vector.
     * @param file = secret key file.
     * @return = decrypted note content.
     * @throws IOException e.
     * @throws NoSuchPaddingException e.
     * @throws NoSuchAlgorithmException e.
     * @throws InvalidAlgorithmParameterException e.
     * @throws InvalidKeyException e.
     * @throws IllegalBlockSizeException e.
     * @throws BadPaddingException e.
     */
    public String decryptNoteContent(byte[] content, byte[] iv, String file) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

        SecretKey key = readSecretKeyFromFile(file);

        // Configure decryption cipher.
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // Decrypt encryptedContent (encrypted byte file)
        byte[] decryptedContent = decryptionCipher.doFinal(content);

        String textContent = new String(decryptedContent, StandardCharsets.UTF_8);

        return textContent;
    }


    /**
     * Helper method  generates Initialization Vector.
     * @return GCMParameterSpec.
     */
    public static GCMParameterSpec generateIv() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(128, iv);
    }
}