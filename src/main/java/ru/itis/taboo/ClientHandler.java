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
    static boolean isGameIsOver = false;

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
            System.out.println("Клиент отключился");
            // Уведомление всех игроков, что клиент отключился
            ClientHandler.broadcastMessage("taboo_bot", "Клиент отключился. Игра окончена.");
            // Если остался только один игрок, объявляем его победителем
            if (ClientHandler.clientHandlers.size() == 1) {
                ClientHandler.broadcastMessage("taboo_bot", "Вы выиграли игру!");
            }
        } finally {
            synchronized (clientHandlers) {
                clientHandlers.remove(this); // Убираем клиента из списка при отключении
            }
        }
    }

    public void handleMessage(String message) {
        try {
            String[] parts = message.split(" ");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Неверный формат сообщения");
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
                                sendMessage("Вы не можете использовать табу-слова!");
                            } else {
                                // Ведущий может писать сообщения, если это не табу-слово
                                ClientHandler.broadcastMessage(this.clientName, messageContent);
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
                    default:
                        throw new IllegalArgumentException("Неизвестное сообщение: " + messageType);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Неизвестный тип сообщения: " + messageType);
            }
        } catch (Exception e) {
            System.out.println("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    public void handleGuess(String guessedWord) {
        // Если игрок угадал слово
        if (guessedWord.equalsIgnoreCase(currentWord[0])) {
            sendMessage("Поздравляем! Вы угадали слово: " + currentWord[0]);
            // Уведомление всем игрокам, что слово угадано
            ClientHandler.broadcastMessage(this.clientName, "Угаданное слово: " + currentWord[0]);
            // Игра заканчивается, игрок выиграл
            endGame(true); // Завершаем игру, игрок выиграл
            isGameIsOver = false;
            stopTimer(); // Останавливаем таймер
        } else {
            ClientHandler.broadcastMessage(this.clientName, guessedWord);
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
        // Проверяем, достаточно ли игроков для начала игры
        if (clientHandlers.size() < 2) {
            broadcastMessage("taboo_bot", "Недостаточно игроков для начала игры. Необходимо как минимум 2 игрока.");
            return; // Прерываем выполнение метода, если игроков недостаточно
        }

        // Назначаем ведущего случайным образом
        currentHost = clientHandlers.get(new Random().nextInt(clientHandlers.size()));

        // Получаем случайное слово и табу-слова
        String[] selectedWord = GameUtil.getRandomWord(words);
        String wordToDescribe = selectedWord[0];
        tabooWords = Arrays.copyOfRange(selectedWord, 1, selectedWord.length);
        currentWord = selectedWord;

        // Отправляем ведущему слово и табу-слова
        currentHost.sendMessage("Вы ведущий! Слово, которое вы должны описать: " + wordToDescribe);
        currentHost.sendMessage("Табу слова: " + String.join(", ", tabooWords));

        // Уведомляем всех игроков
        for (ClientHandler handler : clientHandlers) {
            if (handler != currentHost) {
                handler.sendMessage("Новая игра началась! Ведущий должен описать слово.");
            }
        }
        isGameIsOver = true;
        // Запускаем таймер на 1 минуту
        currentHost.startTimer();
    }

    private void startTimer() {
        timerService = Executors.newSingleThreadScheduledExecutor();
        timerFuture = timerService.schedule(() -> {
            // Время истекло, ведущий выигрывает
            endGame(false);
            isGameIsOver = false;
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
        if (isGameIsOver) {
            for (ClientHandler handler : ClientHandler.clientHandlers) {
                if (isPlayerWin) {
                    handler.sendMessage("Игра окончена! " + this.clientName + " выиграл(а) игру! Ведущий проиграл.");
                } else {
                    handler.sendMessage("Игра окончена! Ведущий победил.");
                }
            }
        }

        // Сброс состояния игры для следующего раунда
        resetGame();
        stopTimer();  // Останавливаем таймер
    }
}