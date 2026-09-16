package logic;

import org.apache.commons.math3.linear.*;
import exceptions.BadSolveException;
import java.math.BigDecimal;

public class LibrarySolver {
    public static double[] solve(BigDecimal[][] A, BigDecimal[] b) {
        int n = A.length;
        double[][] A_double = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A_double[i][j] = A[i][j].doubleValue();
            }
        }
        double[] b_double = new double[n];
        for (int i = 0; i < n; i++) {
            b_double[i] = b[i].doubleValue();
        }
        RealMatrix matrix = new Array2DRowRealMatrix(A_double);
        RealVector vector = new ArrayRealVector(b_double);
        DecompositionSolver solver = new LUDecomposition(matrix).getSolver();
        if (!solver.isNonSingular()) {
            throw new BadSolveException("Матрица вырождена");
        }
        return solver.solve(vector).toArray();
    }

    public static double determinant(BigDecimal[][] A) {
        int n = A.length;
        double[][] A_double = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A_double[i][j] = A[i][j].doubleValue();
            }
        }
        RealMatrix matrix = new Array2DRowRealMatrix(A_double);
        return new LUDecomposition(matrix).getDeterminant();
    }
}