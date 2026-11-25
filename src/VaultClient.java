import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

/**
 * Connects to server via SSL Socket.
 * Create communication Thread.
 * Take user input - send to Handler.
 * Takes messages coming from server.
 * VaultClient can read from the keyboard while VaultSocketHandler is listening to the server.
 */
public class VaultClient {

    // SSL used for secure sockets.
    private final SSLSocket secureSocket;
    private VaultSocketHandler vaultSocketHandler;
    EncryptionHandler encrypt;

    // Note data from server:
    String fetchedNoteTitle;
    byte[] fetchedNoteContent;
    byte[] fetchedNoteIV;

    // Flag for when note is received:
    private boolean noteReceived = false;

    /**
     * Constructor
     * @param host = server address/host
     * @param port = port
     */
    public VaultClient(String host, int port) throws IOException, NoSuchAlgorithmException {
        // Connect SSL socket to server.
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        secureSocket = (SSLSocket) factory.createSocket(host, port);
        encrypt = new EncryptionHandler();

        // Specify TLS versions:
        secureSocket.setEnabledProtocols(new String[]{"TLSv1.3"});

        // Server and client makes agreements on safe communications.
        secureSocket.startHandshake();
    }

    /**
     * Create VaultSocketHandler
     * Create and start Thread with VaultSocketHandler injected.
     * Create messages from user keyboard.
     * Forward messages to VaultSocketHandler for server transfer.
     * @throws IOException propagates to main in Client
     */
    public void startVaultClient() throws IOException {

        Scanner scanner = new Scanner(System.in);

        try {
            vaultSocketHandler = new VaultSocketHandler(this, secureSocket);

            // This reads from the keyboard while
            // vaultSocketHandler is listening to the server.
            Thread.startVirtualThread(vaultSocketHandler);

            // login validation
            boolean loginSuccess = false;

            // Login *************************
            while (!loginSuccess) {
                // Send username to server.
                System.out.print("Enter username: ");
                String userName = scanner.nextLine();
                vaultSocketHandler.sendCommand(userName);

                // Send password to server.
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                vaultSocketHandler.sendCommand(password);

                try {
                    // Determines what is returned based on what is put in BlockingQueue in vaultSocketHandler
                    // depending on server protocol response.
                    // If "LOGIN_SUCCESS" true is put.
                    // If "LOGIN_FAILED" false is put.
                    loginSuccess = vaultSocketHandler.loginAwaitRemove();
                } catch (InterruptedException e) {
                    // Signal interruption to current thread.
                    Thread.currentThread().interrupt();
                    return;
                }

                if (!loginSuccess) {
                    System.out.println("Please try again.");
                }
            }


            // UI when user is logged in:
            while (true) {
                System.out.println("Show notes: (1)");
                System.out.println("Fetch & read note: (2)");
                System.out.println("Wright & save note: (3)");
                System.out.println("Quit: (QUIT)");


                String input = scanner.nextLine();


                // ***************************
                // *********** 1 *************
                // *** Show stored notes: ****
                if (input.equals("1")) {
                    // Send input to server via VaultSocketHandler
                    vaultSocketHandler.sendCommand(input);


                // *************************
                // ********** 2 ************
                // ***** Read one note: ****
                } else if (input.equals("2")) {
                    // Tell the server that we want to save a note
                    vaultSocketHandler.sendCommand("2");

                    // flags
                    boolean noteFetched = false;
                    boolean cancelled = false;

                    try {
                        while (!noteFetched && !cancelled) {

                            // Fetch server protocols stored in
                            // BlockingQueue<String> serverIdResponses in VaultSocketHandler.
                            // protocol options are taken (take()) form BlockingQueue.
                            String response = vaultSocketHandler.awaitServerIdResponse();

                            // Server protocol communication
                            if(response.equals("ENTER_NOTE_ID")) {
                                System.out.println("Enter note ID (or CANCEL): ");
                                String noteId = scanner.nextLine();

                                vaultSocketHandler.sendCommand(noteId);

                            } else if (response.equals("NOTE_ID_INVALID")) {
                                System.out.println("Id not found. Try again please.");

                            } else if (response.equals("NOTE_ID_VALID")) {
                                noteFetched = true;

                            } else if (response.equals("NOTE_CANCELLED")) {
                                cancelled = true;
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    if (cancelled) {
                        continue;
                    }

                    if (noteFetched) {
                        // Wait for socket handler/server response to fetch and deliver.
                        try {
                            waitForNote();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }

                        String content = null;

                        // decrypt and print fetched note.
                        while (content == null) {
                            System.out.println("Enter file name holding secret key: ");
                            String secretKeyFile = scanner.nextLine();

                            try {
                                // decrypt content fetched from server using iv and secret key kept on file.
                                content = encrypt.decryptNoteContent(fetchedNoteContent, fetchedNoteIV, secretKeyFile);
                            } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException |
                                     InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
                                     BadPaddingException e) {
                                System.out.println("File name not found, please try again.");
                            }
                        }

                        System.out.println(" ");
                        System.out.println("-- Note --");
                        System.out.println("Title: " + fetchedNoteTitle + "\nContent: " + content);
                        System.out.println(" ");
                    }


                // *************************
                // *********** 3 ***********
                // ****Store note in DB:****
                } else if (input.equals("3")) {
                    // Tell the server that we want to save a note
                    vaultSocketHandler.sendCommand(input);

                    System.out.println("Enter title: (use _ between words)");
                    String title = scanner.nextLine();

                    //  Generate secret key.
                    //  Write secret key to file (with name of Note-title).
                    encrypt.generateSecretKeyWriteSecretKeyToFile(title);

                    System.out.println("Enter content:");
                    String content = scanner.nextLine();

                    // Convert content to be encrypted to bytes.
                    byte[] byteContent = encrypt.convertContentToBytes(content);

                    // Encrypt text/content.
                    NoteStore noteStore = encrypt.encryptContent(byteContent, title);

                    // Receive encrypted data and initialization vector.
                    byte[] encryptedData = noteStore.getEncryptedData();
                    byte[] initVector = noteStore.getIv();

                    // Send off to server:
                    vaultSocketHandler.sendCommand(title);
                    vaultSocketHandler.sendCommand(Base64.getEncoder().encodeToString(encryptedData));
                    vaultSocketHandler.sendCommand(Base64.getEncoder().encodeToString(initVector));
                }


                if (input.equalsIgnoreCase("QUIT")) {
                    vaultSocketHandler.sendCommand("QUIT");
                    break;
                }

            }
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        } finally {
            secureSocket.close();
        }
    }


    /**
     * Pause code when waiting to receive expected data.
     * When noteReceived == false -> enter loop
     * @throws InterruptedException e.
     */
    public synchronized void waitForNote() throws InterruptedException {
        while (!noteReceived) {
            wait();
        }
        noteReceived = false;
    }


    /**
     * Fetch encrypted note from server in VaultSocketHandler when server/client protocol fulfilled.
     * @param title = title.
     * @param content = content.
     * @param iv = iv.
     */
    public synchronized void getEncryptedNote(String title, byte[] content, byte[] iv) {
        fetchedNoteTitle = title;
        fetchedNoteContent = content;
        fetchedNoteIV = iv;

        // Switch flag.
        noteReceived = true;
        // Wakes threads if not received.
        notifyAll();
    }
}