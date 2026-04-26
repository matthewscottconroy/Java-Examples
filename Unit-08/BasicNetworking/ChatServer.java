import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    // List to hold all client output streams for broadcasting messages.
    private static List<PrintWriter> clientOutputs = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        int port = 12345; // Port the server listens on

        // Note: there is no graceful server-shutdown mechanism here. To stop the
        // server cleanly in production code you would close the ServerSocket from
        // another thread and break out of the accept loop.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Accept a new client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new thread for this client and start it.
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException ex) {
            System.err.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Broadcast a message to all connected clients except the one that sent it.
    // Design note: the sender is excluded here, so chat clients must NOT rely
    // on seeing their own messages echoed back from the server — they should
    // display the message locally at send time instead.  Each client also needs
    // a dedicated reader thread to receive and display messages from other
    // participants; a client that only writes will silently miss all incoming chat.
    public static void broadcast(String message, PrintWriter excludeWriter) {
        synchronized (clientOutputs) {
            for (PrintWriter writer : clientOutputs) {
                if (writer != excludeWriter) {
                    writer.println(message);
                }
            }
        }
    }

    // Inner class that handles communication with a client.
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            // Build a human-readable sender label from the client's address and port.
            String sender = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            try {
                // Create writer for sending messages to the client.
                out = new PrintWriter(socket.getOutputStream(), true);
                // Create reader for receiving messages from the client.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Add this client's output stream to the broadcast list.
                clientOutputs.add(out);

                String message;
                while ((message = in.readLine()) != null) {
                    String tagged = "[" + sender + "]: " + message;
                    System.out.println("Received: " + tagged);
                    // Broadcast the labelled message to all other clients.
                    ChatServer.broadcast(tagged, out);
                }
            } catch (IOException ex) {
                System.err.println("ClientHandler exception: " + ex.getMessage());
            } finally {
                // Clean up: remove client's writer and close connections.
                if (out != null) {
                    clientOutputs.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException ex) {
                    System.err.println("Failed to close socket: " + ex.getMessage());
                }
            }
        }
    }
}

