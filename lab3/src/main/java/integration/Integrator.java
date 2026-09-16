package integration;

import function.FunctionChoose;
import function.FunctionBreak;

public final class Integrator {
    private static final int maxPart = (int) Math.pow(2, 24);
    private static final int maxDeltaIterations = 60;
    private static final int maxMonteCarloSamples = (int) Math.pow(2, 24);

    private Integrator() {
    }

    public static IntegrationResult integrate(FunctionChoose function, double a, double b, IntegrationMethod method, double epsilon, int startParts) {
        if (a == b) {
            return new IntegrationResult(0.0, startParts, 0.0);
        }
        FunctionBreak functionBreak = function.whereGap(a, b);
        if (functionBreak != FunctionBreak.NOTHING) {
            if (!function.isDesicion(a, b)) {
                throw new IllegalArgumentException("Интеграл не существует.");
            }
            return integrateImproper(function, a, b, method, epsilon, startParts, functionBreak);
        }
        return integrateProper(function, a, b, method, epsilon, startParts);
    }

    private static IntegrationResult integrateProper(FunctionChoose function, double a, double b, IntegrationMethod method, double epsilon, int startParts) {
        if (method == IntegrationMethod.MONTE_CARLO) {
            return MonteCarloIntegrator.integrate(function, a, b, epsilon, startParts, maxMonteCarloSamples);
        }
        return integrateProperDirect(function, a, b, method, epsilon, startParts);
    }

    private static IntegrationResult integrateProperDirect(FunctionChoose function, double left, double right, IntegrationMethod method, double epsilon, int startParts) {
        int n = startParts;
        if (method == IntegrationMethod.SIMPSON && n % 2 != 0) {
            n++;
        }
        double previous = integrateChoose(function, left, right, n, method);

        while (n <= maxPart / 2) {
            int newN = n * 2;
            double current = integrateChoose(function, left, right, newN, method);
            double rungeError = Math.abs(current - previous) / (Math.pow(2, method.getK()) - 1);

            if (rungeError <= epsilon) {
                return new IntegrationResult(current, newN, rungeError);
            }
            n = newN;
            previous = current;
        }
        throw new IllegalArgumentException("Не удалось достичь заданной точности. Попробуйте увеличить epsilon.");
    }

    private static IntegrationResult integrateImproper(FunctionChoose function, double a, double b, IntegrationMethod method, double epsilon, int startParts, FunctionBreak functionBreak) {
        double intervalLength = b - a;
        Double breakPoint = function.getGapPoint();
        if (breakPoint == null) {
            throw new IllegalArgumentException("Точка разрыва не определена для несобственного интеграла.");
        }

        double delta = switch (functionBreak) {
            case A, B -> intervalLength / 4.0;
            case INSIDE -> Math.min(breakPoint - a, b - breakPoint) / 2.0;
            case NOTHING -> throw new IllegalStateException("Разрыв не обнаружен.");
        };
        if (!(delta > 0.0)) {
            throw new IllegalArgumentException("Некорректный интервал для несобственного интеграла.");
        }

        IntegrationResult previous = integrateImproperDelta(function, a, b, breakPoint, functionBreak, method, epsilon / 4.0, startParts, delta);

        for (int i = 0; i < maxDeltaIterations; i++) {
            delta /= 2.0;
            IntegrationResult current = integrateImproperDelta(function, a, b, breakPoint, functionBreak, method, epsilon / 4.0, startParts, delta);
            double totalError = Math.abs(current.getValue() - previous.getValue()) + current.getErrorEpsilon();
            if (totalError <= epsilon) {
                return new IntegrationResult(current.getValue(), current.getParts(), totalError);
            }
            previous = current;
            if (delta <= Math.ulp(1.0) * Math.max(1.0, intervalLength)) {
                break;
            }
        }

        throw new IllegalArgumentException("Не удалось достичь заданной точности для несобственного интеграла.");
    }

    private static IntegrationResult integrateImproperDelta(FunctionChoose function, double left, double right, double breakPoint, FunctionBreak functionBreak, IntegrationMethod method, double epsilon, int startParts, double delta) {
        return switch (functionBreak) {
            case A -> integrateProper(function, left + delta, right, method, epsilon, startParts);
            case B -> integrateProper(function, left, right - delta, method, epsilon, startParts);
            case INSIDE -> {
                IntegrationResult leftResult = integrateProper(function, left, breakPoint - delta, method, epsilon / 2.0, startParts);
                IntegrationResult rightResult = integrateProper(function, breakPoint + delta, right, method, epsilon / 2.0, startParts);
                double value = leftResult.getValue() + rightResult.getValue();
                int parts = Math.max(leftResult.getParts(), rightResult.getParts());
                double runge = leftResult.getErrorEpsilon() + rightResult.getErrorEpsilon();
                yield new IntegrationResult(value, parts, runge);
            }
            case NOTHING -> throw new IllegalStateException("Разрыв не обнаружен.");
        };
    }

    public static double integrateChoose(FunctionChoose function, double a, double b, int n, IntegrationMethod method) {
        if (n <= 0) {
            throw new IllegalArgumentException("Число разбиений должно быть положительным.");
        }
        return switch (method) {
            case LEFT_RECTANGLES -> leftRectangles(function, a, b, n);
            case RIGHT_RECTANGLES -> rightRectangles(function, a, b, n);
            case MIDDLE_RECTANGLES -> middleRectangles(function, a, b, n);
            case TRAPEZOID -> trapezoid(function, a, b, n);
            case SIMPSON -> simpson(function, a, b, n);
            case MONTE_CARLO -> throw new IllegalArgumentException("Метод Монте-Карло рассчитывается отдельно.");
        };
    }

    private static double leftRectangles(FunctionChoose function, double a, double b, int n) {
        double h = (b - a) / n;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += function.calculate(a + i * h);
        }
        return sum * h;
    }

    private static double rightRectangles(FunctionChoose function, double a, double b, int n) {
        double h = (b - a) / n;
        double sum = 0.0;
        for (int i = 1; i <= n; i++) {
            sum += function.calculate(a + i * h);
        }
        return sum * h;
    }

    private static double middleRectangles(FunctionChoose function, double a, double b, int n) {
        double h = (b - a) / n;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += function.calculate(a + (i + 0.5) * h);
        }
        return sum * h;
    }

    private static double trapezoid(FunctionChoose function, double a, double b, int n) {
        double h = (b - a) / n;
        double sum = (function.calculate(a) + function.calculate(b)) / 2.0;
        for (int i = 1; i < n; i++) {
            sum += function.calculate(a + i * h);
        }
        return sum * h;
    }

    private static double simpson(FunctionChoose function, double a, double b, int n) {
        if (n % 2 != 0) {
            throw new IllegalArgumentException("Для метода Симпсона число разбиений должно быть четным.");
        }
        double h = (b - a) / n;
        double sum = function.calculate(a) + function.calculate(b);

        for (int i = 1; i < n; i++) {
            double coefficient = (i % 2 == 0) ? 2.0 : 4.0;
            sum += coefficient * function.calculate(a + i * h);
        }
        return sum * h / 3.0;
    }
}
