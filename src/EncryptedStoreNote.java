import java.util.Arrays;

/**
 * Server side Note Class holding encrypted data.
 * Initialization Vector (iv) belongs to encryption/decryption.
 */
public class EncryptedStoreNote {
    private Integer id;
    private String title;
    private byte[] content;
    private byte[] iv;

    /**
     * No args constructor.
     */
    public EncryptedStoreNote(){}


    /**
     * All args constructor.
     * @param id = id.
     * @param title = title.
     * @param content = content.
     * @param iv = content.
     */
    public EncryptedStoreNote(Integer id, String title, byte[] content, byte[] iv) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.iv = iv;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    @Override
    public String toString() {
        return "EncryptedStoreNote{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content=" + Arrays.toString(content) +
                ", iv=" + Arrays.toString(iv) +
                '}';
    }
}

