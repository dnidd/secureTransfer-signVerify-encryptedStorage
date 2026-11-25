import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Class handling connections from clients via socket,
 * BufferedReader and PrintWriter.
 * Class also handles calls to database interactions.
 */
public class ClientHandler implements Runnable {

    // Connection between the client and the server.
    private Socket serverSocket;

    // Take file from client.
    private BufferedReader in;

    // Give file to client.
    private PrintWriter out;

    // server
    private String host;

    private String clientHost;

    private int serverPort;

    // DB repo
    private Repository repo;

    // DB service layer
    private Service service;


    /**
     * Constructor.
     * @param serverSocket = serverSocket.
     * @param host = host.
     * @param repo = repository.
     */
    public ClientHandler(Socket serverSocket, String host, Repository repo) {
        try {
            this.host = host;
            this.serverSocket = serverSocket;
            this.serverPort = serverSocket.getLocalPort();
            this.clientHost = serverSocket.getInetAddress().getHostAddress();
            this.repo = repo;
            this.service = new Service(repo);
        } catch (Exception e) {
            closeEverything(serverSocket, in, out);
        }
    }


    /**
     * Method is responsible for communication with client/s
     * and database. Protocols in uppercase are implemented for
     * clients to handle incoming response.
     */
    @Override
    public void run() {

        try {
            createStreams();

            Integer userId = null;

            // User login
            while (userId == null) {

                // out = send to client via socket.
                // in = input from client via socket.
                String userName = in.readLine();

                if (userName == null) {
                    closeEverything(serverSocket, in, out);
                    return;
                }

                String password = in.readLine();

                if (password == null) {
                    closeEverything(serverSocket, in, out);
                    return;
                }

                userId = service.login(userName, password);

                if (userId != null) {
                    // authenticate == true
                    out.println("Login successful!");
                    out.println("User id: " + userId);

                    // Signal to client that login succeeded.
                    out.println("LOGIN_SUCCESS");
                } else {
                    out.println("Wrong username or password.");

                    // Signal to client that login did not succeed.
                    out.println("LOGIN_FAILED");
                }
            }


            // When user is logged in:
            String input;

            while((input = in.readLine()) != null) {

                if(input.equalsIgnoreCase("QUIT")) {
                    out.println("Goodbye!");
                    break;


                // 1 *************************
                // Show user's stored notes:
                } else if (input.equals("1")) {
                    ArrayList<EncryptedStoreNote> notes = service.getNotesByUserId(userId);
                    for(EncryptedStoreNote encryptedStoreNote : notes) {
                        out.println("Note id: " + encryptedStoreNote.getId() + " | Title: " + encryptedStoreNote.getTitle() + " | " + encryptedStoreNote.getContent( ));
                    }


                // 2 *************************
                // Read one note:
                } else if (input.equals("2")) {
                    // Present notes for user to choose:
                    ArrayList<EncryptedStoreNote> notes = service.getNotesByUserId(userId);

                    for(EncryptedStoreNote encryptedStoreNote : notes) {
                        out.println("id: " + encryptedStoreNote.getId() + " Title: " + encryptedStoreNote.getTitle());
                    }

                    while(true) {
                        out.println("ENTER_NOTE_ID");

                        String noteIdInput = in.readLine();

                        if(noteIdInput == null) {
                            break;
                        }

                        if(noteIdInput.equals("CANCEL")) {
                            out.println("NOTE_CANCELLED");
                                break;
                        }

                        int noteId;

                        try {
                            noteId = Integer.parseInt(noteIdInput);
                        } catch (NumberFormatException e) {
                            out.println("NOTE_ID_INVALID");
                            continue;
                        }

                        EncryptedStoreNote encryptedStoreNote = service.getNoteByNoteId(userId, noteId);

                        if(encryptedStoreNote == null) {
                            out.println("NOTE_ID_INVALID");
                            continue;
                        }

                        out.println("SENDING_NOTE_START");
                        out.println(encryptedStoreNote.getTitle());
                        // Send bytes as text
                        out.println(Base64.getEncoder().encodeToString(encryptedStoreNote.getContent()));
                        out.println(Base64.getEncoder().encodeToString(encryptedStoreNote.getIv()));
                        out.println("SENDING_NOTE_END");
                        out.println("NOTE_ID_VALID");
                        break;

                    }


                // 3 *************************
                // Store note in DB:
                } else if (input.equals("3")) {

                    String title = in.readLine();
                    String contentData = in.readLine();
                    String ivData = in.readLine();

                    // Check that the client sent both values
                    if (title == null || contentData == null || ivData == null) {
                        break;
                    }

                    byte[] content = Base64.getDecoder().decode(contentData);
                    byte[] iv = Base64.getDecoder().decode(ivData);

                    service.createNote(userId, title, content, iv);
                    out.println("Store successful");

                } else {
                    out.println("Error: Invalid input. Try again.");
                }
            }

            closeEverything(serverSocket, in, out);

        }  catch (IOException e) {
            System.err.println("Connection interrupted.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeEverything(serverSocket, in, out);
        }
    }


    /**
     * Close connections & streams when user exit program.
     * @param socket = server-client connection.
     * @param in = BufferedReader.
     * @param out = PrintWriter.
     */
    public void closeEverything(Socket socket, BufferedReader in, PrintWriter out) {
        // Avoid null pointer exceptions
        try {
            if(in != null) {
                in.close();
            }
            if(out != null) {
                out.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Create streams to clients via socket.
     * out: to client.
     * in: from client.
     * @throws IOException propagates to constructor.
     */
    private void createStreams() throws IOException {
        // write stream to client
        out = new PrintWriter(                                                  // enables: .println(message)
                new OutputStreamWriter(                                         // text to bytes
                        serverSocket.getOutputStream(), "UTF-8"),   // create output stream
                true);                                                          // flush buffer

        // get text from client
        in = new BufferedReader(                                                // enables: in.readLine()
                new InputStreamReader(                                          // byte to text
                        serverSocket.getInputStream(),  "UTF-8"));  // create input stream

    }
}
