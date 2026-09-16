package app.ui;

import app.model.DataSet;
import app.model.Point;
import app.model.InterpolationReport;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

public class PlotPanel extends JPanel {
    private static final Color[] SERIES_COLORS = {
            new Color(40, 116, 166),
            new Color(192, 57, 43),
            new Color(39, 174, 96),
            new Color(230, 126, 34),
            new Color(142, 68, 173),
            new Color(22, 160, 133)
    };

    private InterpolationReport report;
    private DataSet dataSet;
    private Map<String, DoubleUnaryOperator> plotFunctions = Map.of();

    public PlotPanel() {
        setBackground(Color.WHITE);
    }

    public void setReport(InterpolationReport report) {
        this.report = report;
        this.dataSet = report == null ? null : report.dataSet();
        this.plotFunctions = report == null ? Map.of() : report.plotFunctions();
        repaint();
    }

    public void setPlot(DataSet dataSet, Map<String, DoubleUnaryOperator> plotFunctions) {
        this.report = null;
        this.dataSet = dataSet;
        this.plotFunctions = new LinkedHashMap<>(plotFunctions);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (dataSet == null || plotFunctions.isEmpty()) {
            g2.dispose();
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int left = 60;
        int right = 20;
        int top = 20;
        int bottom = 50;
        double minX = dataSet.minX();
        double maxX = dataSet.maxX();
        double span = maxX - minX;
        if (span == 0.0) {
            span = 1.0;
        }
        double padding = span * 0.15;
        double plotMinX = minX - padding;
        double plotMaxX = maxX + padding;
        double minY = dataSet.minY();
        double maxY = dataSet.maxY();
        List<Double> sampledValues = new ArrayList<>();
        for (DoubleUnaryOperator function : plotFunctions.values()) {
            for (int i = 0; i < 250; i++) {
                double x = plotMinX + (plotMaxX - plotMinX) * i / 249.0;
                double y = function.applyAsDouble(x);
                if (Double.isFinite(y)) {
                    sampledValues.add(y);
                }
            }
        }
        for (double value : sampledValues) {
            minY = Math.min(minY, value);
            maxY = Math.max(maxY, value);
        }
        if (Math.abs(maxY - minY) < 1.0E-9) {
            maxY += 1.0;
            minY -= 1.0;
        }
        double yPadding = (maxY - minY) * 0.15;
        minY -= yPadding;
        maxY += yPadding;
        g2.setColor(new Color(235, 235, 235));
        g2.fillRect(left, top, width - left - right, height - top - bottom);
        g2.setColor(Color.GRAY);
        g2.drawRect(left, top, width - left - right, height - top - bottom);
        drawAxis(g2, left, top, width - right, height - bottom, plotMinX, plotMaxX, minY, maxY);

        int seriesIndex = 0;
        for (Map.Entry<String, DoubleUnaryOperator> entry : plotFunctions.entrySet()) {
            g2.setColor(SERIES_COLORS[seriesIndex % SERIES_COLORS.length]);
            g2.setStroke(new BasicStroke(entry.getKey().startsWith("Исходная") ? 2.5f : 1.8f));
            Path2D.Double path = new Path2D.Double();
            boolean started = false;
            for (int i = 0; i < 400; i++) {
                double x = plotMinX + (plotMaxX - plotMinX) * i / 399.0;
                double y = entry.getValue().applyAsDouble(x);
                if (!Double.isFinite(y)) {
                    started = false;
                    continue;
                }
                int sx = mapX(x, plotMinX, plotMaxX, left, width - right);
                int sy = mapY(y, minY, maxY, top, height - bottom);
                if (!started) {
                    path.moveTo(sx, sy);
                    started = true;
                } else {
                    path.lineTo(sx, sy);
                }
            }
            g2.draw(path);
            seriesIndex++;
        }
        g2.setColor(Color.BLACK);
        for (Point point : dataSet.points()) {
            int sx = mapX(point.x(), plotMinX, plotMaxX, left, width - right);
            int sy = mapY(point.y(), minY, maxY, top, height - bottom);
            g2.fill(new Ellipse2D.Double(sx - 4, sy - 4, 8, 8));
        }
        drawLegend(g2, left + 10, top + 10);
        g2.dispose();
    }

    private void drawAxis(Graphics2D g2, int left, int top, int right, int bottom, double minX, double maxX, double minY, double maxY) {
        g2.setColor(new Color(150, 150, 150));
        int zeroX = mapX(0.0, minX, maxX, left, right);
        int zeroY = mapY(0.0, minY, maxY, top, bottom);
        if (zeroX >= left && zeroX <= right) {
            g2.drawLine(zeroX, top, zeroX, bottom);
        }
        if (zeroY >= top && zeroY <= bottom) {
            g2.drawLine(left, zeroY, right, zeroY);
        }
        g2.setColor(Color.DARK_GRAY);
        FontMetrics metrics = g2.getFontMetrics();
        for (int i = 0; i <= 5; i++) {
            double x = minX + (maxX - minX) * i / 5.0;
            int sx = mapX(x, minX, maxX, left, right);
            g2.drawLine(sx, bottom, sx, bottom + 4);
            String label = String.format("%.2f", x);
            g2.drawString(label, sx - metrics.stringWidth(label) / 2, bottom + 18);
        }
        for (int i = 0; i <= 5; i++) {
            double y = minY + (maxY - minY) * i / 5.0;
            int sy = mapY(y, minY, maxY, top, bottom);
            g2.drawLine(left - 4, sy, left, sy);
            String label = String.format("%.2f", y);
            g2.drawString(label, 6, sy + metrics.getAscent() / 2);
        }
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        int index = 0;
        for (String name : plotFunctions.keySet()) {
            g2.setColor(SERIES_COLORS[index % SERIES_COLORS.length]);
            g2.fillRect(x, y + index * 18 - 8, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawString(name, x + 18, y + index * 18 + 2);
            index++;
        }
        g2.fillRect(x, y + index * 18 - 8, 12, 12);
        g2.drawString("Узлы интерполяции", x + 18, y + index * 18 + 2);
    }

    private int mapX(double x, double minX, double maxX, int left, int right) {
        return (int) Math.round(left + (x - minX) * (right - left) / (maxX - minX));
    }

    private int mapY(double y, double minY, double maxY, int top, int bottom) {
        return (int) Math.round(bottom - (y - minY) * (bottom - top) / (maxY - minY));
    }
}
