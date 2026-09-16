package gui;

import functions.Equation;
import solver.BurningResult;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.List;

public class AnnealingGraphWindow extends JFrame {
    public AnnealingGraphWindow(int funcIndex, double a, double b, BurningResult result) {
        setTitle("Метод обжига: траектория поиска");
        setSize(920, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(new AnnealingGraphPanel(funcIndex, a, b, result), BorderLayout.CENTER);
    }

    private static class AnnealingGraphPanel extends JPanel {
        private final int funcIndex;
        private final double a;
        private final double b;
        private final BurningResult result;

        private AnnealingGraphPanel(int funcIndex, double a, double b, BurningResult result) {
            this.funcIndex = funcIndex;
            this.a = a;
            this.b = b;
            this.result = result;
            setBorder(BorderFactory.createTitledBorder("График функции и точки метода обжига"));
            setPreferredSize(new Dimension(880, 620));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 56;
            int right = 24;
            int top = 28;
            int bottom = 42;

            double span = Math.max(Math.abs(b - a), 1e-6);
            double xMin = a - span * 0.2 - 0.5;
            double xMax = b + span * 0.2 + 0.5;
            int samples = Math.max(width - left - right, 250);

            double[] bounds = computeYBounds(xMin, xMax, samples);
            double yMin = bounds[0];
            double yMax = bounds[1];

            g2.setColor(new Color(248, 249, 252));
            g2.fillRect(left, top, width - left - right, height - top - bottom);
            g2.setColor(new Color(215, 220, 228));
            g2.drawRect(left, top, width - left - right, height - top - bottom);

            drawAxis(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);
            drawBoundaries(g2, left, top, width, height, right, bottom, xMin, xMax);
            drawFunction(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax, samples);
            drawPoints(g2, result.getSampledPoints(), new Color(214, 76, 76, 110), 6,
                    left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);
            drawPoints(g2, result.getAcceptedPoints(), new Color(35, 145, 75, 170), 8,
                    left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);
            drawRoot(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);
            drawLabels(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);
            drawLegend(g2, left, top);

            g2.dispose();
        }

        private double[] computeYBounds(double xMin, double xMax, int samples) {
            double yMin = Double.POSITIVE_INFINITY;
            double yMax = Double.NEGATIVE_INFINITY;

            for (int i = 0; i <= samples; i++) {
                double x = xMin + (xMax - xMin) * i / samples;
                double y = Equation.evaluate(funcIndex, x);
                if (Double.isFinite(y)) {
                    yMin = Math.min(yMin, y);
                    yMax = Math.max(yMax, y);
                }
            }

            yMin = Math.min(yMin, 0);
            yMax = Math.max(yMax, 0);
            if (!Double.isFinite(yMin) || !Double.isFinite(yMax) || Math.abs(yMax - yMin) < 1e-9) {
                return new double[] {-1, 1};
            }

            double yPad = (yMax - yMin) * 0.18;
            return new double[] {yMin - yPad, yMax + yPad};
        }

        private void drawAxis(Graphics2D g2, int left, int top, int width, int height, int right, int bottom,
                              double xMin, double xMax, double yMin, double yMax) {
            g2.setColor(new Color(150, 157, 168));
            if (0 >= xMin && 0 <= xMax) {
                int x0 = mapX(0, left, width, right, xMin, xMax);
                g2.drawLine(x0, top, x0, height - bottom);
            }
            if (0 >= yMin && 0 <= yMax) {
                int y0 = mapY(0, top, height, bottom, yMin, yMax);
                g2.drawLine(left, y0, width - right, y0);
            }
        }

        private void drawBoundaries(Graphics2D g2, int left, int top, int width, int height, int right, int bottom,
                                    double xMin, double xMax) {
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(214, 76, 76));
            int ax = mapX(a, left, width, right, xMin, xMax);
            int bx = mapX(b, left, width, right, xMin, xMax);
            g2.drawLine(ax, top, ax, height - bottom);
            g2.drawLine(bx, top, bx, height - bottom);
            g2.setColor(new Color(90, 96, 104));
            g2.drawString("a = " + String.format("%.3f", a), ax + 4, top + 16);
            g2.drawString("b = " + String.format("%.3f", b), bx + 4, top + 32);
        }

        private void drawFunction(Graphics2D g2, int left, int top, int width, int height, int right, int bottom,
                                  double xMin, double xMax, double yMin, double yMax, int samples) {
            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(new Color(34, 94, 168));
            Path2D.Double path = new Path2D.Double();
            boolean started = false;

            for (int i = 0; i <= samples; i++) {
                double x = xMin + (xMax - xMin) * i / samples;
                double y = Equation.evaluate(funcIndex, x);
                if (!Double.isFinite(y) || y < yMin - 10 * (yMax - yMin) || y > yMax + 10 * (yMax - yMin)) {
                    started = false;
                    continue;
                }
                int px = mapX(x, left, width, right, xMin, xMax);
                int py = mapY(y, top, height, bottom, yMin, yMax);
                if (!started) {
                    path.moveTo(px, py);
                    started = true;
                } else {
                    path.lineTo(px, py);
                }
            }
            g2.draw(path);
        }

        private void drawPoints(Graphics2D g2, List<Double> points, Color color, int size,
                                int left, int top, int width, int height, int right, int bottom,
                                double xMin, double xMax, double yMin, double yMax) {
            g2.setColor(color);
            for (double x : points) {
                double y = Equation.evaluate(funcIndex, x);
                if (!Double.isFinite(y)) {
                    continue;
                }
                int px = mapX(x, left, width, right, xMin, xMax);
                int py = mapY(y, top, height, bottom, yMin, yMax);
                g2.fillOval(px - size / 2, py - size / 2, size, size);
            }
        }

        private void drawRoot(Graphics2D g2, int left, int top, int width, int height, int right, int bottom,
                              double xMin, double xMax, double yMin, double yMax) {
            if (!result.isSuccess()) {
                return;
            }
            double root = result.getRoot();
            double y = Equation.evaluate(funcIndex, root);
            if (!Double.isFinite(y)) {
                return;
            }
            int px = mapX(root, left, width, right, xMin, xMax);
            int py = mapY(y, top, height, bottom, yMin, yMax);
            g2.setColor(new Color(46, 81, 186));
            g2.fillOval(px - 6, py - 6, 12, 12);
            g2.drawString("x* = " + String.format("%.4f", root), px + 8, py - 8);
        }

        private void drawLabels(Graphics2D g2, int left, int top, int width, int height, int right, int bottom,
                                double xMin, double xMax, double yMin, double yMax) {
            g2.setColor(new Color(70, 76, 84));
            FontMetrics metrics = g2.getFontMetrics();
            String xLeft = String.format("%.2f", xMin);
            String xRight = String.format("%.2f", xMax);
            String yTop = String.format("%.2f", yMax);
            String yBottom = String.format("%.2f", yMin);
            g2.drawString(xLeft, left, height - 12);
            g2.drawString(xRight, width - right - metrics.stringWidth(xRight), height - 12);
            g2.drawString(yTop, 10, top + metrics.getAscent());
            g2.drawString(yBottom, 10, height - bottom);
        }

        private void drawLegend(Graphics2D g2, int left, int top) {
            int legendX = left + 12;
            int legendY = top + 18;

            g2.setColor(new Color(214, 76, 76, 110));
            g2.fillOval(legendX, legendY, 8, 8);
            g2.setColor(new Color(70, 76, 84));
            g2.drawString("случайно проверенные точки", legendX + 14, legendY + 8);

            g2.setColor(new Color(35, 145, 75, 170));
            g2.fillOval(legendX, legendY + 18, 8, 8);
            g2.setColor(new Color(70, 76, 84));
            g2.drawString("принятые точки", legendX + 14, legendY + 26);

            g2.setColor(new Color(46, 81, 186));
            g2.fillOval(legendX, legendY + 36, 8, 8);
            g2.setColor(new Color(70, 76, 84));
            g2.drawString("лучший найденный корень", legendX + 14, legendY + 44);
        }

        private int mapX(double x, int left, int width, int right, double xMin, double xMax) {
            return left + (int) Math.round((x - xMin) * (width - left - right) / (xMax - xMin));
        }

        private int mapY(double y, int top, int height, int bottom, double yMin, double yMax) {
            return height - bottom - (int) Math.round((y - yMin) * (height - top - bottom) / (yMax - yMin));
        }
    }
}
