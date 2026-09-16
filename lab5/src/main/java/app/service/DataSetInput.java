package app.service;

import app.model.DataSet;
import app.model.FunctionItem;
import app.model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DataSetInput {
    private DataSetInput() {
    }

    public static DataSet fromText(String text, String description) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Введите точки в формате: x y");
        }
        List<Point> points = new ArrayList<>();
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String normalized = line.replace(',', '.');
            String[] parts = normalized.split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Строка " + (i + 1) + ": ожидается ровно два числа x и y.");
            }
            try {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                points.add(new Point(x, y));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Строка " + (i + 1) + ": не удалось распознать число.");
            }
        }
        return new DataSet(points, description, null);
    }

    public static DataSet fromFunction(FunctionItem function, double left, double right, int count) {
        if (function == null) {
            throw new IllegalArgumentException("Функция не выбрана.");
        }
        if (count < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не меньше 2.");
        }
        if (Double.compare(left, right) == 0) {
            throw new IllegalArgumentException("Границы интервала не должны совпадать.");
        }
        if (left > right) {
            throw new IllegalArgumentException("Левая граница интервала должна быть меньше правой.");
        }
        List<Point> points = new ArrayList<>();
        double step = (right - left) / (count - 1);
        for (int i = 0; i < count; i++) {
            double x = left + step * i;
            double y = function.function().applyAsDouble(x);
            points.add(new Point(x, y));
        }
        String description = String.format(Locale.US, "Функция %s на [%.4f, %.4f], n=%d", function.name(), left, right, count);
        return new DataSet(points, description, function);
    }
}
