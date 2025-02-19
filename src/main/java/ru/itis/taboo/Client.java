package ru.itis.taboo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextArea chatArea;
    private JTextField inputField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Client().start();
        });
    }

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Запуск интерфейса
            createUI();
            listenForMessages();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createUI() {
        JFrame frame = new JFrame("Taboo Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        inputField = new JTextField();
        frame.add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(e -> sendMessage());

        frame.setVisible(true);
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals(MessageProtocol.NEW_WORD.name())) {
                        chatArea.append("A new word is being described!\n");
                    } else {
                        chatArea.append(message + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();

        if (!message.isEmpty()) {
            if (message.startsWith("/guess ")) {
                out.println(MessageProtocol.GUESS.name() + " " + message.substring(7));
            } else if (message.startsWith("/chat ")) {
                out.println(MessageProtocol.CHAT.name() + " " + message.substring(6));
            }
            inputField.setText("");  // Очищаем поле ввода после отправки
        }
    }

}
