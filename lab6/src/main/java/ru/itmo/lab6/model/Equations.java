package ru.itmo.lab6.model;

import java.util.List;

public final class Equations {
    private Equations() {
    }

    public static List<DifferentialEquation> getEquations() {
        return List.of(
                new DifferentialEquation(
                        "Уравнение 1",
                        "y' = x + y",
                        "y = (y0 + x0 + 1) * e^(x - x0) - x - 1",
                        (x, y) -> x + y,
                        (x0, y0, x) -> (y0 + x0 + 1.0) * Math.exp(x - x0) - x - 1.0
                ),
                new DifferentialEquation(
                        "Уравнение 2",
                        "y' = y - x^2 + 1",
                        "y = (x + 1)^2 + (y0 - (x0 + 1)^2) * e^(x - x0)",
                        (x, y) -> y - x * x + 1.0,
                        (x0, y0, x) -> (x + 1.0) * (x + 1.0) + (y0 - (x0 + 1.0) * (x0 + 1.0)) * Math.exp(x - x0)
                ),
                new DifferentialEquation(
                        "Уравнение 3",
                        "y' = x * y",
                        "y = y0 * e^((x^2 - x0^2) / 2)",
                        (x, y) -> x * y,
                        (x0, y0, x) -> y0 * Math.exp((x * x - x0 * x0) / 2.0)
                )
        );
    }
}
