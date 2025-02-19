package ru.itis.taboo;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clientHandlers = new ArrayList<>();
    protected static String wordToGuess = "java"; // Пример слова
    private static String[] tabooWords = {"programming", "language", "computer"}; // Пример табу

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(String message) {
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(message);
        }
    }

    public static void startGame() {
        // Начало игры: отправка слова для описания и табу слов
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(MessageProtocol.NEW_WORD.name());
            handler.sendMessage(wordToGuess);
            handler.sendMessage("Taboo words: " + Arrays.toString(tabooWords));
        }
    }
}
