package ru.itmo.lab6.model;

public record InputData(
        DifferentialEquation equation,
        double x0,
        double y0,
        double xn,
        double h,
        double epsilon
) {
}
