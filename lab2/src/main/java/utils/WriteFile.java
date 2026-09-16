package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class WriteFile {
    public static void writeToFile(String filename, String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            writer.println(content);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось записать данные в файл.");
        } catch (SecurityException e) {
            throw new IllegalStateException("Нет прав доступа для записи в файл.");
        }
    }
}
