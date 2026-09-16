package ru.itmo.lab6.solver.shooting;

import java.util.List;

record IntegrationResult(
        List<Double> xValues,
        List<Double> yValues,
        List<Double> derivativeValues,
        double initialSlope,
        double residual
) {
}
