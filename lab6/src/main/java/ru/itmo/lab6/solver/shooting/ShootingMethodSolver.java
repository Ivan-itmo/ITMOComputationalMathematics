package ru.itmo.lab6.solver.shooting;

import java.util.ArrayList;
import java.util.List;

public class ShootingMethodSolver {
    private static final int MAX_ITERATIONS = 100;

    public ShootingResult solve(ShootingInput problem) {
        validate(problem);
        double slope0 = problem.initialSlopeGuess1();
        double slope1 = problem.initialSlopeGuess2();

        IntegrationResult result0 = integrate(problem, slope0);
        if (Math.abs(result0.residual()) <= problem.epsilon()) {
            return toShootingResult(result0);
        }
        IntegrationResult result1 = integrate(problem, slope1);
        if (Math.abs(result1.residual()) <= problem.epsilon()) {
            return toShootingResult(result1);
        }
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double denominator = result1.residual() - result0.residual();
            if (Math.abs(denominator) < 1e-12) {
                throw new IllegalStateException("Метод пристрелки выродился: два соседних приближения дали одинаковую невязку.");
            }
            double nextSlope = slope1 - result1.residual() * (slope1 - slope0) / denominator;
            IntegrationResult nextResult = integrate(problem, nextSlope);
            if (Math.abs(nextResult.residual()) <= problem.epsilon()) {
                return toShootingResult(nextResult);
            }
            slope0 = slope1;
            result0 = result1;
            slope1 = nextSlope;
            result1 = nextResult;
        }
        throw new IllegalStateException("Метод пристрелки не сошелся за " + MAX_ITERATIONS + " итераций.");
    }

    private ShootingResult toShootingResult(IntegrationResult result) {
        return new ShootingResult(result.xValues(), result.yValues(), result.derivativeValues(), result.initialSlope(), result.residual());
    }

    private void validate(ShootingInput problem) {
        if (problem == null) {
            throw new IllegalArgumentException("Задача не задана.");
        }
        if (problem.equation() == null) {
            throw new IllegalArgumentException("Не задано уравнение второго порядка.");
        }
        if (problem.h() <= 0) {
            throw new IllegalArgumentException("Шаг h должен быть положительным.");
        }
        if (problem.epsilon() <= 0) {
            throw new IllegalArgumentException("Точность epsilon должна быть положительной.");
        }
        if (problem.xn() <= problem.x0()) {
            throw new IllegalArgumentException("Правая граница интервала должна быть больше x0.");
        }
        double steps = (problem.xn() - problem.x0()) / problem.h();
        double rounded = Math.rint(steps);
        if (Math.abs(steps - rounded) > 1e-9) {
            throw new IllegalArgumentException("Шаг h должен делить интервал [x0, xn] без остатка.");
        }
        if (Math.abs(problem.initialSlopeGuess1() - problem.initialSlopeGuess2()) < 1e-12) {
            throw new IllegalArgumentException("Начальные приближения для производной должны различаться.");
        }
    }

    private IntegrationResult integrate(ShootingInput problem, double initialSlope) {
        int count = (int) Math.round((problem.xn() - problem.x0()) / problem.h()) + 1;
        List<Double> xValues = new ArrayList<>(count);
        List<Double> yValues = new ArrayList<>(count);
        List<Double> derivativeValues = new ArrayList<>(count);

        double x = problem.x0();
        double y = problem.y0();
        double z = initialSlope;

        xValues.add(x);
        yValues.add(y);
        derivativeValues.add(z);

        for (int i = 1; i < count; i++) {
            double h = problem.h();

            double k1y = h * z;
            double k1z = h * problem.equation().apply(x, y, z);

            double k2y = h * (z + k1z / 2.0);
            double k2z = h * problem.equation().apply(x + h / 2.0, y + k1y / 2.0, z + k1z / 2.0);

            double k3y = h * (z + k2z / 2.0);
            double k3z = h * problem.equation().apply(x + h / 2.0, y + k2y / 2.0, z + k2z / 2.0);

            double k4y = h * (z + k3z);
            double k4z = h * problem.equation().apply(x + h, y + k3y, z + k3z);

            y += (k1y + 2.0 * k2y + 2.0 * k3y + k4y) / 6.0;
            z += (k1z + 2.0 * k2z + 2.0 * k3z + k4z) / 6.0;
            x = problem.x0() + i * h;
            xValues.add(x);
            yValues.add(y);
            derivativeValues.add(z);
        }
        return new IntegrationResult(xValues, yValues, derivativeValues, initialSlope, yValues.get(yValues.size() - 1) - problem.yn());
    }
}
