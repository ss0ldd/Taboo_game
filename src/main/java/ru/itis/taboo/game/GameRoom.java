package ru.itis.taboo.game;

import ru.itis.taboo.common.Message;
import ru.itis.taboo.common.WordMessage;
import ru.itis.taboo.server.ClientHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameRoom {
    private List<ClientHandler> players = new ArrayList<>();
    private String currentWord;
    private List<String> tabooWords;
    private boolean isGameStarted;
    private WordManager wordManager;

    public GameRoom(WordManager wordManager) {
        this.wordManager = wordManager;
    }

    public void addPlayer(ClientHandler player) {
        players.add(player);
    }

    public void startGame() {
        // Выбираем случайное слово и "табу" слова
        String[] wordData = wordManager.getRandomWord();
        currentWord = wordData[0];
        tabooWords = Arrays.asList(Arrays.copyOfRange(wordData, 1, wordData.length));

        // Отправляем слово ведущему игроку
        players.get(0).sendMessage(new WordMessage(currentWord, tabooWords));
    }

    public void broadcastMessage(Message message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }
}
