package ru.itmo.lab4.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;

import ru.itmo.lab4.model.AnalysisReport;
import ru.itmo.lab4.model.ApproximationResult;
import ru.itmo.lab4.model.Point;

public class ChartService {
    public BufferedImage buildChart(AnalysisReport report, int width, int height) {
        int left = 90;
        int right = 50;
        int top = 50;
        int bottom = 170;

        List<Point> points = report.points();
        double minX = points.stream().mapToDouble(Point::x).min().orElse(0.0);
        double maxX = points.stream().mapToDouble(Point::x).max().orElse(1.0);
        double minY = points.stream().mapToDouble(Point::y).min().orElse(0.0);
        double maxY = points.stream().mapToDouble(Point::y).max().orElse(1.0);

        double dx = Math.max((maxX - minX) * 0.1, 0.1);
        double dy = Math.max((maxY - minY) * 0.1, 0.1);
        minX -= dx;
        maxX += dx;
        minY -= dy;
        maxY += dy;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(233, 238, 244));
        for (int i = 0; i <= 10; i++) {
            int x = left + i * (width - left - right) / 10;
            int y = top + i * (height - top - bottom) / 10;
            g.drawLine(x, top, x, height - bottom);
            g.drawLine(left, y, width - right, y);
        }

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(left, height - bottom, width - right, height - bottom);
        g.drawLine(left, top, left, height - bottom);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));

        for (int i = 0; i <= 10; i++) {
            double xValue = minX + i * (maxX - minX) / 10.0;
            double yValue = minY + i * (maxY - minY) / 10.0;
            int x = mapX(xValue, minX, maxX, width, left, right);
            int y = mapY(yValue, minY, maxY, height, top, bottom);
            g.drawString(String.format("%.3f", xValue), x - 15, height - bottom + 20);
            g.drawString(String.format("%.3f", yValue), 10, y + 5);
        }

        Color[] colors = {
                new Color(196, 54, 54),
                new Color(35, 119, 186),
                new Color(48, 156, 48),
                new Color(238, 132, 33),
                new Color(133, 94, 182),
                new Color(122, 86, 66)
        };

        int colorIndex = 0;
        for (ApproximationResult result : report.results()) {
            if (!result.isValid()) {
                continue;
            }
            g.setColor(colors[colorIndex % colors.length]);
            g.setStroke(new BasicStroke(2.5f));
            Path2D path = new Path2D.Double();
            boolean started = false;
            int steps = 240;

            for (int i = 0; i <= steps; i++) {
                double x = minX + i * (maxX - minX) / steps;
                double y = result.getModel().apply(x);
                if (!Double.isFinite(y)) {
                    started = false;
                    continue;
                }
                int px = mapX(x, minX, maxX, width, left, right);
                int py = mapY(y, minY, maxY, height, top, bottom);
                if (!started) {
                    path.moveTo(px, py);
                    started = true;
                } else {
                    path.lineTo(px, py);
                }
            }
            g.draw(path);
            colorIndex++;
        }

        g.setColor(Color.BLACK);
        for (Point point : points) {
            int px = mapX(point.x(), minX, maxX, width, left, right);
            int py = mapY(point.y(), minY, maxY, height, top, bottom);
            g.fillOval(px - 4, py - 4, 8, 8);
        }

        drawLegend(g, report, colors, left, height - bottom + 45, width - left - right);
        g.dispose();
        return image;
    }

    private int mapX(double x, double minX, double maxX, int width, int left, int right) {
        return left + (int) Math.round((x - minX) * (width - left - right) / (maxX - minX));
    }

    private int mapY(double y, double minY, double maxY, int height, int top, int bottom) {
        return height - bottom - (int) Math.round((y - minY) * (height - top - bottom) / (maxY - minY));
    }

    private void drawLegend(Graphics2D g, AnalysisReport report, Color[] colors, int left, int top, int availableWidth) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int itemWidth = Math.max(260, availableWidth / 2);
        int rowHeight = 24;
        int x = left;
        int y = top;
        int colorIndex = 0;
        g.setColor(Color.BLACK);
        g.drawString("Легенда:", left, top - 12);

        for (ApproximationResult result : report.results()) {
            if (!result.isValid()) {
                continue;
            }
            Color color = colors[colorIndex % colors.length];
            g.setColor(color);
            g.setStroke(new BasicStroke(3f));
            g.drawLine(x, y, x + 30, y);
            g.setColor(Color.BLACK);
            g.drawString(result.getName(), x + 40, y + 5);
            colorIndex++;
            x += itemWidth;
            if (x + itemWidth > left + availableWidth) {
                x = left;
                y += rowHeight;
            }
        }
    }
}
