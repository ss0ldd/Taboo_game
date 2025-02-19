package ru.itis.taboo;

import lombok.Getter;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {
    protected static List<ClientHandler> clientHandlers = new ArrayList<>(); // Список всех подключенных клиентов
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName; // Имя клиента
    private static final List<String[]> words = GameUtil.loadWordsFromFile("src/main/resources/words.txt");
    private static String[] currentWord; // Текущее слово для объяснения
    private static String[] tabooWords; // Табу-слова для ведущего
    private static ClientHandler currentHost; // Текущий ведущий

    private ScheduledExecutorService timerService; // Сервис для управления таймером
    private ScheduledFuture<?> timerFuture; // Ссылка на задачу таймера


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
        String[] parts = message.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid message format");
        }

        String messageType = parts[0];
        String messageContent = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

        try {
            MessageProtocol protocol = MessageProtocol.valueOf(messageType);
            System.out.println(messageType);
            switch (protocol) {
                case NAME:
                    this.clientName = messageContent;
                    break;
                case CHAT:
                    if (currentHost == this) {
                        // Если ведущий пытается написать сообщение
                        if (Arrays.asList(tabooWords).contains(messageContent.toLowerCase())) {
                            sendMessage("You cannot use taboo words!");
                        } else {
                            // Ведущий может писать только если это не табу-слово
                            sendMessage("As the host, you cannot send chat messages.");
                        }
                    } else {
                        // Если это не ведущий, проверяем на угаданные слова
                        handleGuess(messageContent);
                    }
                    break;
                case GUESS:
                    handleGuess(messageContent); // Если игрок угадал слово
                    break;
                case START:
                    startGame(); // Начать игру
                    break;
                case WORD:
                    handleHostWord(messageContent); // Если ведущий называет слово
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message: " + messageType);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown message type: " + messageType);
        }
    }

    public void handleGuess(String guessedWord) {
        // Если игрок угадал слово
        if (guessedWord.equalsIgnoreCase(currentWord[0])) {
            sendMessage("Congratulations! You guessed the word: " + currentWord[0]);
            // Уведомление всем игрокам, что слово угадано
            ClientHandler.broadcastMessage(this.clientName, "Guessed word: " + currentWord[0]);
            // Игра заканчивается, игрок выиграл
            endGame(true);
            stopTimer(); // Останавливаем таймер
        } else {
            sendMessage("Wrong guess, try again.");
        }
    }

    public void handleHostWord(String word) {
        // Проверка, не назвал ли ведущий слово, которое ему выпало
        if (word.equalsIgnoreCase(currentWord[0])) {
            sendMessage("You named the word! You lose.");
            // Игра завершается, ведущий проиграл
            endGame(false);
        } else if (Arrays.asList(tabooWords).contains(word.toLowerCase())) {
            sendMessage("You named a taboo word! You lose.");
            // Игра завершается, ведущий проиграл
            endGame(false);
        } else {
            sendMessage("You described the word: " + word);
        }
    }

    private String formatMessage(String senderName, String messageContent) {
        // Если отправитель — текущий клиент, показываем "Вы"
        if (senderName.equals(this.clientName)) {
            return "Вы: " + messageContent;
        } else {
            // Иначе показываем имя отправителя
            return senderName + ": " + messageContent;
        }
    }


    public void sendMessage(String message) {
        out.println(message);
    }

    public static void broadcastMessage(String senderName, String messageContent) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                // Каждый клиент форматирует сообщение самостоятельно
                String formattedMessage = handler.formatMessage(senderName, messageContent);
                handler.sendMessage(formattedMessage);
            }
        }
    }

    public static void startGame() {
        if (!clientHandlers.isEmpty()) {
            // Назначаем ведущего случайным образом
            currentHost = clientHandlers.get(new Random().nextInt(clientHandlers.size()));

            // Получаем случайное слово и табу-слова
            String[] selectedWord = GameUtil.getRandomWord(words);
            String wordToDescribe = selectedWord[0];
            tabooWords = Arrays.copyOfRange(selectedWord, 1, selectedWord.length);
            currentWord = selectedWord;

            // Отправляем ведущему слово и табу-слова
            currentHost.sendMessage("You are the host! Your word to describe is: " + wordToDescribe);
            currentHost.sendMessage("Taboo words: " + String.join(", ", tabooWords));

            // Уведомляем всех игроков
            for (ClientHandler handler : clientHandlers) {
                if (handler != currentHost) {
                    handler.sendMessage("A new game has started! The host is preparing to describe a word.");
                }
            }

            // Запускаем таймер на 1 минуту
            currentHost.startTimer();
        }
    }

    private void startTimer() {
        timerService = Executors.newSingleThreadScheduledExecutor();
        timerFuture = timerService.schedule(() -> {
            // Время истекло, ведущий выигрывает
            endGame(false);
        }, 1, TimeUnit.MINUTES);
    }

    private void stopTimer() {
        if (timerFuture != null) {
            timerFuture.cancel(false); // Останавливаем таймер
        }
        if (timerService != null) {
            timerService.shutdown(); // Завершаем сервис
        }
    }

    private void resetGame() {
        currentWord = null;
        tabooWords = null;
        currentHost = null;
    }

    private void endGame(boolean isPlayerWin) {
        if (isPlayerWin) {
            // Если игрок выиграл, отправляем всем сообщение о победе
            ClientHandler.broadcastMessage(this.clientName, "Congratulations! " + this.clientName + " won the game!");
            // Ведущему отправляем сообщение о том, что он проиграл
            currentHost.sendMessage("Game over. You lost the game!");
        } else {
            // Если ведущий проиграл
            ClientHandler.broadcastMessage(this.clientName, "Game over. " + this.clientName + " lost the game!");
            // Ведущему отправляем сообщение о том, что он выиграл
            currentHost.sendMessage("Congratulations! You won the game!");
        }

        // Сброс состояния игры для следующего раунда
        resetGame();
        stopTimer();  // Останавливаем таймер
    }

}