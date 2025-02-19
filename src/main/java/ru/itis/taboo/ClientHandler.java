package ru.itis.taboo;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    protected static List<ClientHandler> clientHandlers = new ArrayList<>(); // Список всех подключенных клиентов
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName; // Имя клиента

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            synchronized (clientHandlers) {
                clientHandlers.add(this); // Добавляем клиента в список при подключении
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            synchronized (clientHandlers) {
                clientHandlers.remove(this); // Убираем клиента из списка при отключении
            }
        }
    }

    public void handleMessage(String message) {
        // Разделяем сообщение на тип и содержание
        String[] parts = message.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid message format");
        }

        String messageType = parts[0];
        String messageContent = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

        try {
            MessageProtocol protocol = MessageProtocol.valueOf(messageType);
            switch (protocol) {
                case CHAT:
                    // Обрабатываем чат-сообщение
                    broadcastMessage(clientName + ": " + messageContent);
                    break;
                case GUESS:
                    // Обрабатываем угадывание (можно добавить логику угадывания)
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message: " + messageType);
            }
        } catch (IllegalArgumentException e) {
            // Логируем ошибку, если тип сообщения неизвестен
            System.out.println("Unknown message type: " + messageType);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public static void broadcastMessage(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(message);
            }
        }
    }

    public static void startGame() {
        // Назначаем ведущего случайным образом (например, первый подключенный игрок)
        if (!clientHandlers.isEmpty()) {
            ClientHandler currentHost = clientHandlers.get(new Random().nextInt(clientHandlers.size()));
            currentHost.sendMessage("You are the host! Your word to describe is: java");

            // Уведомляем всех игроков
            for (ClientHandler handler : clientHandlers) {
                if (handler != currentHost) {
                    handler.sendMessage("A new game has started! The host is preparing to describe a word.");
                }
            }
        }
    }
}
