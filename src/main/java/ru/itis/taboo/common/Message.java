package ru.itis.taboo.common;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter

public abstract class Message implements Serializable {
    private String type; // Тип сообщения (например, "CHAT", "WORD", "GUESS")
}