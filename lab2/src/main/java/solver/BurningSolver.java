package solver;

import functions.Equation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BurningSolver {
    private static final int defaultMaxIterations = 20;
    private static final double defaultInitialTemperature = 1.0;
    private static final double defaultCoolingRate = 0.995;
    private static final double minTemperature = 1e-8;

    public static Result solve(int funcIndex, double a, double b, double epsilon) {
        return solveDetailed(funcIndex, a, b, epsilon, defaultMaxIterations, defaultInitialTemperature, defaultCoolingRate);
    }

    public static Result solve(int funcIndex, double a, double b, double epsilon, int maxIterations, double initialTemperature, double coolingRate) {
        return solveDetailed(funcIndex, a, b, epsilon, maxIterations, initialTemperature, coolingRate);
    }

    public static BurningResult solveDetailed(int funcIndex, double a, double b, double epsilon) {
        return solveDetailed(funcIndex, a, b, epsilon, defaultMaxIterations, defaultInitialTemperature, defaultCoolingRate);
    }

    public static BurningResult solveDetailed(int funcIndex, double a, double b, double epsilon, int maxIterations, double initialTemperature, double coolingRate) {
        if (a >= b) {
            return new BurningResult(0, 0, 0, false, "Метод обжига",
                    "Левая граница интервала должна быть меньше правой");
        }
        if (epsilon <= 0) {
            return new BurningResult(0, 0, 0, false, "Метод обжига",
                    "Точность должна быть положительной");
        }
        if (maxIterations <= 0) {
            return new BurningResult(0, 0, 0, false, "Метод обжига",
                    "Число итераций должно быть положительным");
        }
        if (initialTemperature <= 0) {
            return new BurningResult(0, 0, 0, false, "Метод обжига",
                    "Начальная температура должна быть положительной");
        }
        if (coolingRate <= 0 || coolingRate >= 1) {
            return new BurningResult(0, 0, 0, false, "Метод обжига",
                    "Коэффициент охлаждения должен лежать в интервале (0, 1)");
        }

        Random random = new Random();
        List<Double> sampledPoints = new ArrayList<>();
        List<Double> acceptedPoints = new ArrayList<>();
        double x = a + random.nextDouble() * (b - a);
        sampledPoints.add(x);
        acceptedPoints.add(x);
        double error = Math.abs(Equation.evaluate(funcIndex, x));
        double bestX = x;
        double bestError = error;
        double temperature = initialTemperature;
        int iterations = 0;

        for (int i = 0; i < maxIterations && temperature > minTemperature; i++) {
            double step = (b - a) * temperature;
            double candidate = Math.max(a, Math.min(b, x + (random.nextDouble() * 2 - 1) * step));
            double candidateError = Math.abs(Equation.evaluate(funcIndex, candidate));
            sampledPoints.add(candidate);

            if (shouldAccept(candidateError, error, temperature, random)) {
                x = candidate;
                error = candidateError;
                acceptedPoints.add(candidate);
            }

            if (candidateError < bestError) {
                bestX = candidate;
                bestError = candidateError;
            }

            iterations++;
            if (bestError <= epsilon) {
                return createResult(bestX, iterations, true, "", sampledPoints, acceptedPoints, funcIndex);
            }

            temperature *= coolingRate;
        }

        boolean success = bestError <= epsilon;
        if (success) {
            return createResult(bestX, iterations, true, "", sampledPoints, acceptedPoints, funcIndex);
        }
        return createResult(bestX, iterations, false, "Не удалось достичь заданной точности",
                sampledPoints, acceptedPoints, funcIndex);
    }

    private static boolean shouldAccept(double candidateError, double currentError, double temperature, Random random) {
        if (candidateError <= currentError) {
            return true;
        }
        double probability = Math.exp(-(candidateError - currentError) / temperature);
        return random.nextDouble() < probability;
    }

    private static BurningResult createResult(double x, int iterations, boolean success, String message, List<Double> sampledPoints, List<Double> acceptedPoints, int funcIndex) {
        return new BurningResult(x, Equation.evaluate(funcIndex, x), iterations, success, "Метод обжига",
                message, sampledPoints, acceptedPoints);
    }
}
