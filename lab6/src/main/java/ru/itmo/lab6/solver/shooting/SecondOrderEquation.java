package ru.itmo.lab6.solver.shooting;

@FunctionalInterface
public interface SecondOrderEquation {
    double apply(double x, double y, double firstDerivative);
}
