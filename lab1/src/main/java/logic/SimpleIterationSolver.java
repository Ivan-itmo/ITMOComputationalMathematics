package logic;

import exceptions.BadSolveException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

public class SimpleIterationSolver {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int MAX_ITERATIONS = 10000;
    private static final BigDecimal EPS = new BigDecimal("1e-30");

    public static GaussResult solve(int n, BigDecimal[][] A, BigDecimal[] b, Consumer<BigDecimal[]> onIteration) {
        if (!diagonalDominance(A, n)) {
            throw new BadSolveException("Метод Якоби требует диагональное преобладание");
        }
        BigDecimal[][] B = new BigDecimal[n][n];
        BigDecimal[] c = new BigDecimal[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    B[i][j] = ZERO;
                } else {
                    B[i][j] = A[i][j].negate().divide(A[i][i], 100, RoundingMode.HALF_UP);
                }
            }
            c[i] = b[i].divide(A[i][i], 100, RoundingMode.HALF_UP);
        }

        BigDecimal[] x = new BigDecimal[n];
        for (int i = 0; i < n; i++) {
            x[i] = ZERO;
        }
        if (onIteration != null) {
            onIteration.accept(x.clone());
        }
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            BigDecimal[] xNew = new BigDecimal[n];
            boolean epsFlag = true;
            for (int i = 0; i < n; i++) {
                BigDecimal sum = c[i];
                for (int j = 0; j < n; j++) {
                    sum = sum.add(B[i][j].multiply(x[j]));
                }
                xNew[i] = sum;
                BigDecimal diff = xNew[i].subtract(x[i]).abs();
                if (diff.compareTo(EPS) > 0) {
                    epsFlag = false;
                }
            }
            if (onIteration != null) {
                onIteration.accept(xNew.clone());
            }
            if (epsFlag) {
                return buildResult(n, A, b, xNew, B, c);
            }
            x = xNew;
        }
        throw new BadSolveException("Метод не сошёлся за " + MAX_ITERATIONS + " итераций");
    }

    private static boolean diagonalDominance(BigDecimal[][] A, int n) {
        for (int i = 0; i < n; i++) {
            BigDecimal rowSum = ZERO;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    rowSum = rowSum.add(A[i][j].abs());
                }
            }
            if (A[i][i].abs().compareTo(rowSum) <= 0) {
                return false;
            }
        }
        return true;
    }

    private static GaussResult buildResult(int n, BigDecimal[][] A, BigDecimal[] b, BigDecimal[] x, BigDecimal[][] B, BigDecimal[] c) {
        BigDecimal[] residuals = new BigDecimal[n];
        for (int i = 0; i < n; i++) {
            BigDecimal sum = ZERO;
            for (int j = 0; j < n; j++) {
                sum = sum.add(A[i][j].multiply(x[j]));
            }
            residuals[i] = b[i].subtract(sum);
        }
        BigDecimal det = BigDecimal.ONE;
        for (int i = 0; i < n; i++) {
            det = det.multiply(A[i][i]);
        }
        return new GaussResult(x, residuals, det, A, b);
    }
}