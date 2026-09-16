package functions;

public class Systems {
    private static final String[][] systems = {{"tg(xy + 0.1) = x²", "x² + 2y² = 1"}, {"sin(x + 0.5) - y = 1", "cos(y - 2) + x = 0"}, {"x + sin(y) = -0.4", "2y - cos(x + 1) = 0"}};

    public static String[] getSystem(int index) {
        return systems[index];
    }

    public static int getLength() {
        return systems.length;
    }

    public static double[] evaluatePhi(int index, double x, double y) {
        switch (index) {
            case 0:
                double tanVal = Math.tan(x * y + 0.1);
                double ySquare = (1 - x * x) / 2;
                if (tanVal < 0 || ySquare < 0) {
                    throw new IllegalArgumentException("Начальное приближение выводит итерации за область определения системы");
                }
                double phi1 = Math.sqrt(tanVal);
                double phi2 = Math.sqrt(ySquare);
                return new double[] {phi1, phi2};
            case 1:
                return new double[] {-Math.cos(y - 2), Math.sin(x + 0.5) - 1};
            case 2:
                return new double[] {-0.4 - Math.sin(y), Math.cos(x + 1) / 2};
            default:
                throw new IllegalArgumentException("Неверный индекс системы: " + index);
        }
    }

    public static double[] evaluateSystem(int index, double x, double y) {
        switch (index) {
            case 0:
                return new double[] {Math.tan(x * y + 0.1) - x * x, x * x + 2 * y * y - 1};
            case 1:
                return new double[] {Math.sin(x + 0.5) - y - 1, Math.cos(y - 2) + x};
            case 2:
                return new double[] {x + Math.sin(y) + 0.4, 2 * y - Math.cos(x + 1)};
            default:
                throw new IllegalArgumentException("Неверный индекс системы: " + index);
        }
    }

    public static double[][] phiJacobian(int index, double x, double y) {
        switch (index) {
            case 0:
                return new double[][] {
                        {
                                y / (2 * Math.sqrt(Math.max(Math.tan(x * y + 0.1), 1e-12)) * Math.cos(x * y + 0.1) * Math.cos(x * y + 0.1)),
                                x / (2 * Math.sqrt(Math.max(Math.tan(x * y + 0.1), 1e-12)) * Math.cos(x * y + 0.1) * Math.cos(x * y + 0.1))
                        },
                        {
                                -x / (2 * Math.sqrt(Math.max((1 - x * x) / 2, 1e-12))),
                                0
                        }
                };
            case 1:
                return new double[][] {{0, Math.sin(y - 2)},{Math.cos(x + 0.5), 0}};
            case 2:
                return new double[][] {{0, -Math.cos(y)},{-Math.sin(x + 1) / 2, 0}};
            default:
                throw new IllegalArgumentException("Неверный индекс системы: " + index);
        }
    }

    public static double maxSum(int index, double x, double y) {
        double[][] jacobian = phiJacobian(index, x, y);
        return Math.max(Math.abs(jacobian[0][0]) + Math.abs(jacobian[0][1]), Math.abs(jacobian[1][0]) + Math.abs(jacobian[1][1]));
    }
}
