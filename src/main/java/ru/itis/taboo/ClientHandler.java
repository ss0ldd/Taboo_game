package ru.itis.taboo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {
    protected static List<ClientHandler> clientHandlers = new ArrayList<>(); //список всех подключенных клиентов
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private static final List<String[]> words = GameUtil.loadWordsFromFile("src/main/resources/words.txt");
    private static String[] currentWord;// слово для объяснения
    private static String[] tabooWords; // табу слова
    private static ClientHandler currentHost;
    static boolean isGameIsOver = false;

    private ScheduledExecutorService timerService; //сервис для того чтобы управлять таймером
    private ScheduledFuture<?> timerFuture; // ссылка на задачу таймера

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            synchronized (clientHandlers) {
                clientHandlers.add(this);
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
            ClientHandler.broadcastMessage("taboo_bot", "Клиент отключился. Игра окончена.\uD83D\uDC40");
            if (ClientHandler.clientHandlers.size() == 1) {
                ClientHandler.broadcastMessage("taboo_bot", "Вы выиграли игру!\uD83C\uDF89");
            }
        } finally {
            synchronized (clientHandlers) {
                clientHandlers.remove(this); //удаляем клиента из списка
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
                            if (Arrays.asList(tabooWords).contains(messageContent.toLowerCase())) {
                                sendMessage("Вы не можете использовать табу-слова!❌");
                            } else if (messageContent.equalsIgnoreCase(currentWord[0])) {
                                sendMessage("Вы не можете назвать слово, которое вам выпало!❌");
                            } else {
                                ClientHandler.broadcastMessage(this.clientName, messageContent);
                            }
                        } else {
                            handleGuess(messageContent);
                        }
                        break;
                    case START:
                        startGame();
                        break;
                    default:
                        throw new IllegalArgumentException("неизвестное сообщение: " + messageType);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("неизвестный тип сообщения: " + messageType);
            }
        } catch (Exception e) {
            System.out.println("ошибка обработки сообщения: " + e.getMessage());
        }
    }

    public void handleGuess(String guessedWord) {
        if (guessedWord.equalsIgnoreCase(currentWord[0])) {// Если игрок угадал слово
            sendMessage("Поздравляем!\uD83C\uDF89 Вы угадали слово✅: " + currentWord[0]);
            ClientHandler.broadcastMessage(this.clientName, "Угаданное слово: " + currentWord[0]);
            endGame(true);
            isGameIsOver = false;
            stopTimer();
        } else {
            ClientHandler.broadcastMessage(this.clientName, guessedWord);
        }
    }


    private String formatMessage(String senderName, String messageContent) {
        //если тот кто отправил сообщение, отправитель
        if (senderName.equals(this.clientName)) {
            return "ВЫ: " + messageContent;
        } else {
            return senderName + ": " + messageContent;
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public static void broadcastMessage(String senderName, String messageContent) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                String formattedMessage = handler.formatMessage(senderName, messageContent);
                handler.sendMessage(formattedMessage);
            }
        }
    }

    public static void startGame() {
        //проверяем хвтатает ли игроков
        if (clientHandlers.size() < 2) {
            broadcastMessage("taboo_bot", "Недостаточно игроков для начала игры. Необходимо как минимум 2 игрока.");
            return;
        }

        //ведущий выбирается случайно
        currentHost = clientHandlers.get(new Random().nextInt(clientHandlers.size()));
        //получаем случайное слово и табу слова
        String[] selectedWord = GameUtil.getRandomWord(words);
        String wordToDescribe = selectedWord[0];
        tabooWords = Arrays.copyOfRange(selectedWord, 1, selectedWord.length);
        currentWord = selectedWord;

        currentHost.sendMessage("Вы ведущий!⭐ Слово, которое вы должны описать: " + wordToDescribe);
        currentHost.sendMessage("Табу слова: " + String.join(", ", tabooWords));

        for (ClientHandler handler : clientHandlers) {
            if (handler != currentHost) {
                handler.sendMessage("Новая игра началась!⭐ Ведущий должен описать слово.⭐");
            }
        }
        isGameIsOver = true;

        currentHost.startTimer();
    }

    private void startTimer() {
        timerService = Executors.newSingleThreadScheduledExecutor();
        timerFuture = timerService.schedule(() -> {
            // время закончилось
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
                    handler.sendMessage("Игра окончена! " + this.clientName + " выиграл(а) игру!⭐ Ведущий проиграл.\uD83D\uDE1E");
                } else {
                    handler.sendMessage("Игра окончена!✅ Ведущий победил.⭐");
                }
            }
        }
        //сброс состояния до след раунда
        resetGame();
        stopTimer();
    }
}