package ru.itis.taboo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static JTextArea chatArea;
    private static JTextField inputField;
    private static JButton startButton;
    private static String clientName;

    public static void main(String[] args) {
        try {
            clientName = JOptionPane.showInputDialog("Enter your name");

            if (clientName == null || clientName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Name cannot be empty. Closing application.");
                System.exit(0);  // Закрыть приложение, если имя не введено
                return;  // Завершаем выполнение метода
            }

            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Интерфейс
            JFrame frame = new JFrame("Taboo Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            chatArea = new JTextArea();
            chatArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(chatArea);

            inputField = new JTextField();
            inputField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage(inputField.getText());
                }
            });

            startButton = new JButton("Start Game");
            startButton.setEnabled(false); // Кнопка недоступна, пока не подключатся все игроки
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    out.println("/start");  // Сообщаем серверу, что нужно начать игру
                }
            });

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(inputField, BorderLayout.SOUTH);
            panel.add(startButton, BorderLayout.NORTH);

            frame.add(panel);
            frame.setVisible(true);

            // Чтение сообщений от сервера
            String message;
            while ((message = in.readLine()) != null) {
                handleServerMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String message) {
        if (!message.isEmpty()) {
            out.println("CHAT " + message);  // Отправляем чат-сообщение
            inputField.setText("");  // Очищаем поле ввода
        }
    }

    private static void handleServerMessage(String message) {
        // Обработка сообщений от сервера
        if (message.equals("All players are connected. Press 'Start' to begin the game.")) {
            startButton.setEnabled(true);  // Включаем кнопку старта
        } else if (message.startsWith("You are the host!")) {
            // Это сообщение для ведущего
            chatArea.append("You are the host. Your word is: " + message.substring(19) + "\n");
        } else if (message.startsWith("A new game has started!")) {
            chatArea.append(message + "\n");
        } else {
            chatArea.append(message + "\n");
        }
    }
}
