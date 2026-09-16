package ru.itmo.lab4.service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;

import ru.itmo.lab4.model.AnalysisReport;
import ru.itmo.lab4.model.ApproximationResult;
import ru.itmo.lab4.model.ModelFunction;
import ru.itmo.lab4.model.Point;

public class ApproximationService {
    private static final DecimalFormat DF = new DecimalFormat("0.000000", DecimalFormatSymbols.getInstance(Locale.US));

    public AnalysisReport analyze(List<Point> points) {
        validatePoints(points);
        List<ApproximationResult> results = new java.util.ArrayList<>();
        results.add(buildLinear(points));
        results.add(buildQuadratic(points));
        results.add(buildCubic(points));
        results.add(buildExponential(points));
        results.add(buildLogarithmic(points));
        results.add(buildPower(points));
        ApproximationResult best = results.stream()
                .filter(ApproximationResult::isValid)
                .min((a, b) -> Double.compare(a.getSigma(), b.getSigma()))
                .orElseThrow(() -> new IllegalStateException("Не удалось построить ни одну аппроксимирующую функцию."));
        return new AnalysisReport(points, results, best);
    }

    public String formatReport(AnalysisReport report) {
        StringBuilder builder = new StringBuilder();
        builder.append("Исходные данные:\n");
        builder.append(String.format("%-4s %-14s %-14s%n", "i", "x", "y"));
        for (int i = 0; i < report.points().size(); i++) {
            Point point = report.points().get(i);
            builder.append(String.format(Locale.US, "%-4d %-14.6f %-14.6f%n", i + 1, point.x(), point.y()));
        }
        for (ApproximationResult result : report.results()) {
            builder.append('\n');
            builder.append(result.getName()).append('\n');
            if (!result.isValid()) {
                builder.append("Не построена: ").append(result.getInvalidReason()).append('\n');
                continue;
            }
            builder.append("Формула: ").append(result.getFormula()).append('\n');
            builder.append("Коэффициенты:\n");
            double[] coefficients = result.getCoefficients();
            for (int i = 0; i < coefficients.length; i++) {
                builder.append("  ").append(coefficientName(i)).append(" = ").append(fmt(coefficients[i])).append('\n');
            }
            builder.append("S = ").append(fmt(result.getS())).append('\n');
            builder.append("Среднеквадратичное отклонение = ").append(fmt(result.getSigma())).append('\n');
            builder.append("R^2 = ").append(fmt(result.getR2())).append('\n');
            builder.append(result.getR2Message()).append('\n');
            if (result.getPearson() != null) {
                builder.append("Коэффициент корреляции Пирсона = ").append(fmt(result.getPearson())).append('\n');
            }
            builder.append("Массивы значений:\n");
            builder.append(String.format("%-4s %-14s %-14s %-14s %-14s%n", "i", "x", "y", "φ(x)", "ε"));
            for (int i = 0; i < report.points().size(); i++) {
                Point point = report.points().get(i);
                builder.append(String.format(
                        Locale.US,
                        "%-4d %-14.6f %-14.6f %-14.6f %-14.6f%n",
                        i + 1,
                        point.x(),
                        point.y(),
                        result.getPhi()[i],
                        result.getEpsilon()[i]
                ));
            }
        }

        builder.append("\nЛучшая аппроксимирующая функция:\n");
        builder.append(report.bestResult().getName()).append(": ").append(report.bestResult().getFormula()).append('\n');
        return builder.toString();
    }

    private void validatePoints(List<Point> points) {
        if (points.size() < 8 || points.size() > 12) {
            throw new IllegalArgumentException("Таблица должна содержать от 8 до 12 точек.");
        }
        for (Point point : points) {
            if (!Double.isFinite(point.x()) || !Double.isFinite(point.y())) {
                throw new IllegalArgumentException("Все значения x и y должны быть конечными.");
            }
        }
    }

    private ApproximationResult buildLinear(List<Point> points) {
        double[][] matrix = new double[2][3];
        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double sxy = 0.0;
        int n = points.size();
        for (Point p : points) {
            sx += p.x();
            sy += p.y();
            sxx += p.x() * p.x();
            sxy += p.x() * p.y();
        }
        matrix[0] = new double[]{n, sx, sy};
        matrix[1] = new double[]{sx, sxx, sxy};
        double[] coeffs = solve(matrix);
        ModelFunction model = x -> coeffs[0] + coeffs[1] * x;
        Double pearson = calcPearson(points, sx / n, sy / n);
        return finalizeResult("Линейная функция", coeffs, "φ(x) = " + fmt(coeffs[0]) + " + " + fmt(coeffs[1]) + " * x", model, points, pearson);
    }

    private ApproximationResult buildQuadratic(List<Point> points) {
        double sx = 0.0;
        double sx2 = 0.0;
        double sx3 = 0.0;
        double sx4 = 0.0;
        double sy = 0.0;
        double sxy = 0.0;
        double sx2y = 0.0;
        int n = points.size();
        for (Point p : points) {
            double x2 = p.x() * p.x();
            sx += p.x();
            sx2 += x2;
            sx3 += x2 * p.x();
            sx4 += x2 * x2;
            sy += p.y();
            sxy += p.x() * p.y();
            sx2y += x2 * p.y();
        }
        double[][] matrix = {
                {n, sx, sx2, sy},
                {sx, sx2, sx3, sxy},
                {sx2, sx3, sx4, sx2y}
        };
        double[] coeffs = solve(matrix);
        ModelFunction model = x -> coeffs[0] + coeffs[1] * x + coeffs[2] * x * x;
        return finalizeResult("Полином 2-й степени", coeffs, "φ(x) = " + fmt(coeffs[0]) + " + " + fmt(coeffs[1]) + " * x + " + fmt(coeffs[2]) + " * x^2", model, points, null);
    }

    private ApproximationResult buildCubic(List<Point> points) {
        double sx = 0.0;
        double sx2 = 0.0;
        double sx3 = 0.0;
        double sx4 = 0.0;
        double sx5 = 0.0;
        double sx6 = 0.0;
        double sy = 0.0;
        double sxy = 0.0;
        double sx2y = 0.0;
        double sx3y = 0.0;
        int n = points.size();
        for (Point p : points) {
            double x2 = p.x() * p.x();
            double x3 = x2 * p.x();
            sx += p.x();
            sx2 += x2;
            sx3 += x3;
            sx4 += x2 * x2;
            sx5 += x2 * x3;
            sx6 += x3 * x3;
            sy += p.y();
            sxy += p.x() * p.y();
            sx2y += x2 * p.y();
            sx3y += x3 * p.y();
        }
        double[][] matrix = {
                {n, sx, sx2, sx3, sy},
                {sx, sx2, sx3, sx4, sxy},
                {sx2, sx3, sx4, sx5, sx2y},
                {sx3, sx4, sx5, sx6, sx3y}
        };
        double[] coeffs = solve(matrix);
        ModelFunction model = x -> coeffs[0] + coeffs[1] * x + coeffs[2] * x * x + coeffs[3] * x * x * x;
        return finalizeResult("Полином 3-й степени", coeffs, "φ(x) = " + fmt(coeffs[0]) + " + " + fmt(coeffs[1]) + " * x + " + fmt(coeffs[2])+ " * x^2 + " + fmt(coeffs[3]) + " * x^3", model, points, null);
    }

    private ApproximationResult buildExponential(List<Point> points) {
        if (points.stream().anyMatch(p -> p.y() <= 0.0)) {
            return invalidResult("Экспоненциальная функция", "Экспоненциальная аппроксимация невозможна: все y должны быть > 0.");
        }
        List<Point> transformed = points.stream()
                .map(p -> new Point(p.x(), Math.log(p.y())))
                .toList();

        ApproximationResult linear = buildLinear(transformed);
        double a = Math.exp(linear.getCoefficients()[0]);
        double b = linear.getCoefficients()[1];
        ModelFunction model = x -> a * Math.exp(b * x);
        return finalizeResult("Экспоненциальная функция", new double[]{a, b}, "φ(x) = " + fmt(a) + " * e^(" + fmt(b) + " * x)", model, points, null);
    }

    private ApproximationResult buildLogarithmic(List<Point> points) {
        if (points.stream().anyMatch(p -> p.x() <= 0.0)) {
            return invalidResult("Логарифмическая функция", "Логарифмическая аппроксимация невозможна: все x должны быть > 0.");
        }

        List<Point> transformed = points.stream()
                .map(p -> new Point(Math.log(p.x()), p.y()))
                .toList();
        ApproximationResult linear = buildLinear(transformed);
        double a = linear.getCoefficients()[0];
        double b = linear.getCoefficients()[1];
        ModelFunction model = x -> a + b * Math.log(x);
        return finalizeResult("Логарифмическая функция", new double[]{a, b}, "φ(x) = " + fmt(a) + " + " + fmt(b) + " * ln(x)", model, points, null);
    }

    private ApproximationResult buildPower(List<Point> points) {
        if (points.stream().anyMatch(p -> p.x() <= 0.0 || p.y() <= 0.0)) {
            return invalidResult("Степенная функция", "Степенная аппроксимация невозможна: все x и y должны быть > 0.");
        }
        List<Point> transformed = points.stream()
                .map(p -> new Point(Math.log(p.x()), Math.log(p.y())))
                .toList();
        ApproximationResult linear = buildLinear(transformed);
        double a = Math.exp(linear.getCoefficients()[0]);
        double b = linear.getCoefficients()[1];
        ModelFunction model = x -> a * Math.pow(x, b);
        return finalizeResult("Степенная функция", new double[]{a, b}, "φ(x) = " + fmt(a) + " * x^(" + fmt(b) + ")", model, points, null);
    }

    private ApproximationResult finalizeResult(String name, double[] coefficients, String formula, ModelFunction model, List<Point> points, Double pearson) {
        double[] phi = new double[points.size()];
        double[] epsilon = new double[points.size()];
        double s = 0.0;
        double avarageY = points.stream().mapToDouble(Point::y).average().orElse(0.0);
        double totalSumOfSquares = 0.0;
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double value = model.apply(point.x());
            if (!Double.isFinite(value)) {
                return invalidResult(name, "Функция не определена на части входных точек.");
            }
            phi[i] = value;
            epsilon[i] = value - point.y();
            s += epsilon[i] * epsilon[i];
            totalSumOfSquares += Math.pow(point.y() - avarageY, 2.0);
        }
        double sigma = Math.sqrt(s / points.size());
        double r2 = totalSumOfSquares == 0.0 ? 1.0 : 1.0 - s / totalSumOfSquares;
        return new ApproximationResult(name, true, null, formula, coefficients, phi, epsilon, s, sigma, r2, describeR2(r2), pearson, model);
    }

    private ApproximationResult invalidResult(String name, String reason) {
        return new ApproximationResult(name, false, reason, null, null, null, null, Double.NaN, Double.NaN, Double.NaN, null, null, null);
    }

    private Double calcPearson(List<Point> points, double averageX, double averageY) {
        double numerator = 0.0;
        double dx = 0.0;
        double dy = 0.0;
        for (Point p : points) {
            numerator += (p.x() - averageX) * (p.y() - averageY);
            dx += Math.pow(p.x() - averageX, 2.0);
            dy += Math.pow(p.y() - averageY, 2.0);
        }
        if (dx == 0.0 || dy == 0.0) {
            return null;
        }
        return numerator / Math.sqrt(dx * dy);
    }

    private String describeR2(double r2) {
        if (r2 >= 0.95) {
            return "Высокая точность аппроксимации.";
        }
        if (r2 >= 0.75) {
            return "Удовлетворительная точность аппроксимации.";
        }
        if (r2 >= 0.5) {
            return "Слабая аппроксимация.";
        }
        return "Точность аппроксимации недостаточна, модель требует изменения.";
    }

    private double[] solve(double[][] matrix) {
        int n = matrix.length;
        double[][] a = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            a[i] = Arrays.copyOf(matrix[i], n + 1);
        }
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(a[row][col]) > Math.abs(a[pivot][col])) {
                    pivot = row;
                }
            }
            if (Math.abs(a[pivot][col]) < 1e-12) {
                throw new IllegalArgumentException("Система нормальных уравнений вырождена.");
            }
            double[] tmp = a[col];
            a[col] = a[pivot];
            a[pivot] = tmp;
            double divisor = a[col][col];
            for (int j = col; j <= n; j++) {
                a[col][j] /= divisor;
            }
            for (int row = 0; row < n; row++) {
                if (row == col) {
                    continue;
                }
                double factor = a[row][col];
                for (int j = col; j <= n; j++) {
                    a[row][j] -= factor * a[col][j];
                }
            }
        }
        double[] solution = new double[n];
        for (int i = 0; i < n; i++) {
            solution[i] = a[i][n];
        }
        return solution;
    }

    private String coefficientName(int index) {
        return switch (index) {
            case 0 -> "a";
            case 1 -> "b";
            case 2 -> "c";
            case 3 -> "d";
            default -> "k" + index;
        };
    }

    private String fmt(double value) {
        return DF.format(value);
    }
}
