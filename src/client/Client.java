package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends JFrame {
    private JTextField messageField;
    private JTextArea chatArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private Map<String, PrivateChatWindow> privateChatWindows;
    private final Object privateChatWindowsLock = new Object();

    public Client(String serverAddress, int serverPort) {
        privateChatWindows = new HashMap<>();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        messageField = new JTextField();

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScrollPane = new JScrollPane(userList);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScrollPane, userScrollPane);
        splitPane.setResizeWeight(0.8);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(messageField, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        add(panel);

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            boolean usernameSet = false;
            while (!usernameSet) {
                String serverMessage = reader.readLine();
                if (serverMessage.equals("Enter your username:")) {
                    username = JOptionPane.showInputDialog(this, "Enter your name:");
                    writer.println(username);
                } else if (serverMessage.equals("Username is already taken. Please enter another username.")) {
                    username = JOptionPane.showInputDialog(this, "Username is already taken. Please enter another username:");
                    writer.println(username);
                } else if (serverMessage.equals("Username set successfully.")) {
                    usernameSet = true;
                }
            }
            setTitle("Chat Client: " + username);
            Thread messageReceiver = new Thread(new MessageReceiver(reader,this));
            messageReceiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser != null && !selectedUser.equals(username)) {
                    openPrivateChat(selectedUser);
                }
            }
        });
    }

    void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }

    private void openPrivateChat(String recipient) {
        if (privateChatWindows.containsKey(recipient)) {
            PrivateChatWindow privateChatWindow = privateChatWindows.get(recipient);
            privateChatWindow.setVisible(true);
            privateChatWindow.requestFocus();
        } else {
            PrivateChatWindow privateChatWindow = new PrivateChatWindow(writer, recipient);
            privateChatWindows.put(recipient, privateChatWindow);
            privateChatWindow.setVisible(true);
        }
    }

    void handlePrivateCommand(String sender, String message) {
        synchronized (privateChatWindowsLock) {
            PrivateChatWindow privateChatWindow = privateChatWindows.get(sender);
            if (privateChatWindow != null) {
                privateChatWindow.addMessageToChatArea(sender + ": " + message);
                if (!privateChatWindow.isVisible()) {
                    privateChatWindow.setNotification(true);
                }
            } else {
                openPrivateChat(sender);
                privateChatWindow = privateChatWindows.get(sender);
                privateChatWindow.addMessageToChatArea(sender + ": " + message);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String serverAddress = "localhost";
            int serverPort = 1234;

            Client client1 = new Client(serverAddress, serverPort);
            client1.setVisible(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Client client2 = new Client(serverAddress, serverPort);
            client2.setVisible(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Client client3 = new Client(serverAddress, serverPort);
            client3.setVisible(true);
        });
    }
}