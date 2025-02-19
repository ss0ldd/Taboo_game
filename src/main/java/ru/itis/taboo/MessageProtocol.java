package ru.itis.taboo;

public enum MessageProtocol {
    NEW_WORD,    // Сервер отправляет новое слово
    GUESS,       // Клиент делает попытку угадать слово
    CHAT,
    NAME,// Сообщения в чат
    GAME_OVER;   // Конец игры

    public static MessageProtocol fromString(String str) {
        switch (str) {
            case "NAME":
                return NAME;
            case "NEW_WORD":
                return NEW_WORD;
            case "GUESS":
                return GUESS;
            case "CHAT":
                return CHAT;
            case "GAME_OVER":
                return GAME_OVER;
            default:
                throw new IllegalArgumentException("Unknown message: " + str);
        }
    }
}

