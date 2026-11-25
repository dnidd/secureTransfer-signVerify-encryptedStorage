import java.util.ArrayList;

/**
 * Service layer sitting between Server and Repository.
 */
public class Service {

    Repository repo;

    /**
     * Constructor.
     * @param repo = Repository.
     */
    public Service(Repository repo) {
        this.repo = repo;
    }

    /**
     * User login.
     * @param username = username.
     * @param password = password.
     * @return userId.
     */
    public Integer login(String username, String password) {
        Integer userId = repo.login(username, password);
        return userId;
    }


    /**
     * Create and store note.
     * @param userId = userId.
     * @param title = note title.
     * @param content = note content.
     * @param iv = initialization vector.
     */
    public void createNote(int userId, String title, byte[] content, byte[] iv) {
        repo.createNote(userId, title, content, iv);
    }


    /**
     * Get user's all notes.
     * @param userId = userId.
     * @return ArrayList of user's Note.
     */
    public ArrayList<EncryptedStoreNote> getNotesByUserId(int userId) {
        return repo.getNotesByUserId(userId);
    }


    /**
     * Fetch note on id.
     * @param userId = userId.
     * @param noteId = noteId.
     * @return encrypted note.
     */
    public EncryptedStoreNote getNoteByNoteId(int userId, int noteId) {
        return repo.getNoteByNoteId(userId, noteId);
    }
}
