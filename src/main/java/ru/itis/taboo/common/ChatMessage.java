package ru.itis.taboo.common;

import lombok.*;

@Getter
@Setter

public class ChatMessage extends Message {
    private String playerName; // Имя игрока
    private String message;   // Текст сообщения

    public ChatMessage(String playerName, String message) {
        this.playerName = playerName;
        this.message = message;
        setType("CHAT"); // Указываем тип сообщения
    }
}

