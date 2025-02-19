package ru.itis.taboo;

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
    private static String clientName;// Флаг для отслеживания состояния игры

    public static void main(String[] args) {
        try {
            clientName = JOptionPane.showInputDialog("Напишите ваше имя");

            if (clientName == null || clientName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Имя не может быть пустым. Закрытие приложения.");
                System.exit(0);
                return;
            }

            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("NAME " + clientName);
            // Интерфейс
            JFrame frame = new JFrame("Game Taboo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(960, 540);

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

            startButton = new JButton("Начать игру");
            startButton.setEnabled(false);
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    out.println("START game started!");  // Сообщаем серверу, что нужно начать игру
                }
            });

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(inputField, BorderLayout.SOUTH);
            panel.add(startButton, BorderLayout.NORTH);

            frame.add(panel, BorderLayout.CENTER);

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
        if (message.equals("taboo_bot: Все игроки присоединились, нажмите кнопку старт.")) {
            chatArea.append(message + "\n");
            startButton.setEnabled(true);  // Включаем кнопку старта
        } else if (message.startsWith("Вы ведущий!")) {
            chatArea.append(message + "\n");
        } else if (message.startsWith("Новая игра началась!")) {
            chatArea.append(message + "\n");
        } else {
            chatArea.append(message + "\n");
        }
    }
}