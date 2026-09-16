package solver;

import functions.Equation;

public class EquationSolver {
    public static Result bisection(int funcIndex, double a, double b, double epsilon) {
        int iterations = 0;
        double fa = Equation.evaluate(funcIndex, a);
        double fb = Equation.evaluate(funcIndex, b);
        if (fa * fb > 0) {
            return new Result(0, 0, 0, false, "Метод половинного деления", "Корень не найден: f(a)·f(b) > 0");
        }
        double x = a;
        double fx = fa;
        while ((b - a) / 2 > epsilon) {
            x = (a + b) / 2;
            fx = Equation.evaluate(funcIndex, x);
            iterations++;
            if (Math.abs(fx) < epsilon) {
                break;
            }
            if (fa * fx < 0) {
                b = x;
                fb = fx;
            } else {
                a = x;
                fa = fx;
            }
        }
        double root = (a + b) / 2;
        double fRoot = Equation.evaluate(funcIndex, root);
        return new Result(root, fRoot, iterations, true, "Метод половинного деления");
    }

    public static Result newton(int funcIndex, double a, double b, double epsilon, int maxIterations) {
        double fa = Equation.evaluate(funcIndex, a);
        double fb = Equation.evaluate(funcIndex, b);
        if (fa * fb > 0) {
            return new Result(0, 0, 0, false, "Метод Ньютона", "На интервале [a, b] не выполнено условие f(a)·f(b) < 0");
        }
        if (Equation.severalRoots(funcIndex, a, b)) {
            return new Result(0, 0, 0, false, "Метод Ньютона", "На интервале [a, b] производная меняет знак, метод Ньютона нельзя применять");
        }

        double x = chooseInitialApproximation(funcIndex, a, b);

        int iterations = 0;
        for (int i = 0; i < maxIterations; i++) {
            double fx = Equation.evaluate(funcIndex, x);
            double dfx = Equation.derivative(funcIndex, x);
            if (dfx == 0) {
                return new Result(x, fx, iterations, false, "Метод Ньютона", "Производная равна нулю");
            }
            double xNew = x - fx / dfx;
            iterations++;
            if (Math.abs(xNew - x) < epsilon) {
                double fNew = Equation.evaluate(funcIndex, xNew);
                return new Result(xNew, fNew, iterations, true, "Метод Ньютона");
            }

            x = xNew;
        }

        return new Result(x, Equation.evaluate(funcIndex, x), iterations, false, "Метод Ньютона", "Превышено максимальное число итераций");
    }

    public static Result simpleIteration(int funcIndex, double x0, double a, double b, double epsilon, int maxIterations) {
        double maxDerivative = 0;
        boolean allPositive = true;
        boolean allNegative = true;
        int steps = 100;
        double step = (b - a) / steps;

        for (int i = 0; i <= steps; i++) {
            double x = a + i * step;
            double derivative = Equation.derivative(funcIndex, x);
            maxDerivative = Math.max(maxDerivative, Math.abs(derivative));
            if (derivative <= 0) {
                allPositive = false;
            }
            if (derivative >= 0) {
                allNegative = false;
            }
        }

        if (maxDerivative == 0) {
            return new Result(0, 0, 0, false, "Метод простой итерации", "На интервале производная равна нулю, невозможно выбрать параметр lambda");
        }

        double lambda;
        if (allPositive) {
            lambda = -1.0 / maxDerivative;
        } else if (allNegative) {
            lambda = 1.0 / maxDerivative;
        } else {
            return new Result(0, 0, 0, false, "Метод простой итерации", "Производная меняет знак на интервале, параметр lambda нельзя выбрать");
        }

        double q = qValue(funcIndex, a, b, lambda);
        if (q >= 1) {
            return new Result(0, 0, 0, false, "Метод простой итерации", "Условие сходимости не выполняется: q");
        }

        double x = Double.isNaN(x0) ? chooseInitialApproximation(funcIndex, a, b) : x0;
        int iterations = 0;
        for (int i = 0; i < maxIterations; i++) {
            double fx = Equation.evaluate(funcIndex, x);
            double xNew = x + lambda * fx;
            iterations++;
            double delta = Math.abs(xNew - x);
            double qDelta = qChange(delta, q);
            if (qDelta < epsilon) {
                double fNew = Equation.evaluate(funcIndex, xNew);
                return new Result(xNew, fNew, iterations, true, "Метод простой итерации (q = " + String.format("%.3f", q) + ")");
            }
            x = xNew;
        }
        double fx = Equation.evaluate(funcIndex, x);
        return new Result(x, fx, iterations, false, "Метод простой итерации", "Превышено максимальное число итераций");
    }

    private static double qValue(int funcIndex, double a, double b, double lambda) {
        int steps = 100;
        double step = (b - a) / steps;
        double q = 0;
        for (int i = 0; i <= steps; i++) {
            double x = a + i * step;
            q = Math.max(q, Math.abs(1 + lambda * Equation.derivative(funcIndex, x)));
        }

        return q;
    }

    private static double qChange(double delta, double q) {
        if (q <= 0.5) {
            return delta;
        }
        return q * delta / (1 - q);
    }

    private static double chooseInitialApproximation(int funcIndex, double a, double b) {
        double fa = Equation.evaluate(funcIndex, a);
        double fb = Equation.evaluate(funcIndex, b);
        if (fa * Equation.secondDerivative(funcIndex, a) > 0) {
            return a;
        }
        if (fb * Equation.secondDerivative(funcIndex, b) > 0) {
            return b;
        }
        return (a + b) / 2;
    }
}
