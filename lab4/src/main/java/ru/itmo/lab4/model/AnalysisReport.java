package ru.itmo.lab4.model;

import java.util.List;

public record AnalysisReport(List<Point> points, List<ApproximationResult> results, ApproximationResult bestResult) {
}
