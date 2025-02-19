package ru.itis.taboo;

import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 2;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новый клиент подключен");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();

                if (ClientHandler.clientHandlers.size() == MAX_PLAYERS) { // Когда подключится нужное количество игроков, подсвечиваем кнопку старта игры
                    enableStartGameButton();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enableStartGameButton() {
        System.out.println("Все игроки подключены. Игра может начаться!");
        ClientHandler.broadcastMessage("taboo_bot", "Все игроки присоединились, нажмите кнопку старт."); // Уведомление всех игроков, что игра готова начать
    }

    // Метод для старта игры при нажатии кнопки Start
    public static void startGame() {
        ClientHandler.startGame(); // Передаем управление ClientHandler для старта игры
    }
}