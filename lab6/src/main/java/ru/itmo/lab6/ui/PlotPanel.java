package ru.itmo.lab6.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.swing.JPanel;
import ru.itmo.lab6.model.MethodResult;
import ru.itmo.lab6.model.Solutions;

public class PlotPanel extends JPanel {
    private final String methodName;
    private final Color methodColor;
    private final Function<Solutions, MethodResult> resultExtractor;
    private Solutions solution;

    public PlotPanel(String methodName, Color methodColor, Function<Solutions, MethodResult> resultExtractor) {
        this.methodName = methodName;
        this.methodColor = methodColor;
        this.resultExtractor = resultExtractor;
        setPreferredSize(new Dimension(960, 420));
        setBackground(Color.WHITE);
    }

    public void setSolution(Solutions solution) {
        this.solution = solution;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int left = 60;
        int right = getWidth() - 25;
        int top = 20;
        int bottom = getHeight() - 45;
        g2.setColor(new Color(240, 240, 240));
        g2.fillRect(left, top, right - left, bottom - top);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(left, top, right - left, bottom - top);

        if (solution == null) {
            g2.drawString("После расчета здесь появится график метода " + methodName + ".", left + 20, top + 30);
            g2.dispose();
            return;
        }

        List<Double> xValues = solution.xValues();
        List<Double> exact = solution.exactValues();
        MethodResult methodResult = resultExtractor.apply(solution);
        List<Double> methodValues = methodResult.getValues();

        double minX = xValues.get(0);
        double maxX = xValues.get(xValues.size() - 1);
        double minY = min(exact, methodValues);
        double maxY = max(exact, methodValues);
        if (Math.abs(maxY - minY) < 1e-12) {
            maxY += 1.0;
            minY -= 1.0;
        }

        drawAxisLabels(g2, left, right, top, bottom, minX, maxX, minY, maxY);
        drawLine(g2, xValues, exact, left, right, top, bottom, minX, maxX, minY, maxY, new Color(25, 111, 61), 3f);
        drawLine(g2, xValues, methodValues, left, right, top, bottom, minX, maxX, minY, maxY, methodColor, 2.5f);
        drawLegend(g2, left, top);
        g2.dispose();
    }

    private void drawAxisLabels(Graphics2D g2, int left, int right, int top, int bottom, double minX, double maxX, double minY, double maxY) {
        g2.setColor(Color.GRAY);
        for (int i = 0; i <= 5; i++) {
            int y = top + i * (bottom - top) / 5;
            int x = left + i * (right - left) / 5;
            g2.drawLine(left, y, right, y);
            g2.drawLine(x, top, x, bottom);
            double yValue = maxY - i * (maxY - minY) / 5.0;
            double xValue = minX + i * (maxX - minX) / 5.0;
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.format("%.3f", yValue), 8, y + 5);
            g2.drawString(String.format("%.3f", xValue), x - 14, bottom + 20);
            g2.setColor(Color.GRAY);
        }
    }

    private void drawLine(Graphics2D g2, List<Double> xValues, List<Double> yValues, int left, int right, int top, int bottom,
            double minX, double maxX, double minY, double maxY, Color color, float width
    ) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(width));
        for (int i = 0; i < xValues.size() - 1; i++) {
            int x1 = scaleX(xValues.get(i), left, right, minX, maxX);
            int y1 = scaleY(yValues.get(i), top, bottom, minY, maxY);
            int x2 = scaleX(xValues.get(i + 1), left, right, minX, maxX);
            int y2 = scaleY(yValues.get(i + 1), top, bottom, minY, maxY);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawLegend(Graphics2D g2, int left, int top) {
        int x = left + 15;
        int y = top + 18;
        drawLegendItem(g2, x, y, new Color(25, 111, 61), "Точное решение");
        drawLegendItem(g2, x + 210, y, methodColor, methodName);
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String text) {
        g2.setColor(color);
        g2.fillRect(x, y - 10, 18, 8);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(text, x + 24, y);
    }

    private int scaleX(double x, int left, int right, double minX, double maxX) {
        return left + (int) Math.round((x - minX) / (maxX - minX) * (right - left));
    }

    private int scaleY(double y, int top, int bottom, double minY, double maxY) {
        return bottom - (int) Math.round((y - minY) / (maxY - minY) * (bottom - top));
    }

    private double min(List<Double>... collections) {
        return Arrays.stream(collections)
                .flatMap(List::stream)
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(Double.NaN);
    }

    private double max(List<Double>... collections) {
        return Arrays.stream(collections)
                .flatMap(List::stream)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(Double.NaN);
    }
}
