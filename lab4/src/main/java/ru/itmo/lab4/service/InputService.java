package ru.itmo.lab4.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.MalformedInputException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ru.itmo.lab4.model.Point;

public class InputService {
    public List<Point> parseText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Файл пуст или не содержит данных.");
        }
        List<Point> points = new ArrayList<>();
        String[] lines = text.trim().split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+|;");
            if (parts.length != 2) {
                throw new IllegalArgumentException(parts.length < 2
                        ? "В строке " + (i + 1) + " не хватает значения x или y."
                        : "В строке " + (i + 1) + " слишком много значений. Ожидаются только x и y.");
            }
            points.add(new Point(parseDouble(parts[0], i + 1, "x"), parseDouble(parts[1], i + 1, "y")));
        }
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Файл пуст или не содержит корректных строк с точками.");
        }
        return points;
    }

    public List<Point> readFromFile(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Путь к файлу не указан.");
        }
        if (!Files.exists(path)) {
            throw new NoSuchFileException(path.toString());
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Указанный путь не является файлом.");
        }
        if (!Files.isReadable(path)) {
            throw new AccessDeniedException(path.toString());
        }
        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            return parseText(text);
        } catch (MalformedInputException ex) {
            throw new IllegalArgumentException("Файл содержит неподдерживаемую кодировку или повреждённые данные.");
        }
    }

    public String toText(List<Point> points) {
        StringBuilder builder = new StringBuilder();
        for (Point point : points) {
            builder.append(point.x()).append(' ').append(point.y()).append('\n');
        }
        return builder.toString();
    }

    private double parseDouble(String raw, int lineNumber, String coordinateName) {
        try {
            return Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "В строке " + lineNumber + " значение " + coordinateName + " не является числом: " + raw
            );
        }
    }
}
