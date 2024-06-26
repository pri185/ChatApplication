// add VM option when give error in edit run configuration

//--module-path C:\Users\HP\Downloads\openjfx-22.0.1_windows-x64_bin-sdk\javafx-sdk-22.0.1\lib --add-modules javafx.controls,javafx.fxml
package ChatApplication;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClientUIUser1 extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private TextField messageField;
    private TextArea chatArea;
    private PrintWriter writer;

    private StringBuilder chatHistory; // To store chat history

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Initialize UI components
        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setOnAction(e -> sendMessage());

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());

        VBox layout = new VBox(10, chatArea, messageField, sendButton);

        Scene scene = new Scene(layout, 400, 400);
        stage.setTitle("Chat Client - User 1");
        stage.setScene(scene);
        stage.show();

        // Connect to the server and set up the input/output streams
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;
                    while ((message = reader.readLine()) != null) {
                        System.out.println("Received message from server: " + message); // Debugging print statement
                        updateChatHistory(message);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        chatHistory = new StringBuilder(); // Initialize chat history
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            System.out.println("Sending message to server: " + message); // Debugging print statement
            writer.println(message);
            messageField.clear();
        }
    }
    private void updateChatHistory(String message) {
        chatHistory.append(message).append("\n"); // Append new message to chat history
        chatArea.setText(chatHistory.toString()); // Update chat area with the updated chat history
    }
}
