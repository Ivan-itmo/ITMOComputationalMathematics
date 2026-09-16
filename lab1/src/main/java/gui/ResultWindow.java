package gui;

import logic.GaussResult;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ResultWindow extends JFrame {
    public ResultWindow(int n, GaussResult myResult, double[] libX, double libDet) {
        setTitle("Результаты решения СЛАУ");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1050, 750);
        setLocationRelativeTo(null);

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        StringBuilder sb = new StringBuilder();

        sb.append("Матрица коэффициентов и правая часть:\n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sb.append(format(myResult.triangularMatrixA[i][j], 16));
            }
            sb.append(" | ").append(format(myResult.triangularVectorB[i], 16)).append("\n");
        }
        sb.append("\n");

        sb.append(String.format("%-40s %s\n", "Определитель (ручной):", format(myResult.determinant, 20)));
        sb.append(String.format("%-40s %.12e\n", "Определитель (библиотека):", libDet));
        BigDecimal diffDet = myResult.determinant.subtract(BigDecimal.valueOf(libDet));
        sb.append(String.format("%-40s %s\n", "Разница:", format(diffDet, 20)));
        sb.append("\n");

        sb.append("Сравнение решений:\n");
        sb.append(String.format("%3s   %20s   %20s   %16s\n", "i", "Ручной", "Библиотека", "Разница"));

        for (int i = 0; i < n; i++) {
            BigDecimal myVal = myResult.x[i];
            double libVal = libX[i];
            BigDecimal diff = myVal.subtract(BigDecimal.valueOf(libVal));
            String sMy = format(myVal, 20);
            String sLib = String.format("%.12e", libVal);
            String sDiff = format(diff, 16);
            sb.append(String.format("%3d   %20s   %20s   %16s\n", i + 1, sMy, sLib, sDiff));
        }
        sb.append("\n");

        sb.append("Невязки (r = b - A*x):\n");
        for (int i = 0; i < n; i++) {
            sb.append(String.format("r[%d] = %s\n", i + 1, format(myResult.residuals[i], 20)));
        }
        text.setText(sb.toString());
        add(new JScrollPane(text));
    }
    private static String format(BigDecimal value, int width) {
        if (value == null) return String.format("%" + width + "s", "null");
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return String.format("%" + width + "s", "0");
        }
        try {
            long asLong = value.longValueExact();
            return String.format("%" + width + "d", asLong);
        } catch (ArithmeticException e) {}

        BigDecimal stripped = value.stripTrailingZeros();
        String plain = stripped.toPlainString();
        if (plain.length() > width || plain.contains("E") || plain.contains("e")) {
            return String.format("%" + width + ".3e", value.doubleValue());
        }
        return String.format("%" + width + "s", plain);
    }
}