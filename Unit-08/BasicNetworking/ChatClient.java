import java.io.*;
import java.net.*;

public class ChatClient {

    public static void main(String[] args) {
        String serverAddress = "localhost";  // Server's IP address or hostname
        int port = 12345;

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Start a new thread to read messages from the server.
            new Thread(new ServerListener(socket)).start();

            System.out.println("Connected to the chat server. Type messages to send:");

            // Read messages from the console and send them to the server.
            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                out.println(userInput);
            }
        } catch (IOException ex) {
            System.err.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Inner class to handle incoming messages from the server.
    private static class ServerListener implements Runnable {
        private Socket socket;

        public ServerListener(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = serverReader.readLine()) != null) {
                    // Display message from the server.
                    System.out.println("Message from others: " + message);
                }
                // readLine() returns null when the server closes the connection.
                System.out.println("Server disconnected. Exiting.");
            } catch (IOException ex) {
                System.err.println("ServerListener exception: " + ex.getMessage());
            }
            // Exit the process so the main thread (blocked on System.in) also terminates.
            System.exit(0);
        }
    }
}

