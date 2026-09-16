package app.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataSet {
    private final List<Point> points;
    private final String sourceDescription;
    private final FunctionItem sourceFunction;

    public DataSet(List<Point> points, String sourceDescription, FunctionItem sourceFunction) {
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("Нужно не менее двух точек.");
        }
        List<Point> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingDouble(Point::x));
        validate(sorted);
        this.points = List.copyOf(sorted);
        this.sourceDescription = sourceDescription;
        this.sourceFunction = sourceFunction;
    }

    private void validate(List<Point> sorted) {
        for (int i = 1; i < sorted.size(); i++) {
            double prevX = sorted.get(i - 1).x();
            double currentX = sorted.get(i).x();
            if (Double.compare(prevX, currentX) == 0) {
                throw new IllegalArgumentException("Значения x должны быть уникальными.");
            }
        }
    }

    public List<Point> points() {
        return points;
    }

    public String sourceDescription() {
        return sourceDescription;
    }

    public FunctionItem sourceFunction() {
        return sourceFunction;
    }

    public double[] xValues() {
        double[] result = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            result[i] = points.get(i).x();
        }
        return result;
    }

    public double[] yValues() {
        double[] result = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            result[i] = points.get(i).y();
        }
        return result;
    }

    public boolean isUniform() {
        if (points.size() < 3) {
            return true;
        }
        double step = points.get(1).x() - points.get(0).x();
        for (int i = 2; i < points.size(); i++) {
            double currentStep = points.get(i).x() - points.get(i - 1).x();
            if (Math.abs(currentStep - step) > 1.0E-7) {
                return false;
            }
        }
        return true;
    }

    public double step() {
        if (points.size() < 2) {
            throw new IllegalStateException("Недостаточно точек для шага.");
        }
        return points.get(1).x() - points.get(0).x();
    }

    public double minX() {
        return points.get(0).x();
    }

    public double maxX() {
        return points.get(points.size() - 1).x();
    }

    public double minY() {
        return points.stream().mapToDouble(Point::y).min().orElse(0.0);
    }

    public double maxY() {
        return points.stream().mapToDouble(Point::y).max().orElse(0.0);
    }
}
