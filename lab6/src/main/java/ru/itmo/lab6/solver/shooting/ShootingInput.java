package ru.itmo.lab6.solver.shooting;

public record ShootingInput(
        double x0,
        double xn,
        double y0,
        double yn,
        double h,
        double epsilon,
        double initialSlopeGuess1,
        double initialSlopeGuess2,
        SecondOrderEquation equation
) {
}
