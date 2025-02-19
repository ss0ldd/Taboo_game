package ru.itis.taboo.server;

import ru.itis.taboo.common.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            // Обработка входящих сообщений
            while (true) {
                Message message = (Message) in.readObject();
                System.out.println("Получено сообщение: " + message.getType());
                handleMessage(message); // Обработка сообщения
            }
        } catch (Exception e) {
            System.out.println("Клиент отключен1: " + clientSocket.getInetAddress());
        } finally {
            disconnect(); // Закрытие соединения
        }
    }

    private void handleMessage(Message message) {
        if (message instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage) message;
            System.out.println("Чат: " + chatMessage.getPlayerName() + " -> " + chatMessage.getMessage());
            broadcastMessage(chatMessage); // Пересылаем сообщение всем клиентам
        } else if (message instanceof GuessMessage) {
            GuessMessage guessMessage = (GuessMessage) message;
            System.out.println("Игрок " + guessMessage.getPlayerName() + " предположил: " + guessMessage.getGuess());
            checkGuess(guessMessage); // Проверяем предположение
        } else if (message instanceof WordMessage) {
            WordMessage wordMessage = (WordMessage) message;
            System.out.println("Новое слово: " + wordMessage.getWord());
            updateCurrentWord(wordMessage); // Обновляем текущее слово
        }
    }

    private void broadcastMessage(Message message) {
        for (ClientHandler client : Server.getConnectedClients()) {
            try {
                client.out.writeObject(message);
                client.out.flush();
            } catch (Exception e) {
                System.out.println("Ошибка при отправке сообщения клиенту: " + e.getMessage());
            }
        }
    }

    private void checkGuess(GuessMessage guessMessage) {
        String currentWord = Server.getCurrentWord();
        if (guessMessage.getGuess().equalsIgnoreCase(currentWord)) {
            System.out.println("Игрок " + guessMessage.getPlayerName() + " угадал слово!");
            broadcastMessage(new GameStateMessage(guessMessage.getPlayerName(), true));
        } else {
            System.out.println("Игрок " + guessMessage.getPlayerName() + " не угадал.");
        }
    }

    private void updateCurrentWord(WordMessage wordMessage) {
        Server.setCurrentWord(wordMessage.getWord());
        broadcastMessage(wordMessage);
    }

    private void disconnect() {
        try {
            if (clientSocket != null) clientSocket.close();
            if (out != null) out.close();
            if (in != null) in.close();
            System.out.println("Клиент отключен: " + clientSocket.getInetAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
