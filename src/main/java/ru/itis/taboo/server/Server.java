package ru.itis.taboo.server;

import lombok.Getter;
import ru.itis.taboo.game.GameRoom;
import ru.itis.taboo.game.WordManager;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<GameRoom> gameRooms = new ArrayList<>();
    private WordManager wordManager;
    // Список подключенных клиентов
    @Getter
    private static List<ClientHandler> connectedClients = new ArrayList<>();


    public Server() {
        this.wordManager = new WordManager("words.txt"); // Загружаем слова из файла
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket);

                // Создаем обработчик клиента
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                // Добавляем клиента в комнату (например, в первую комнату)
                if (gameRooms.isEmpty()) {
                    GameRoom newRoom = new GameRoom(wordManager);
                    gameRooms.add(newRoom);
                }
                GameRoom currentRoom = gameRooms.get(0);
                currentRoom.addPlayer(clientHandler);
                clientHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.start(12345); // Запуск сервера на порту 12345
    }

    // Метод для добавления клиента в список
    public static void addClient(ClientHandler client) {
        connectedClients.add(client);
        System.out.println("Клиент добавлен. Текущее количество клиентов: " + connectedClients.size());
    }

    // Метод для удаления клиента из списка
    public static void removeClient(ClientHandler client) {
        connectedClients.remove(client);
        System.out.println("Клиент удален. Текущее количество клиентов: " + connectedClients.size());
    }

    // Метод для получения текущего слова
    // Текущее слово в игре
    @Getter
    private static String currentWord;

    // Метод для обновления текущего слова
    public static void setCurrentWord(String word) {
        currentWord = word;
        System.out.println("Текущее слово обновлено: " + currentWord);
    }


}
