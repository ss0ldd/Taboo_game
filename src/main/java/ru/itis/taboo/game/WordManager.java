package ru.itis.taboo.game;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordManager {
    private List<String[]> words = new ArrayList<>();

    public WordManager(String filePath) {
        loadWords(filePath); // Загружаем слова из файла
    }

    private void loadWords(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                words.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getRandomWord() {
        if (words.isEmpty()) {
            return new String[]{"слово", "табу1", "табу2", "табу3"}; // Заглушка, если список пуст
        }
        return words.get(new Random().nextInt(words.size()));
    }
}
