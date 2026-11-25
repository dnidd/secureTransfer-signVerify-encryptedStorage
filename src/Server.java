import java.sql.Connection;

/**
 * Server entry point.
 * Takes args or uses default arg for port to use.
 * Invokes call to initiate connection.
 */
public class Server {

    private static final int DEFAULT_PORT = 2000;

    public static void main(String[] args) {

        //  System variables for keystore name and password used for build and run in IntelliJ IDEA
//        System.setProperty("javax.net.ssl.keyStore", "keystore");
//        System.setProperty("javax.net.ssl.keyStorePassword", "password");

        int port = DEFAULT_PORT;

        // If arg, assign variable port.
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port number must be an integer: java Server <port>");
                return;
            }
        } else if (args.length > 1) {
            System.err.println("Too many arguments: java Server <port>");
            return;
        }

        // DBConnector holds connection to DB.
        // .connectDb(); creates actual connection.
        DBConnector dbConnector = new DBConnector();
        Connection connection = dbConnector.connectDb();

        Repository repository = new Repository(connection);

        // Create/initialize tables and placeholder data.
        repository.initDB();


        CreateServer createServer = new CreateServer(port, repository);
        createServer.initServer();


    }

}
