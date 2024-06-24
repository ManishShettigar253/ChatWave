import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

class ServerChatThread extends Thread {
    private JTextArea chatArea;
    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;

    public ServerChatThread(JTextArea chatArea) {
        this.chatArea = chatArea;
    }

    public void run() {
        try {
            socket = new DatagramSocket(9876);
            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                clientAddress = receivePacket.getAddress();
                clientPort = receivePacket.getPort();
                
                SwingUtilities.invokeLater(() -> chatArea.append("Them: " + clientMessage + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        chatArea.append("You: " + message + "\n");
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class ChatServer {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private ServerChatThread serverThread;

    public ChatServer() {
        frame = new JFrame("User 1");
        chatArea = new JTextArea(20, 50);
        inputField = new JTextField(50);

        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                inputField.setText("");
                serverThread.sendMessage(message);
            }
        });

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(inputField, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        serverThread = new ServerChatThread(chatArea);
        serverThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ChatServer();
            }
        });
    }
}
