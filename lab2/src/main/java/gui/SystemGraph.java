package gui;

import functions.Systems;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class SystemGraph extends JPanel {
    private int systemIndex;
    private double x0;
    private double y0;
    private Double rootX;
    private Double rootY;
    private double xMin = -2;
    private double xMax = 2;
    private double yMin = -2;
    private double yMax = 2;

    public SystemGraph() {
        setBorder(BorderFactory.createTitledBorder("График системы"));
    }

    public void updatePlot(int systemIndex, double x0, double y0, Double rootX, Double rootY) {
        this.systemIndex = systemIndex;
        this.x0 = x0;
        this.y0 = y0;
        this.rootX = rootX;
        this.rootY = rootY;
        updateBounds();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int left = 48;
        int right = 18;
        int top = 28;
        int bottom = 36;

        g2.setColor(new Color(248, 249, 252));
        g2.fillRect(left, top, width - left - right, height - top - bottom);
        g2.setColor(new Color(215, 220, 228));
        g2.drawRect(left, top, width - left - right, height - top - bottom);

        drawAxis(g2, left, top, width, height, right, bottom);
        drawContours(g2, left, top, width, height, right, bottom);
        drawPoints(g2, left, top, width, height, right, bottom);
        drawLabels(g2, left, top, width, height, right, bottom);

        g2.dispose();
    }

    private void drawAxis(Graphics2D g2, int left, int top, int width, int height, int right, int bottom) {
        g2.setColor(new Color(150, 157, 168));
        if (0 >= xMin && 0 <= xMax) {
            int xAxis = mapX(0, left, width, right);
            g2.drawLine(xAxis, top, xAxis, height - bottom);
        }
        if (0 >= yMin && 0 <= yMax) {
            int yAxis = mapY(0, top, height, bottom);
            g2.drawLine(left, yAxis, width - right, yAxis);
        }
    }

    private void drawContours(Graphics2D g2, int left, int top, int width, int height, int right, int bottom) {
        int samplesX = Math.max(width - left - right, 220);
        int samplesY = Math.max(height - top - bottom, 180);

        for (int ix = 0; ix < samplesX; ix++) {
            for (int iy = 0; iy < samplesY; iy++) {
                double x = xMin + (xMax - xMin) * ix / (samplesX - 1.0);
                double y = yMax - (yMax - yMin) * iy / (samplesY - 1.0);
                double[] values;
                try {
                    values = Systems.evaluateSystem(systemIndex, x, y);
                } catch (Exception e) {
                    continue;
                }
                if (!Double.isFinite(values[0]) || !Double.isFinite(values[1])) {
                    continue;
                }

                int px = mapX(x, left, width, right);
                int py = mapY(y, top, height, bottom);

                if (Math.abs(values[0]) < contourTolerance()) {
                    g2.setColor(new Color(39, 105, 190, 140));
                    g2.fillRect(px, py, 1, 1);
                }
                if (Math.abs(values[1]) < contourTolerance()) {
                    g2.setColor(new Color(206, 74, 74, 140));
                    g2.fillRect(px, py, 1, 1);
                }
            }
        }

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(39, 105, 190));
        g2.drawString("Первая кривая", left + 8, top + 16);
        g2.setColor(new Color(206, 74, 74));
        g2.drawString("Вторая кривая", left + 120, top + 16);
    }

    private void drawPoints(Graphics2D g2, int left, int top, int width, int height, int right, int bottom) {
        int startX = mapX(x0, left, width, right);
        int startY = mapY(y0, top, height, bottom);
        g2.setColor(new Color(70, 70, 70));
        g2.fillOval(startX - 4, startY - 4, 8, 8);
        g2.drawString("x0, y0", startX + 6, startY - 6);

        if (rootX != null && rootY != null) {
            int rootPx = mapX(rootX, left, width, right);
            int rootPy = mapY(rootY, top, height, bottom);
            g2.setColor(new Color(35, 145, 75));
            g2.fillOval(rootPx - 5, rootPy - 5, 10, 10);
            g2.drawString("(" + String.format("%.3f", rootX) + ", " + String.format("%.3f", rootY) + ")",
                    rootPx + 8, rootPy - 8);
        }
    }

    private void drawLabels(Graphics2D g2, int left, int top, int width, int height, int right, int bottom) {
        g2.setColor(new Color(70, 76, 84));
        FontMetrics metrics = g2.getFontMetrics();
        String leftLabel = String.format("%.2f", xMin);
        String rightLabel = String.format("%.2f", xMax);
        String topLabel = String.format("%.2f", yMax);
        String bottomLabel = String.format("%.2f", yMin);
        g2.drawString(leftLabel, left, height - 12);
        g2.drawString(rightLabel, width - right - metrics.stringWidth(rightLabel), height - 12);
        g2.drawString(topLabel, 8, top + metrics.getAscent());
        g2.drawString(bottomLabel, 8, height - bottom);
    }

    private void updateBounds() {
        double centerX = rootX != null ? rootX : x0;
        double centerY = rootY != null ? rootY : y0;
        double span = systemIndex == 0 ? 2.5 : 4.0;
        xMin = centerX - span / 2;
        xMax = centerX + span / 2;
        yMin = centerY - span / 2;
        yMax = centerY + span / 2;
    }

    private double contourTolerance() {
        return 0.003 * Math.max((xMax - xMin), (yMax - yMin));
    }

    private int mapX(double x, int left, int width, int right) {
        return left + (int) Math.round((x - xMin) * (width - left - right) / (xMax - xMin));
    }

    private int mapY(double y, int top, int height, int bottom) {
        return height - bottom - (int) Math.round((y - yMin) * (height - top - bottom) / (yMax - yMin));
    }
}
