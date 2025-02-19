package ru.itis.taboo.common;

import java.util.List;
import lombok.*;

@Getter
@Setter

public class WordMessage extends Message {
    private String word; // Слово для описания
    private List<String> tabooWords; // Список "табу" слов

    public WordMessage(String word, List<String> tabooWords) {
        this.word = word;
        this.tabooWords = tabooWords;
        setType("WORD"); // Указываем тип сообщения
    }

}
