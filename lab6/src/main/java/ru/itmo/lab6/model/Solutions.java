package ru.itmo.lab6.model;

import java.util.List;

public record Solutions(
        List<Double> xValues,
        List<Double> exactValues,
        MethodResult improvedEuler,
        MethodResult rungeKutta,
        MethodResult milne
) {
}
