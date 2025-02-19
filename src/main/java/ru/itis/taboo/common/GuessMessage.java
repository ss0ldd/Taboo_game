package ru.itis.taboo.common;

import lombok.*;

@Getter
@Setter

public class GuessMessage extends Message {
    private String playerName; // Имя игрока
    private String guess;      // Предположение

    public GuessMessage(String playerName, String guess) {
        this.playerName = playerName;
        this.guess = guess;
        setType("GUESS"); // Указываем тип сообщения
    }
}
