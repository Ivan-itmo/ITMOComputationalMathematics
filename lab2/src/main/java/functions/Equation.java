package functions;

public class Equation {
    private static final String[] equations = {
            "-1.38x³ - 5.42x² + 2.57x + 10.95",
            "x³ - 2x + 1",
            "sin(x) + 0.1x²",
            "e^x - 2x²",
            "cos(x)"
    };

    public static String getEquation(int index) {
        return equations[index];
    }
    public static int getLength() {
        return equations.length;
    }

    public static double evaluate(int index, double x) {
        switch (index) {
            case 0:
                return -1.38 * Math.pow(x, 3) - 5.42 * Math.pow(x, 2) + 2.57 * x + 10.95;
            case 1:
                return Math.pow(x, 3) - 2 * x + 1;
            case 2:
                return Math.sin(x) + 0.1 * x * x;
            case 3:
                return Math.exp(x) - 2 * x * x;
            case 4:
                return Math.cos(x);
            default:
                throw new IllegalArgumentException("Неверный индекс функции: " + index);
        }
    }

    public static double derivative(int index, double x) {
        switch (index) {
            case 0:
                return -4.14 * x * x - 10.84 * x + 2.57;
            case 1:
                return 3 * x * x - 2;
            case 2:
                return Math.cos(x) + 0.2 * x;
            case 3:
                return Math.exp(x) - 4 * x;
            case 4:
                return -Math.sin(x);
            default:
                throw new IllegalArgumentException("Неверный индекс функции: " + index);
        }
    }

    public static double secondDerivative(int index, double x) {
        switch (index) {
            case 0:
                return -8.28 * x - 10.84;
            case 1:
                return 6 * x;
            case 2:
                return -Math.sin(x) + 0.2;
            case 3:
                return Math.exp(x) - 4;
            case 4:
                return -Math.cos(x);
            default:
                throw new IllegalArgumentException("Неверный индекс функции: " + index);
        }
    }
    public static boolean severalRoots(int index, double a, double b) {
        int steps = 1000;
        double step = (b - a) / steps;
        double prev = derivative(index, a);
        for (int i = 1; i <= steps; i++) {
            double x = a + i * step;
            double current = derivative(index, x);
            if (prev * current < 0) {
                return true;
            }
            prev = current;
        }
        return false;
    }
}
