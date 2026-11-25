import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class used in separate threads for each connected client.
 * implements Runnable to be used in thread.
 * Communicator to server via socket.
 * Takes incoming messages from server.
 * Forward incoming messages to client.
 * ClientSocketHandler is listening to the server while client can read the keyboard.
 */
public class VaultSocketHandler implements Runnable {

    private final VaultClient vaultClient;
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // Thread safe BlockingQueue holds incoming responses.
    private final BlockingQueue<Boolean> loginResults = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> serverIdResponses = new LinkedBlockingQueue<>();


    /**
     * Constructor
     * Create in and out streams
     * @param vaultClient = client
     * @param socket = connected socket
     * @throws IOException e.
     */
    public VaultSocketHandler(VaultClient vaultClient, Socket socket) throws IOException {
            this.vaultClient = vaultClient;
            this.socket = socket;
            createStreams();
    }


    /**
     * Continually reading from server.
     * Listening/waiting for messages is a blocking operation - needs a separate thread.
     * Keep listening when username or password not correct & when fetching notes.
     */
    @Override
    public void run() {
        String serverData;

        try {
            // Constant listening to server.
            while ((serverData = in.readLine()) != null) {


                // User login responses.
                if (serverData.equalsIgnoreCase("LOGIN_SUCCESS")) {
                    // stores boolean
                    loginResults.put(true);
                    continue;

                // Keep listening when username or password not correct.
                } if (serverData.equalsIgnoreCase("LOGIN_FAILED")) {
                    // stores boolean
                    loginResults.put(false);
                    continue;
                }

                // Fetching note - handling note id.
                if (serverData.equals("ENTER_NOTE_ID")
                        || serverData.equals("NOTE_ID_INVALID")
                        || serverData.equals("NOTE_ID_VALID")
                        || serverData.equals("NOTE_CANCELLED")) {
                    // puts responses in queue for main thread to wait for events to respond to.
                    // Stores String values.
                    serverIdResponses.put(serverData);
                    continue;
                }

                // Fetch note data.
                if (serverData.equals("SENDING_NOTE_START")) {
                    String title = in.readLine();
                    String content = in.readLine();
                    String iv = in.readLine();
                    String end = in.readLine();

                    if (title == null || content == null || iv == null || !end.equals("SENDING_NOTE_END")) {
                        System.err.println("Corrupted note");
                        continue;
                    }

                    try {
                        byte[] contentBytes = Base64.getDecoder().decode(content);
                        byte[] ivBytes = Base64.getDecoder().decode(iv);

                        // vaultClient fetches note data.
                        vaultClient.getEncryptedNote(title, contentBytes, ivBytes);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Error reading note");
                    }

                    continue;
                }

                // prints from server response.
                System.out.println(serverData);

            }
        } catch (IOException e) {
            System.err.println("Connection down.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }


    /**
     * Send user input to server via socket (out)
     * @param input = user input
     */
    public void sendCommand(String input) {
        out.println(input);
    }


    /**
     * take() - waits for a head element of a queue and removes it.
     * If the queue is empty, it blocks and waits for an element to become available.
     * @return = head of this queue: true or false.
     * @throws InterruptedException e.
     */
    public boolean loginAwaitRemove() throws InterruptedException {
        return loginResults.take();
    }


    /**
     * take() - waits for a head element of a queue and removes it.
     * If the queue is empty, it blocks and waits for an element to become available.
     * @return = head of this queue - protocol response.
     * @throws InterruptedException e.
     */
    public String awaitServerIdResponse() throws InterruptedException {
        return serverIdResponses.take();
    }


    /**
     * Create streams to server via socket.
     * out: to server
     * in: from server
     * @throws IOException propagates to constructor.
     */
    private void createStreams() throws IOException {
        // write stream to socket
        out = new PrintWriter(                                            // enables: .println(message)
                new OutputStreamWriter(                                   // text to bytes
                        socket.getOutputStream(), "UTF-8"),   // create output stream
                true);                                                    // flush buffer

        // get text from server
        in = new BufferedReader(                                          // enables: in.readLine()
                new InputStreamReader(                                    // byte to text
                        socket.getInputStream(),  "UTF-8"));  // create input stream

    }

}
