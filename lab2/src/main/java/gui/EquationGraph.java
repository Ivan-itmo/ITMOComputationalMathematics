package gui;

import functions.Equation;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

public class EquationGraph extends JPanel {
    private int funcIndex;
    private double a = -2;
    private double b = 2;
    private Double root;

    public EquationGraph() {
        setBorder(BorderFactory.createTitledBorder("График функции"));
    }

    public void updatePlot(int funcIndex, double a, double b, Double root) {
        this.funcIndex = funcIndex;
        this.a = a;
        this.b = b;
        this.root = root;
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

        double span = Math.max(Math.abs(b - a), 1e-6);
        double xMin = a - span * 0.2 - 0.5;
        double xMax = b + span * 0.2 + 0.5;

        int samples = Math.max(width - left - right, 200);
        double yMin = 1000000;
        double yMax = -1000000;

        for (int i = 0; i <= samples; i++) {
            double x = xMin + (xMax - xMin) * i / samples;
            double y = Equation.evaluate(funcIndex, x);
            if (Double.isFinite(y)) {
                yMin = Math.min(yMin, y);
                yMax = Math.max(yMax, y);
            }
        }

        double fa = Equation.evaluate(funcIndex, a);
        double fb = Equation.evaluate(funcIndex, b);
        if (Double.isFinite(fa)) {
            yMin = Math.min(yMin, fa);
            yMax = Math.max(yMax, fa);
        }
        if (Double.isFinite(fb)) {
            yMin = Math.min(yMin, fb);
            yMax = Math.max(yMax, fb);
        }
        yMin = Math.min(yMin, 0);
        yMax = Math.max(yMax, 0);

        if (!Double.isFinite(yMin) || !Double.isFinite(yMax) || Math.abs(yMax - yMin) < 1e-9) {
            yMin = -1;
            yMax = 1;
        } else {
            double yPad = (yMax - yMin) * 0.15;
            yMin -= yPad;
            yMax += yPad;
        }

        g2.setColor(new Color(248, 249, 252));
        g2.fillRect(left, top, width - left - right, height - top - bottom);
        g2.setColor(new Color(215, 220, 228));
        g2.drawRect(left, top, width - left - right, height - top - bottom);

        drawAxis(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);
        drawBoundaries(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);
        drawFunction(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax, samples);
        drawRoot(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);
        drawLabels(g2, left, top, width, height, right, bottom, xMin, xMax, yMin, yMax);

        g2.dispose();
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

    private void drawBoundaries(Graphics2D g2, int left, int top, int width, int height, int right, int bottom, double xMin, double xMax, double yMin, double yMax) {
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

    private void drawFunction(Graphics2D g2, int left, int top, int width, int height, int right, int bottom, double xMin, double xMax, double yMin, double yMax, int samples) {
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

    private void drawRoot(Graphics2D g2, int left, int top, int width, int height, int right, int bottom, double xMin, double xMax, double yMin, double yMax) {
        if (root == null) {
            return;
        }
        double y = Equation.evaluate(funcIndex, root);
        if (!Double.isFinite(y)) {
            return;
        }
        int px = mapX(root, left, width, right, xMin, xMax);
        int py = mapY(y, top, height, bottom, yMin, yMax);
        g2.setColor(new Color(35, 145, 75));
        g2.fillOval(px - 5, py - 5, 10, 10);
        g2.drawString("x* = " + String.format("%.3f", root), px + 8, py - 8);
    }

    private void drawLabels(Graphics2D g2, int left, int top, int width, int height, int right, int bottom, double xMin, double xMax, double yMin, double yMax) {
        g2.setColor(new Color(70, 76, 84));
        FontMetrics metrics = g2.getFontMetrics();
        String xLeft = String.format("%.2f", xMin);
        String xRight = String.format("%.2f", xMax);
        String yTop = String.format("%.2f", yMax);
        String yBottom = String.format("%.2f", yMin);
        g2.drawString(xLeft, left, height - 12);
        g2.drawString(xRight, width - right - metrics.stringWidth(xRight), height - 12);
        g2.drawString(yTop, 8, top + metrics.getAscent());
        g2.drawString(yBottom, 8, height - bottom);
    }

    private int mapX(double x, int left, int width, int right, double xMin, double xMax) {
        return left + (int) Math.round((x - xMin) * (width - left - right) / (xMax - xMin));
    }

    private int mapY(double y, int top, int height, int bottom, double yMin, double yMax) {
        return height - bottom - (int) Math.round((y - yMin) * (height - top - bottom) / (yMax - yMin));
    }
}
