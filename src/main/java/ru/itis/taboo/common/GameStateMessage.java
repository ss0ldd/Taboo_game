package ru.itis.taboo.common;
import lombok.*;

@Getter
@Setter

public class GameStateMessage extends Message {
    private String currentPlayer; // Игрок, который сейчас описывает слово
    private boolean isGameOver;   // Закончена ли игра

    public GameStateMessage(String currentPlayer, boolean isGameOver) {
        this.currentPlayer = currentPlayer;
        this.isGameOver = isGameOver;
        setType("GAME_STATE"); // Указываем тип сообщения
    }
}
