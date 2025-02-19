package ru.itis.taboo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.io.*;
import java.util.*;

public class GameUtil {
    // Загрузка слов из файла
    public static List<String[]> loadWordsFromFile(String fileName) {
        List<String[]> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Разделяем строку на слова по запятой
                String[] parts = line.split(",");
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
        return words.get(random.nextInt(words.size()));
    }
}
