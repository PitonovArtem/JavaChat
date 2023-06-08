package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NewServer {
    private List<ClientHandler> clients;
    private Lock lock;
    List<String> takenUsernames;

    public NewServer(int port) {
        clients = new ArrayList<>();
        takenUsernames = new ArrayList<>();
        lock = new ReentrantLock();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                takenUsernames.add(clientHandler.getUsername());

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message, String sender, String recipient) {
        for (ClientHandler client : clients) {
            if (!client.getUsername().equals(sender) && client.getUsername().equals(recipient)) {
                client.sendMessage(message);
            }
        }
    }

    void sendUserList() {
        StringBuilder userList = new StringBuilder();
        for (ClientHandler client : clients) {
            userList.append(client.getUsername()).append(",");
        }
        String userListMessage = "/users " + userList.toString();

        for (ClientHandler client : clients) {
            client.sendMessage(userListMessage);
        }
    }

    void sendPrivateMessage(String sender, String recipient, String message) {
        String recipientMessage = "from " + sender + " " + message;
        broadcastMessage(recipientMessage, sender, recipient);
    }

    boolean isUsernameTaken(String username) {
        return takenUsernames.contains(username);
    }

    void removeClient(ClientHandler client) {
        clients.remove(client);
        takenUsernames.remove(client.getUsername());
        sendUserList();
        client.sendMessage("/exit");
    }

    public static void main(String[] args) {
        int port = 1234;

        NewServer server = new NewServer(port);
    }
}