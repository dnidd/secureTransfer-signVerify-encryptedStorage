import java.sql.Connection;
import java.sql.DriverManager;


/**
 * Establish connection to database.
 * Database username & password as environment variables.
 */
public class DBConnector {

    Connection connection;

    public Connection connectDb() {

        // allowMultiQueries=true
        // Allow multiple placeholder queries from populateStartDataToDB() in GuestRepository.
        String url =
                "jdbc:mysql://localhost:3306/safestore"
                        + "?allowMultiQueries=true"
                        + "&sslMode=DISABLED";


        // DB credentials as environment variables.
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (username == null || password == null) {
            System.out.println("Please provide DB_USERNAME and DB_PASSWORD as environment variables.");
            return null;
        }

        // Manage connection to a specific db.
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            connection = DriverManager.getConnection(
                    url, username, password
            );


        } catch (Exception e) {
            System.out.println("MySQL connection failed:");
            e.printStackTrace();
        }

        return connection;
    }

}
