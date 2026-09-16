package app.service;

import app.model.FunctionItem;
import app.model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public final class ChebyshevLagrangeInterpolator {
    private ChebyshevLagrangeInterpolator() {
    }

    public static double interpolate(FunctionItem function, double left, double right, int count, double x) {
        List<Point> nodes = buildNodes(function, left, right, count);
        double result = 0.0;
        for (int i = 0; i < nodes.size(); i++) {
            double term = nodes.get(i).y();
            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) {
                    term *= (x - nodes.get(j).x()) / (nodes.get(i).x() - nodes.get(j).x());
                }
            }
            result += term;
        }
        return result;
    }

    private static List<Point> buildNodes(FunctionItem function, double left, double right, int count) {
        if (function == null) {
            throw new IllegalArgumentException("Функция не выбрана.");
        }
        if (count < 2) {
            throw new IllegalArgumentException("Количество узлов должно быть не меньше 2.");
        }
        if (Double.compare(left, right) == 0) {
            throw new IllegalArgumentException("Границы интервала не должны совпадать.");
        }
        double intervalCenter = (left + right) / 2.0;
        double intervalRadius = (right - left) / 2.0;
        DoubleUnaryOperator source = function.function();
        List<Point> nodes = new ArrayList<>(count);
        for (int k = 0; k < count; k++) {
            double angle = (2.0 * k + 1.0) * Math.PI / (2.0 * count);
            double x = intervalCenter + intervalRadius * Math.cos(angle);
            nodes.add(new Point(x, source.applyAsDouble(x)));
        }
        return nodes;
    }
}
