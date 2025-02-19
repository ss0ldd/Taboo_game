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
    private static JLabel timerLabel;
    private static JButton startButton;
    private static String clientName;// Флаг для отслеживания состояния игры
    private static Timer timer; // Таймер для отсчета времени
    private static int timeLeft = 60;

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

            timerLabel = new JLabel("Осталось времени: 60"); // Инициализация метки таймера
            timerLabel.setFont(new Font("Arial", Font.BOLD, 24));

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(inputField, BorderLayout.SOUTH);
            panel.add(startButton, BorderLayout.NORTH);
            panel.add(timerLabel, BorderLayout.EAST);

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
            startTimer();
        } else if (message.startsWith("Игра окончена!")) {
            chatArea.append(message + "\n");
            if (timer != null) { // Проверяем, инициализирован ли таймер
                timer.stop(); // Останавливаем таймер
                timer = null; // Сбрасываем таймер
            }
        } else {
            chatArea.append(message + "\n");
        }
    }

    private static void startTimer() {
        timeLeft = 60; // Сбрасываем таймер на 60 секунд
        timerLabel.setText("Осталось времени: " + timeLeft); // Обновляем метку таймера

        // Создаем таймер, который будет обновлять метку каждую секунду
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;

                timerLabel.setText("Осталось времени: " + timeLeft);
                if (timeLeft <= 0) {
                    timer.stop(); // Останавливаем таймер
                    out.println("TIME_UP"); // Сообщаем серверу, что время истекло
                }
            }
        });
        timer.start();
    }
}