package ChatApplication;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
public class ChatServer {
    private static final int PORT = 12345;
    private static final Map<String, PrintWriter> clients = new ConcurrentHashMap<>();
    private static final Map<String, String> userPasswords = new HashMap<>();
    private static final Map<String, BlockingQueue<String>> userMessages = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> chatHistory = new ConcurrentHashMap<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static void main(String[] args) {
        // Load user credentials (dummy method)
        loadUserCredentials();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadUserCredentials() {
        // Dummy method to load user credentials
        userPasswords.put("Priyam", "pri");
        userPasswords.put("Aniket", "aniket7800");
        // Add more users as needed
    }
    private static class Handler extends Thread {
        private final Socket socket;
        private final PrintWriter writer;
        private final BufferedReader reader;
        private String username;

        public Handler(Socket socket) throws IOException {
            this.socket = socket;
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void run() {
            try {
                authenticateUser();

                // Send chat history to the client
                sendChatHistory();

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.equalsIgnoreCase("/exit")) {
                        break;
                    }
                    handleMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    clients.remove(username);
                    broadcast(username + " has left the chat");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void authenticateUser() throws IOException {
            while (true) {
                writer.println("Please enter your username:");
                username = reader.readLine();
                if (username == null || !userPasswords.containsKey(username)) {
                    writer.println("Invalid username. Please try again.");
                } else {
                    String passwordHash = userPasswords.get(username);
                    writer.println("Please enter your password:");
                    String password = reader.readLine();
                    if (password == null || !passwordHash.equals(password)) {
                        writer.println("Incorrect password. Please try again.");
                    } else {
                        clients.put(username, writer);
                        userMessages.put(username, new LinkedBlockingQueue<>());
                        broadcast(username + " has joined the chat");
                        break;
                    }
                }
            }
        }

        private void handleMessage(String message) {
            String formattedMessage = "[" + dateFormat.format(new Date()) + "] " + username + ": " + message;
            chatHistory.computeIfAbsent(username, k -> new ArrayList<>()).add(formattedMessage);
            broadcast(formattedMessage);
        }

        private void broadcast(String message) {
            for (PrintWriter clientWriter : clients.values()) {
                clientWriter.println(message);
            }
        }

        private void sendChatHistory() {
            List<String> history = chatHistory.getOrDefault(username, Collections.emptyList());
            for (String msg : history) {
                writer.println(msg);
            }
        }
    }
}
