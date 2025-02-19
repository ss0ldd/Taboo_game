package ru.itis.taboo;

import java.util.*;
import java.io.*;

public class GameUtil {
    // Загрузка слов из файла
    public static List<String[]> loadWordsFromFile(String fileName) {
        List<String[]> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(","); // Разделяем строку на слова по запятой
                words.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    // Рандомный выбор слова
    public static String[] getRandomWord(List<String[]> words) {
        Random random = new Random();
        return words.get(random.nextInt(words.size())); // Возвращаем рандомное слово
    }
}
