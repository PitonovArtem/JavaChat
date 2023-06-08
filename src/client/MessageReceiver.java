package client;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageReceiver implements Runnable {
    private BufferedReader reader;
    private Client client;

    public MessageReceiver(BufferedReader reader, Client client) {
        this.reader = reader;
        this.client = client;
    }

    @Override
    public synchronized void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("/users ")) {
                    String[] users = message.substring(7).split(",");
                    client.updateUserList(users);
                } else {
                    String[] parts = message.split(" ", 3);
                    String sender = parts[1];
                    String messageText = parts[2];
                    client.handlePrivateCommand(sender, messageText);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}