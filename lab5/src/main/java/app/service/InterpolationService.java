package app.service;

import app.model.DataSet;
import app.model.InterpolationReport;
import app.model.MethodResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

public class InterpolationService {
    public InterpolationReport analyze(DataSet dataSet, double x) {
        double[][] differenceTable = dataSet.isUniform() ? buildFiniteDifferences(dataSet.yValues()) : buildDividedDifferences(dataSet.xValues(), dataSet.yValues());

        List<MethodResult> results = new ArrayList<>();
        Map<String, DoubleUnaryOperator> plotFunctions = new LinkedHashMap<>();

        double lagrange = lagrangeValue(dataSet, x);
        results.add(success("Лагранж", lagrange));
        plotFunctions.put("Лагранж", value -> lagrangeValue(dataSet, value));

        if (dataSet.sourceFunction() != null) {
            double chebyshevLagrange = ChebyshevLagrangeInterpolator.interpolate(
                    dataSet.sourceFunction(),
                    dataSet.minX(),
                    dataSet.maxX(),
                    dataSet.points().size(),
                    x
            );
            results.add(success("Лагранж (корни Чебышева)", chebyshevLagrange));
            plotFunctions.put("Лагранж (корни Чебышева)", value -> ChebyshevLagrangeInterpolator.interpolate(
                    dataSet.sourceFunction(),
                    dataSet.minX(),
                    dataSet.maxX(),
                    dataSet.points().size(),
                    value
            ));
        } else {
            results.add(unavailable("Лагранж (корни Чебышева)"));
        }

        if (dataSet.isUniform()) {
            double newtonForward = newtonForwardValue(dataSet, x);
            double newtonBackward = newtonBackwardValue(dataSet, x);
            results.add(success("Ньютон (1-я формула)", newtonForward));
            results.add(success("Ньютон (2-я формула)", newtonBackward));
            plotFunctions.put("Ньютон", value -> newtonForwardValue(dataSet, value));

            if (dataSet.points().size() >= 3) {
                double gaussForward = gaussForwardValue(dataSet, x);
                double gaussBackward = gaussBackwardValue(dataSet, x);
                results.add(success("Гаусс (1-я формула)", gaussForward));
                results.add(success("Гаусс (2-я формула)", gaussBackward));
                plotFunctions.put("Гаусс", value -> gaussForwardValue(dataSet, value));
            } else {
                results.add(unavailable("Гаусс (1-я формула)"));
                results.add(unavailable("Гаусс (2-я формула)"));
            }

            if (canUseStirling(dataSet)) {
                double stirling = stirlingValue(dataSet, x);
                results.add(success("Стирлинг", stirling));
                plotFunctions.put("Стирлинг", value -> stirlingValue(dataSet, value));
            } else {
                results.add(unavailable("Стирлинг"));
            }

            if (canUseBessel(dataSet)) {
                double bessel = besselValue(dataSet, x);
                results.add(success("Бессель", bessel));
                plotFunctions.put("Бессель", value -> besselValue(dataSet, value));
            } else {
                results.add(unavailable("Бессель"));
            }
        } else {
            results.add(unavailable("Ньютон (1-я формула)"));
            results.add(unavailable("Ньютон (2-я формула)"));
            results.add(unavailable("Гаусс (1-я формула)"));
            results.add(unavailable("Гаусс (2-я формула)"));
            results.add(unavailable("Стирлинг"));
            results.add(unavailable("Бессель"));
        }

        if (dataSet.sourceFunction() != null) {
            plotFunctions.put("Исходная функция", dataSet.sourceFunction().function()::applyAsDouble);
        }
        String analysis = buildAnalysis(dataSet, x, results);
        String[] headers = dataSet.isUniform() ? finiteDifferenceHeaders(dataSet.points().size()) : dividedDifferenceHeaders(dataSet.points().size());
        return new InterpolationReport(dataSet, x, results, differenceTable, headers, analysis, plotFunctions);
    }

    private MethodResult success(String method, double value) {
        return new MethodResult(method, formatDouble(value), value);
    }

    private MethodResult unavailable(String method) {
        return new MethodResult(method, "—", null);
    }

    private String buildAnalysis(DataSet dataSet, double x, List<MethodResult> results) {
        StringBuilder builder = new StringBuilder();
        builder.append("Источник данных: ").append(dataSet.sourceDescription()).append('\n');
        builder.append("Точек: ").append(dataSet.points().size()).append('\n');
        builder.append("Сетка: ").append(dataSet.isUniform() ? "равномерная" : "неравномерная").append('\n');

        if (x < dataSet.minX() || x > dataSet.maxX()) {
            builder.append("Режим: экстраполяция.\n");
        }
        List<Double> values = results.stream()
                .map(MethodResult::numericValue)
                .filter(value -> value != null)
                .toList();
        if (!values.isEmpty()) {
            double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            builder.append("Разброс результатов методов: ")
                    .append(formatDouble(max - min))
                    .append('\n');
        }
        if (dataSet.sourceFunction() != null) {
            double exact = dataSet.sourceFunction().function().applyAsDouble(x);
            builder.append("Точное значение функции: ").append(formatDouble(exact)).append('\n');
            for (MethodResult result : results) {
                if (result.numericValue() != null) {
                    builder.append(result.methodName())
                            .append(": |ошибка| = ")
                            .append(formatDouble(Math.abs(result.numericValue() - exact)))
                            .append('\n');
                }
            }
        }
        return builder.toString();
    }

    public static String formatDouble(double value) {
        return String.format(Locale.US, "%.8f", value);
    }

    public static double[][] buildFiniteDifferences(double[] y) {
        int n = y.length;
        double[][] table = new double[n][n];
        for (int i = 0; i < n; i++) {
            table[i][0] = y[i];
        }
        for (int order = 1; order < n; order++) {
            for (int row = 0; row < n - order; row++) {
                table[row][order] = table[row + 1][order - 1] - table[row][order - 1];
            }
        }
        return table;
    }

    public static double[][] buildDividedDifferences(double[] x, double[] y) {
        int n = y.length;
        double[][] table = new double[n][n];
        for (int i = 0; i < n; i++) {
            table[i][0] = y[i];
        }
        for (int order = 1; order < n; order++) {
            for (int row = 0; row < n - order; row++) {
                table[row][order] = (table[row + 1][order - 1] - table[row][order - 1]) / (x[row + order] - x[row]);
            }
        }
        return table;
    }

    public static double lagrangeValue(DataSet dataSet, double x) {
        List<app.model.Point> points = dataSet.points();
        double result = 0.0;
        for (int i = 0; i < points.size(); i++) {
            double term = points.get(i).y();
            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    term *= (x - points.get(j).x()) / (points.get(i).x() - points.get(j).x());
                }
            }
            result += term;
        }
        return result;
    }

    public static double newtonForwardValue(DataSet dataSet, double x) {
        double[] y = dataSet.yValues();
        double[][] diff = buildFiniteDifferences(y);
        double h = dataSet.step();
        double t = (x - dataSet.points().get(0).x()) / h;
        double result = diff[0][0];
        double product = 1.0;
        double factorial = 1.0;
        for (int order = 1; order < y.length; order++) {
            product *= (t - (order - 1));
            factorial *= order;
            result += diff[0][order] * product / factorial;
        }
        return result;
    }

    public static double newtonBackwardValue(DataSet dataSet, double x) {
        double[] y = dataSet.yValues();
        double[][] diff = buildFiniteDifferences(y);
        double h = dataSet.step();
        int n = y.length;
        double t = (x - dataSet.points().get(n - 1).x()) / h;
        double result = diff[n - 1][0];
        double product = 1.0;
        double factorial = 1.0;
        for (int order = 1; order < n; order++) {
            product *= (t + (order - 1));
            factorial *= order;
            result += diff[n - 1 - order][order] * product / factorial;
        }
        return result;
    }

    public static double gaussForwardValue(DataSet dataSet, double x) {
        double[] y = dataSet.yValues();
        double[][] diff = buildFiniteDifferences(y);
        int n = y.length;
        int center = (n - 1) / 2;
        double h = dataSet.step();
        double t = (x - dataSet.points().get(center).x()) / h;
        double result = diff[center][0];
        for (int order = 1; order < n; order++) {
            int index = center - (order / 2);
            result += diff[index][order] * gaussForwardProduct(t, order) / factorial(order);
        }
        return result;
    }

    public static double gaussBackwardValue(DataSet dataSet, double x) {
        double[] y = dataSet.yValues();
        double[][] diff = buildFiniteDifferences(y);
        int n = y.length;
        int center = n / 2;
        double h = dataSet.step();
        double t = (x - dataSet.points().get(center).x()) / h;
        double result = diff[center][0];
        for (int order = 1; order < n; order++) {
            int index = center - ((order + 1) / 2);
            result += diff[index][order] * gaussBackwardProduct(t, order) / factorial(order);
        }
        return result;
    }

    public static double stirlingValue(DataSet dataSet, double x) {
        if (!canUseStirling(dataSet)) {
            throw new IllegalArgumentException("Формула Стирлинга требует равномерную сетку и нечетное число узлов не меньше 5.");
        }
        double[][] diff = buildFiniteDifferences(dataSet.yValues());
        int center = dataSet.points().size() / 2;
        double t = (x - dataSet.points().get(center).x()) / dataSet.step();
        double result = diff[center][0];

        for (int order = 1; order < dataSet.points().size(); order++) {
            double coefficient;
            if (order % 2 == 0) {
                int evenHalf = order / 2;
                int index = center - evenHalf;
                coefficient = diff[index][order];
            } else {
                int oddHalf = order / 2;
                int leftIndex = center - oddHalf - 1;
                int rightIndex = center - oddHalf;
                coefficient = (diff[leftIndex][order] + diff[rightIndex][order]) / 2.0;
            }
            result += coefficient * stirlingProduct(t, order) / factorial(order);
        }
        return result;
    }

    public static double besselValue(DataSet dataSet, double x) {
        if (!canUseBessel(dataSet)) {
            throw new IllegalArgumentException("Формула Бесселя требует равномерную сетку и четное число узлов не меньше 4.");
        }
        double[][] diff = buildFiniteDifferences(dataSet.yValues());
        int leftCenter = dataSet.points().size() / 2 - 1;
        double h = dataSet.step();
        double t = (x - dataSet.points().get(leftCenter).x()) / h;
        double result = (diff[leftCenter][0] + diff[leftCenter + 1][0]) / 2.0;

        for (int order = 1; order < dataSet.points().size(); order++) {
            double coefficient;
            if (order % 2 == 1) {
                int oddHalf = order / 2;
                int index = leftCenter - oddHalf;
                coefficient = diff[index][order];
            } else {
                int evenHalf = order / 2;
                int leftIndex = leftCenter - evenHalf;
                int rightIndex = leftCenter - evenHalf + 1;
                coefficient = (diff[leftIndex][order] + diff[rightIndex][order]) / 2.0;
            }
            result += coefficient * besselProduct(t, order) / factorial(order);
        }
        return result;
    }

    private static double gaussForwardProduct(double t, int order) {
        double product = 1.0;
        for (int currentOrder = 1; currentOrder <= order; currentOrder++) {
            if (currentOrder == 1) {
                product *= t;
            } else if (currentOrder % 2 == 0) {
                product *= t - currentOrder / 2.0;
            } else {
                product *= t + (currentOrder - 1) / 2.0;
            }
        }
        return product;
    }

    private static double gaussBackwardProduct(double t, int order) {
        double product = 1.0;
        for (int currentOrder = 1; currentOrder <= order; currentOrder++) {
            if (currentOrder == 1) {
                product *= t;
            } else if (currentOrder % 2 == 0) {
                product *= t + currentOrder / 2.0;
            } else {
                product *= t - (currentOrder - 1) / 2.0;
            }
        }
        return product;
    }

    private static boolean canUseStirling(DataSet dataSet) {
        return dataSet.isUniform() && dataSet.points().size() >= 5 && dataSet.points().size() % 2 == 1;
    }

    private static boolean canUseBessel(DataSet dataSet) {
        return dataSet.isUniform() && dataSet.points().size() >= 4 && dataSet.points().size() % 2 == 0;
    }

    private static double stirlingProduct(double t, int order) {
        if (order % 2 == 0) {
            double product = t * t;
            for (int i = 1; i < order / 2; i++) {
                product *= t * t - i * i;
            }
            return product;
        }

        double product = t;
        for (int i = 1; i <= order / 2; i++) {
            product *= t * t - i * i;
        }
        return product;
    }

    private static double besselProduct(double t, int order) {
        if (order == 1) {
            return t - 0.5;
        }

        double product = order % 2 == 1 ? t - 0.5 : 1.0;
        int lastPosition = order % 2 == 0 ? order : order - 1;
        for (int position = 1; position <= lastPosition; position++) {
            if (position == 1) {
                product *= t;
            } else if (position % 2 == 0) {
                product *= t - position / 2.0;
            } else {
                product *= t + (position - 1) / 2.0;
            }
        }
        return product;
    }

    private static double factorial(int value) {
        double result = 1.0;
        for (int i = 2; i <= value; i++) {
            result *= i;
        }
        return result;
    }

    private static String[] finiteDifferenceHeaders(int size) {
        String[] headers = new String[size + 1];
        headers[0] = "x";
        headers[1] = "y";
        for (int i = 2; i <= size; i++) {
            headers[i] = "Δ^" + (i - 1) + "y";
        }
        return headers;
    }

    private static String[] dividedDifferenceHeaders(int size) {
        String[] headers = new String[size + 1];
        headers[0] = "x";
        headers[1] = "f[x]";
        for (int i = 2; i <= size; i++) {
            headers[i] = "f[x.." + (i - 1) + "]";
        }
        return headers;
    }

}
