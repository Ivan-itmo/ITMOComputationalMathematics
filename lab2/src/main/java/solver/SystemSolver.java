package solver;

import functions.Systems;

public class SystemSolver {
    public static Result[] simpleIteration(int sysIndex, double x0, double y0, double epsilon, int maxIterations) {
        double x = x0;
        double y = y0;
        int iterations = 0;
        double deltaX = 0;
        double deltaY = 0;
        double q;

        try {
            q = Systems.maxSum(sysIndex, x, y);
        } catch (IllegalArgumentException e) {
            return new Result[] {
                    Result.system(x, 0, iterations, false, "Метод простой итерации (x)", e.getMessage()),
                    Result.system(y, 0, iterations, false, "Метод простой итерации (y)", e.getMessage())
            };
        }

        double[] startResiduals = Systems.evaluateSystem(sysIndex, x, y);
        if (Math.abs(startResiduals[0]) < epsilon && Math.abs(startResiduals[1]) < epsilon) {
            return new Result[] {
                    Result.system(x, 0, iterations, true, "Метод простой итерации (x)", "Начальное приближение уже удовлетворяет системе с заданной точностью"),
                    Result.system(y, 0, iterations, true, "Метод простой итерации (y)", "Начальное приближение уже удовлетворяет системе с заданной точностью")
            };
        }

        for (int i = 0; i < maxIterations; i++) {
            double[] phi;
            try {
                phi = Systems.evaluatePhi(sysIndex, x, y);
            } catch (IllegalArgumentException e) {
                return new Result[] {
                        Result.system(x, 0, iterations, false, "Метод простой итерации (x)", e.getMessage()),
                        Result.system(y, 0, iterations, false, "Метод простой итерации (y)", e.getMessage())
                };
            }
            double xNew = phi[0];
            double yNew = phi[1];
            iterations++;
            deltaX = Math.abs(xNew - x);
            deltaY = Math.abs(yNew - y);

            double currentQ;
            try {
                currentQ = Systems.maxSum(sysIndex, xNew, yNew);
            } catch (IllegalArgumentException e) {
                return new Result[] {
                        Result.system(xNew, 0, iterations, false, "Метод простой итерации (x)", e.getMessage()),
                        Result.system(yNew, 0, iterations, false, "Метод простой итерации (y)", e.getMessage())
                };
            }

            double qDeltaX = qChange(deltaX, currentQ);
            double qDeltaY = qChange(deltaY, currentQ);
            boolean deltaSmall = qDeltaX < epsilon && qDeltaY < epsilon;
            double[] residuals = Systems.evaluateSystem(sysIndex, xNew, yNew);
            boolean residualSmall = Math.abs(residuals[0]) < epsilon && Math.abs(residuals[1]) < epsilon;
            if (deltaSmall || residualSmall) {
                return new Result[] {Result.system(xNew, qDeltaX, iterations, true, "Метод простой итерации (q = " + String.format("%.3f", currentQ) + ", x)"),
                        Result.system(yNew, qDeltaY, iterations, true, "Метод простой итерации (q = " + String.format("%.3f", currentQ) + ", y)")
                };
            }
            x = xNew;
            y = yNew;
        }
        return new Result[] {
                Result.system(x, 0, iterations, false, "Метод простой итерации (x)", "Превышено максимальное число итераций"),
                Result.system(y, 0, iterations, false, "Метод простой итерации (y)", "Превышено максимальное число итераций")
        };
    }

    private static double qChange(double delta, double q) {
        if (q <= 0.5) {
            return delta;
        }
        if (q >= 1) {
            return delta;
        }
        return q * delta / (1 - q);
    }

}
