import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Initialize and create/establish connection using SSL/TLS socket.
 * Create concurrent client communications in Thread using runnable.
 */
public class CreateServer {

    private final SSLServerSocketFactory factory;
    private SSLServerSocket sslServerSocket;
    private final int port;
    private Repository repository;



    /**
     * Constructor.
     * @param port = default or arg.
     * @param repository = Repository.
     */
    public CreateServer(int port, Repository repository) {
        this.port = port;
        this.factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.sslServerSocket = null;
        this.repository = repository;
    }


    /**
     * Initialize/create SSLServerSocket.
     * Call to start server.
     */
    public void initServer() {
        try {
            // Returns a server socket bound to the specified port.
            sslServerSocket = (SSLServerSocket) factory.createServerSocket(port);

            // Specify TLS versions:
            sslServerSocket.setEnabledProtocols(new String[]{"TLSv1.3"});

            // No server side authentication for client.
            sslServerSocket.setNeedClientAuth(false);

            // Call to establish connection.
            connectToServer();
        } catch (IOException e) {
            System.err.println("Server connection failed." );
            e.printStackTrace();
        }
    }


    /**
     * sslServerSocket.accept() returns Socket when connection to client accepted.
     * Server communicates with client with returned Socket.
     * Create ClientHandler to be used in new separate Thread.
     * New Thread contains connection between client and server.
     * Server can handle multiple clients.
     */
    public void connectToServer() {
        System.out.println("Starting server on port " + sslServerSocket.getLocalPort());

        // host name where server lives.
        String hostname;

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // When connection not closed - accept new connection.
        try {
            while(!sslServerSocket.isClosed()) {
                // Connection established between server and client.
                SSLSocket serverSocket = (SSLSocket) sslServerSocket.accept();

                ClientHandler clientHandler = new ClientHandler(serverSocket, hostname, repository);

                Thread.startVirtualThread(clientHandler);
            }
        } catch (IOException e) {
            terminateServerSocket();
        }
    }


    /**
     * Close server socket if failure.
     */
    private void terminateServerSocket() {
        try {
            if(sslServerSocket != null) {
                sslServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
