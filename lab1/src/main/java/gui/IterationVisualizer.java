package gui;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class IterationVisualizer extends JFrame {
    private final List<Point> points = new ArrayList<>();

    public IterationVisualizer(BigDecimal[][] A, BigDecimal[] b) {
        setTitle("Визуализация метода простых итераций (n=2)");
        setSize(600, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new DrawingPanel(A, b));
        setVisible(true);
    }

    public void addPoint(BigDecimal x1, BigDecimal x2) {
        points.add(new Point(x1.doubleValue(), x2.doubleValue()));
        repaint();
    }

    private class DrawingPanel extends JPanel {
        private final BigDecimal[][] A;
        private final BigDecimal[] b;

        public DrawingPanel(BigDecimal[][] A, BigDecimal[] b) {
            this.A = A;
            this.b = b;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 600, 600);
            g2d.setColor(Color.BLACK);
            g2d.drawLine(0, 300, 600, 300);
            g2d.drawLine(300, 0, 300, 600);

            g2d.setColor(Color.GRAY);
            for (int i = -5; i <= 5; i++) {
                if (i == 0) continue;
                int x = 300 + i * 60;
                g2d.drawLine(x, 297, x, 303);
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(i), x - 4, 320);
                g2d.setColor(Color.GRAY);
                int y = 300 - i * 60;
                g2d.drawLine(297, y, 303, y);
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(i), 310, y + 4);
                g2d.setColor(Color.GRAY);
            }

            g2d.setColor(Color.BLACK);
            g2d.drawString("x\u2081", 580, 295);
            g2d.drawString("x\u2082", 315, 15);
            drawLine(g2d, 0, Color.BLUE);
            drawLine(g2d, 1, Color.RED);

            for (int i = 0; i < points.size(); i++) {
                Point p = points.get(i);
                if (p.x < -5 || p.x > 5 || p.y < -5 || p.y > 5) continue;
                int x = 300 + (int)(p.x * 60);
                int y = 300 - (int)(p.y * 60);
                if (i == points.size() - 1) {
                    g2d.setColor(Color.RED);
                    g2d.fillOval(x - 4, y - 4, 8, 8);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString("x*", x + 6, y - 6);
                } else {
                    g2d.setColor(Color.GREEN);
                    g2d.fillOval(x - 3, y - 3, 6, 6);
                }
            }
        }

        private void drawLine(Graphics2D g, int idx, Color color) {
            double a1 = A[idx][0].doubleValue();
            double a2 = A[idx][1].doubleValue();
            double rhs = b[idx].doubleValue();
            g.setColor(color);
            if (Math.abs(a2) > 1e-12) {
                int x1Start = -5, x1End = 5;
                double x2Start = (rhs - a1 * x1Start) / a2;
                double x2End = (rhs - a1 * x1End) / a2;

                int xStart = 300 + (int)(x1Start * 60);
                int yStart = 300 - (int)(x2Start * 60);
                int xEnd = 300 + (int)(x1End * 60);
                int yEnd = 300 - (int)(x2End * 60);

                g.drawLine(xStart, yStart, xEnd, yEnd);
            } else if (Math.abs(a1) > 1e-12) {
                double x1 = rhs / a1;
                int x = 300 + (int)(x1 * 60);
                g.drawLine(x, 0, x, 600);
            }
        }
    }

    private static class Point {
        final double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}