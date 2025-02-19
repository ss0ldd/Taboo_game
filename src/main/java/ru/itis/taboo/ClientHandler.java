package ru.itis.taboo;
import java.io.*;
import java.net.*;

import static ru.itis.taboo.Server.broadcastMessage;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
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
        }
    }

    public void handleMessage(String message) {
        // Разделяем сообщение на тип и текст
        String[] parts = message.split(" ", 2); // Разделяем на 2 части: тип и сообщение
        if (parts.length < 2) {
            // Если нет сообщения, выкидываем исключение
            throw new IllegalArgumentException("Invalid message format");
        }

        String messageType = parts[0]; // Тип сообщения
        String messageContent = parts[1]; // Содержание сообщения

        // Обрабатываем сообщение в зависимости от его типа
        try {
            MessageProtocol protocol = MessageProtocol.valueOf(messageType); // Преобразуем тип в enum
            switch (protocol) {
                case CHAT:
                    // Обрабатываем чат-сообщение
                    broadcastMessage("Chat: " + messageContent);
                    break;
                case GUESS:
                    // Обрабатываем угадывание
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
}
