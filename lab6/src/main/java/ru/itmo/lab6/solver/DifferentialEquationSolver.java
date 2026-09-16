package ru.itmo.lab6.solver;

import java.util.ArrayList;
import java.util.List;
import ru.itmo.lab6.model.DifferentialEquation;
import ru.itmo.lab6.model.InputData;
import ru.itmo.lab6.model.MethodResult;
import ru.itmo.lab6.model.Solutions;

public class DifferentialEquationSolver {
    public Solutions solve(InputData input) {
        validate(input);

        List<Double> xValues = buildXValues(input.x0(), input.xn(), input.h());
        List<Double> exactValues = calculateExactValues(input, xValues);

        List<Double> improvedEuler = solveImprovedEuler(input.equation(), input.x0(), input.y0(), input.h(), xValues.size());
        double improvedEulerError = estimateRungeError(input.equation(), input.x0(), input.y0(), input.xn(), input.h(), 2);

        List<Double> rungeKutta = solveRungeKutta4(input.equation(), input.x0(), input.y0(), input.h(), xValues.size());
        double rungeKuttaError = estimateRungeError(input.equation(), input.x0(), input.y0(), input.xn(), input.h(), 4);

        List<Double> milne = solveMilne(input.equation(), input.x0(), input.y0(), input.h(), xValues.size(), input.epsilon());
        double milneError = maxAbsoluteError(milne, exactValues);

        return new Solutions(
                xValues,
                exactValues,
                new MethodResult("Усовершенствованный Эйлер", improvedEuler, improvedEulerError, "Оценка по правилу Рунге"),
                new MethodResult("Рунге-Кутта 4 порядка", rungeKutta, rungeKuttaError, "Оценка по правилу Рунге"),
                new MethodResult("Милн", milne, milneError, "Максимальная ошибка относительно точного решения")
        );
    }

    private void validate(InputData input) {
        if (input.equation() == null) {
            throw new IllegalArgumentException("Выберите дифференциальное уравнение.");
        }
        if (input.h() <= 0) {
            throw new IllegalArgumentException("Шаг h должен быть положительным.");
        }
        if (input.epsilon() <= 0) {
            throw new IllegalArgumentException("Точность epsilon должна быть положительной.");
        }
        if (input.xn() <= input.x0()) {
            throw new IllegalArgumentException("Правая граница интервала должна быть больше x0.");
        }
        double steps = (input.xn() - input.x0()) / input.h();
        double rounded = Math.rint(steps);
        if (Math.abs(steps - rounded) > 1e-9) {
            throw new IllegalArgumentException("Шаг h должен делить интервал [x0, xn] без остатка.");
        }
        if (rounded < 4) {
            throw new IllegalArgumentException("Для метода Милна нужно минимум 4 шага на интервале.");
        }
    }

    private List<Double> buildXValues(double x0, double xn, double h) {
        int count = (int) Math.round((xn - x0) / h) + 1;
        List<Double> xValues = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            xValues.add(x0 + i * h);
        }
        return xValues;
    }

    private List<Double> calculateExactValues(InputData input, List<Double> xValues) {
        List<Double> exactValues = new ArrayList<>(xValues.size());
        for (double x : xValues) {
            exactValues.add(input.equation().exactValue(input.x0(), input.y0(), x));
        }
        return exactValues;
    }

    private List<Double> solveImprovedEuler(DifferentialEquation equation, double x0, double y0, double h, int count) {
        List<Double> yValues = new ArrayList<>(count);
        yValues.add(y0);
        double x = x0;
        double y = y0;
        for (int i = 1; i < count; i++) {
            double predictor = y + h * equation.derivative(x, y);
            y = y + h / 2.0 * (equation.derivative(x, y) + equation.derivative(x + h, predictor));
            x += h;
            yValues.add(y);
        }
        return yValues;
    }

    private List<Double> solveRungeKutta4(DifferentialEquation equation, double x0, double y0, double h, int count) {
        List<Double> yValues = new ArrayList<>(count);
        yValues.add(y0);
        double x = x0;
        double y = y0;
        for (int i = 1; i < count; i++) {
            double k1 = h * equation.derivative(x, y);
            double k2 = h * equation.derivative(x + h / 2.0, y + k1 / 2.0);
            double k3 = h * equation.derivative(x + h / 2.0, y + k2 / 2.0);
            double k4 = h * equation.derivative(x + h, y + k3);
            y = y + (k1 + 2 * k2 + 2 * k3 + k4) / 6.0;
            x += h;
            yValues.add(y);
        }
        return yValues;
    }

    private List<Double> solveMilne(DifferentialEquation equation, double x0, double y0, double h, int count, double epsilon) {
        List<Double> yValues = new ArrayList<>(solveRungeKutta4(equation, x0, y0, h, Math.min(count, 4)));
        List<Double> fValues = new ArrayList<>(count);
        for (int i = 0; i < yValues.size(); i++) {
            double x = x0 + i * h;
            fValues.add(equation.derivative(x, yValues.get(i)));
        }
        for (int i = 3; i < count - 1; i++) {
            double xNext = x0 + (i + 1) * h;
            double predictor = yValues.get(i - 3) + 4.0 * h / 3.0 * (2.0 * fValues.get(i) - fValues.get(i - 1) + 2.0 * fValues.get(i - 2));
            double corrected = predictor;
            double previous;
            int iterations = 0;
            do {
                previous = corrected;
                corrected = yValues.get(i - 1) + h / 3.0 * (fValues.get(i - 1) + 4.0 * fValues.get(i) + equation.derivative(xNext, previous));
                iterations++;
            } while (Math.abs(corrected - previous) > epsilon && iterations < 100);
            yValues.add(corrected);
            fValues.add(equation.derivative(xNext, corrected));
        }
        return yValues;
    }

    private double estimateRungeError(DifferentialEquation equation, double x0, double y0, double xn, double h, int order) {
        List<Double> coarse;
        List<Double> fine;
        int coarseCount = (int) Math.round((xn - x0) / h) + 1;
        int fineCount = (int) Math.round((xn - x0) / (h / 2.0)) + 1;

        if (order == 2) {
            coarse = solveImprovedEuler(equation, x0, y0, h, coarseCount);
            fine = solveImprovedEuler(equation, x0, y0, h / 2.0, fineCount);
        } else {
            coarse = solveRungeKutta4(equation, x0, y0, h, coarseCount);
            fine = solveRungeKutta4(equation, x0, y0, h / 2.0, fineCount);
        }

        double maxError = 0.0;
        for (int i = 0; i < coarse.size(); i++) {
            double diff = Math.abs(fine.get(i * 2) - coarse.get(i)) / (Math.pow(2, order) - 1.0);
            maxError = Math.max(maxError, diff);
        }
        return maxError;
    }

    private double maxAbsoluteError(List<Double> actual, List<Double> expected) {
        double max = 0.0;
        for (int i = 0; i < actual.size(); i++) {
            max = Math.max(max, Math.abs(actual.get(i) - expected.get(i)));
        }
        return max;
    }
}
