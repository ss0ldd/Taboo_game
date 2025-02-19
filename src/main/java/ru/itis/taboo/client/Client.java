package ru.itis.taboo.client;

import ru.itis.taboo.common.ChatMessage;
import ru.itis.taboo.common.Message;
import ru.itis.taboo.common.WordMessage;
import ru.itis.taboo.common.GameStateMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean running = true; // Флаг для управления потоком
    private String username;

    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Подключение к серверу успешно!");

            listenForMessages(); // Запуск потока для чтения сообщений
            startConsoleInterface(); // Запуск консольного интерфейса
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (running) { // Проверяем флаг
                    Message message = (Message) in.readObject();
                    handleMessage(message); // Обработка сообщения
                }
            } catch (Exception e) {
                if (running) { // Выводим ошибку только если поток не был завершен корректно
                    System.out.println("Соединение с сервером разорвано.");
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void handleMessage(Message message) {
        if (message instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage) message;
            System.out.println(chatMessage.getPlayerName() + ": " + chatMessage.getMessage());
        } else if (message instanceof WordMessage) {
            WordMessage wordMessage = (WordMessage) message;
            System.out.println("Новое слово: " + wordMessage.getWord());
            System.out.println("Табу слова: " + wordMessage.getTabooWords());
        } else if (message instanceof GameStateMessage) {
            GameStateMessage gameStateMessage = (GameStateMessage) message;
            System.out.println("Текущий игрок: " + gameStateMessage.getCurrentPlayer());
            System.out.println("Игра завершена: " + gameStateMessage.isGameOver());
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeObject(new ChatMessage(username, message));
            out.flush(); // Сбросить буфер
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startConsoleInterface() {
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            System.out.println("Введите ваше имя:");
            this.username = scanner.nextLine();

            while (true) {
                System.out.println("Введите сообщение (или 'exit' для выхода):");
                String input = scanner.nextLine();

                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }

                sendMessage(input); // Отправка сообщения на сервер
            }
        } finally {
            disconnect(); // Закрытие соединения при выходе
        }
    }

    public void disconnect() {
        running = false; // Останавливаем поток
        try {
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
            System.out.println("Соединение с сервером закрыто.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Client client = new Client();
        client.connect("localhost", 12345); // Подключение к серверу
    }
}
