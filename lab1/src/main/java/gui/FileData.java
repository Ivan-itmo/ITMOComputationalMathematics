package gui;

import java.math.BigDecimal;

public class FileData {
    public final int n;
    public final BigDecimal[][] A;
    public final BigDecimal[] b;
    public FileData(int n, BigDecimal[][] A, BigDecimal[] b) {
        this.n = n;
        this.A = A;
        this.b = b;
    }
}