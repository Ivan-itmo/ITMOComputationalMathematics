package ru.itmo.lab6.solver.shooting;

import java.util.List;

public record ShootingResult(
        List<Double> xValues,
        List<Double> yValues,
        List<Double> derivativeValues,
        double initialSlope,
        double boundaryResidual
) {
}
