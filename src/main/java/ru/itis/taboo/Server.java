package ru.itis.taboo;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clientHandlers = new ArrayList<>();
    protected static String wordToGuess = "java"; // Пример слова
    private static String[] tabooWords = {"programming", "language", "computer"}; // Пример табу
    private static final int MAX_PLAYERS = 2;
    private static ClientHandler currentHost = null;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();

                // Если подключилось нужное количество игроков, можно активировать кнопку "Start"
                if (clientHandlers.size() == MAX_PLAYERS) {
                    enableStartGameButton();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Функция для активации кнопки Start и начала игры
    private static void enableStartGameButton() {
        System.out.println("All players are connected. Game can start now!");

        // Уведомление всех игроков, что игра готова начать
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage("All players are connected. Press 'Start' to begin the game.");
        }
    }


    public static void broadcastMessage(String message) {
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(message);
        }
    }

    // Метод для старта игры при нажатии кнопки Start
    public static void startGame() {
        // Назначаем ведущего случайным образом
        Random rand = new Random();
        currentHost = clientHandlers.get(rand.nextInt(clientHandlers.size()));
        currentHost.sendMessage("You are the host! Your word to describe is: " + wordToGuess);

        // Уведомляем всех игроков
        for (ClientHandler handler : clientHandlers) {
            if (handler != currentHost) {
                handler.sendMessage("A new game has started! The host is preparing to describe a word.");
            }
        }
    }

}
