package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private NewServer server;

    public ClientHandler(Socket socket, NewServer server) {
        this.socket = socket;
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            boolean usernameTaken = true;
            while (usernameTaken) {
                writer.println("Enter your username:");
                username = reader.readLine();

                if (server.isUsernameTaken(username) || username.length() < 1) {
                    writer.println("Username is already taken. Please enter another username.");
                } else {
                    usernameTaken = false;
                    writer.println("Username set successfully.");
                }
            }
            server.takenUsernames.add(username);
            System.out.println("User connected: " + username);
            server.sendUserList();

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(message + " server message");
                if (message.startsWith("/private ")) {
                    String[] parts = message.substring(9).split(" ", 2);
                    String recipient = parts[0];
                    String privateMessage = parts[1];
                    server.sendPrivateMessage(username, recipient, privateMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                writer.close();
                socket.close();
                System.out.println("User disconnected: " + username);
                server.removeClient(this);
                server.sendUserList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}