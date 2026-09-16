package app.service;

import app.model.DataSet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class FileInput {
    private FileInput() {
    }

    public static String readText(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (NoSuchFileException exception) {
            throw new IllegalArgumentException("Файл не найден: " + path.getFileName() + ".");
        } catch (AccessDeniedException exception) {
            throw new IllegalArgumentException("Нет доступа на чтение файла: " + path.getFileName() + ".");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Не удалось прочитать файл: " + path.getFileName() + ".");
        }
    }

    public static DataSet readDataSet(Path path) {
        String text = readText(path);
        try {
            return DataSetInput.fromText(text, "Файл: " + path.getFileName());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Не удалось разобрать файл \"" + path.getFileName() + "\": " + exception.getMessage());
        }
    }
}
