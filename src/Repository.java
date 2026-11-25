import java.sql.*;
import java.util.ArrayList;

/**
 * Class handling CRUD operations on database.
 * For now write and read only.
 */
public class Repository {

    Connection connection;

    public Repository(Connection connection) {
        this.connection = connection;
    }


    /**
     * User login.
     * Query looks for matching values for username and password in table 'users'.
     * @param username = username.
     * @param password = password.
     * @return users id.
     */
    public Integer login(String username, String password) {
        String query = """
                        SELECT id
                        FROM users
                        WHERE username = ? AND password = ?
                        """;

        // PreparedStatement safer against SQL injection.
        try (PreparedStatement ps = connection.prepareStatement(query)){
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Create and store encrypted note.
     * @param userId = id.
     * @param title = note title.
     * @param content = note content.
     * @param iv = initialization vector.
     */
    public void createNote(int userId, String  title, byte[] content, byte[] iv) {
        String query = """
                INSERT INTO notes (user_id, title, encrypted_content, iv) 
                VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(query)){
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setBytes(3, content);
            ps.setBytes(4, iv);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Fetch note by id.
     * @param userId = user id.
     * @param noteId = note id.
     * @return encrypted note.
     */
    public EncryptedStoreNote getNoteByNoteId(int userId, int noteId) {
        String query = """
            SELECT id, user_id, title, encrypted_content, iv
            FROM notes
            WHERE id = ? AND user_id = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    byte[] content = rs.getBytes("encrypted_content");
                    byte[] iv = rs.getBytes("iv");

                    return new EncryptedStoreNote(id, title, content, iv);
                }
            }
            // No note found with this ID
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * Fetch all notes from one user.
     * @return List of notes.
     */
    public ArrayList<EncryptedStoreNote> getNotesByUserId(int userId) {
        String query = """
                SELECT id, user_id, title, encrypted_content, iv
                FROM notes
                WHERE user_id = ?
                """ ;

        ArrayList<EncryptedStoreNote> notes = new ArrayList<>();

        // statement objects returns resultset objects.
        // ResultSet: table representing the results returned from the db.
        try (PreparedStatement ps = connection.prepareStatement(query)) {

            // 1 = first column.
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    byte[] content = rs.getBytes("encrypted_content");
                    byte[] iv = rs.getBytes("iv");
                    notes.add(new EncryptedStoreNote(id, title, content, iv));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return notes;
    }


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    // ***********   Initialize DB with placeholder data  ***********
    // --------------------------------------------------------------
    // --------------------------------------------------------------

    /**
     * Initialize placeholder data in tables: users and notes.
     * Call to methods:
     * usersPlaceholdersToDB();
     * notesPlaceholdersToDB();
     */
    public void initDB() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS notes");
        } catch (SQLException e) {
            throw new RuntimeException("Could not drop table 'notes'", e);
        }

        usersPlaceholdersToDB();
        notesPlaceholdersToDB();

        System.out.println("Database initialized successfully.");
    }


    /**
     * Create table 'users'.
     * Delete data on restart.
     * Populate DB with placeholder users with id (PK), username and password.
     */
    public void usersPlaceholdersToDB() {
        String dropSql = "DROP TABLE IF EXISTS users";

        String createSql = """
        CREATE TABLE users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(255) NOT NULL UNIQUE,
            password VARCHAR(255) NOT NULL
        )
        """;

        String insertSql = """
        INSERT INTO users (username, password)
        VALUES (?, ?)
        """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropSql);
            statement.executeUpdate(createSql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (PreparedStatement statement =
                     connection.prepareStatement(insertSql)) {

            statement.setString(1, "user1");
            statement.setString(2, "pass1");
            statement.executeUpdate();

            statement.setString(1, "user2");
            statement.setString(2, "pass2");
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Create table 'notes'.
     * Delete data on restart.
     * Populate DB with placeholder notes with id, user_id, title, content.
     */
    public void notesPlaceholdersToDB() {
        String dropSql = "DROP TABLE IF EXISTS notes";

        String createSql = """
        CREATE TABLE notes (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            title VARCHAR(255) NOT NULL,
            encrypted_content BLOB NOT NULL,
            iv BLOB NOT NULL,
            FOREIGN KEY (user_id) 
                REFERENCES users(id)
                ON DELETE CASCADE -- if a user is deleted, the users nptes are deleted.
        )
        """;

        String insertSql = """
        INSERT INTO notes (user_id, title, encrypted_content, iv)
        VALUES (?, ?, ?, ?)
        """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropSql);
            statement.executeUpdate(createSql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // byte[] placeholder data
        byte[] encryptedContent1 = new byte[] {10, 20, 30, 40, 50, 60, 70, 80, 90};
        byte[] encryptedContent2 = new byte[] {20, 30, 40, 50, 60, 70, 80, 90, 10};
        byte[] encryptedContent3 = new byte[] {30, 40, 50, 60, 70, 80, 90, 10, 20};
        byte[] iv1 = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        byte[] iv2 = new byte[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1};
        byte[] iv3 = new byte[] {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2};

        try (PreparedStatement statement =
                     connection.prepareStatement(insertSql)) {

            // user id
            statement.setInt(1, 1);
            statement.setString(2, "title1_userId_1");
            statement.setBytes(3, encryptedContent1);
            statement.setBytes(4, iv1);
            statement.executeUpdate();

            statement.setInt(1, 1);
            statement.setString(2, "title2_userId_1");
            statement.setBytes(3, encryptedContent2);
            statement.setBytes(4, iv2);
            statement.executeUpdate();

            // user id
            statement.setInt(1, 2);
            statement.setString(2, "title3_userId_2");
            statement.setBytes(3, encryptedContent3);
            statement.setBytes(4, iv3);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
