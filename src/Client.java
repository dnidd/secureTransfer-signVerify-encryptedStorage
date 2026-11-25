import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Entry point.
 * Takes command line args host and port or defaults.
 * Creates VaultClient depending on args.
 */
public class Client {

    private static final String DEFAULT_HOST =  "127.0.0.1";
    private static final int DEFAULT_PORT = 2000;

    public static void main(String[] args) throws IOException {

        //  System variables for truststore name and password used for build and run in IntelliJ IDEA
//        System.setProperty("javax.net.ssl.trustStore", "truststore");
//        System.setProperty("javax.net.ssl.trustStorePassword", "password");


        // 0 args -> default host + port
        if (args.length == 0) {
            try {
                VaultClient vaultClient = new VaultClient(DEFAULT_HOST, DEFAULT_PORT);
                vaultClient.startVaultClient();
            } catch (IOException e) {
                System.err.println("Server connection failed.");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }


        // 1 args -> set host, default port
        else if(args.length == 1) {
            try {
                VaultClient vaultClient = new VaultClient(args[0], DEFAULT_PORT);
                vaultClient.startVaultClient();
            } catch (IOException | NoSuchAlgorithmException e) {
                System.err.println("Server connection failed.");
            }
        }


        // 2 args -> set host + port
        else if(args.length == 2 ) {
            try {
                int port = Integer.parseInt(args[1]);
                VaultClient vaultClient = new VaultClient(args[0], port);
                vaultClient.startVaultClient();
            } catch (NumberFormatException e) {
                System.err.println("Port must be a number.");
            } catch (IOException | NoSuchAlgorithmException e) {
                System.err.println("Server connection failed.");
            }
        }


        else {
            System.err.println("Usage: java Client <host> <port>");
        }

    }

}