package logic;

import java.math.BigDecimal;

public class GaussResult {
    public final BigDecimal[] x;
    public final BigDecimal[] residuals;
    public final BigDecimal determinant;
    public final BigDecimal[][] triangularMatrixA;
    public final BigDecimal[] triangularVectorB;
    public GaussResult(BigDecimal[] x, BigDecimal[] residuals, BigDecimal det, BigDecimal[][] triA, BigDecimal[] triB) {
        this.x = x;
        this.residuals = residuals;
        this.determinant = det;
        this.triangularMatrixA = triA;
        this.triangularVectorB = triB;
    }
}