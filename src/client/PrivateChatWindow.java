package client;
import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class PrivateChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private boolean hasNewMessages;
    private String recipient;

    public PrivateChatWindow(PrintWriter writer, String recipient) {
        setTitle("Private Chat with " + recipient);
        setSize(400, 300);
        this.recipient = recipient;

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        inputField = new JTextField();
        inputPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String message = inputField.getText();
            writer.println("/private " + recipient + " " + message);
            System.out.println(recipient + " fgggf");
            addMessageFromClient("You: " + message);
            inputField.setText("");
        });
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(false);
    }

    public void addMessageFromClient(String message) {
        addMessageToChatArea(message);
    }

    public void addMessageToChatArea(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    public void setNotification(boolean hasNewMessages) {
        this.hasNewMessages = hasNewMessages;
        if (hasNewMessages) {
            setTitle("Private Chat with " + this.recipient + " (New Messages)");
        } else {
            setTitle("Private Chat with " + this.recipient);
        }
    }
}