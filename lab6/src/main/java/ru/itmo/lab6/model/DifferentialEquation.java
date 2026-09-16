package ru.itmo.lab6.model;

import ru.itmo.lab6.function.EquationFunction;
import ru.itmo.lab6.function.ExactSolution;

public class DifferentialEquation {
    private final String name;
    private final String formula;
    private final String exactFormula;
    private final EquationFunction function;
    private final ExactSolution exactSolution;

    public DifferentialEquation(String name, String formula, String exactFormula, EquationFunction function, ExactSolution exactSolution) {
        this.name = name;
        this.formula = formula;
        this.exactFormula = exactFormula;
        this.function = function;
        this.exactSolution = exactSolution;
    }

    public String getName() {
        return name;
    }

    public String getFormula() {
        return formula;
    }

    public String getExactFormula() {
        return exactFormula;
    }

    public double derivative(double x, double y) {
        return function.apply(x, y);
    }

    public double exactValue(double x0, double y0, double x) {
        return exactSolution.apply(x0, y0, x);
    }

    @Override
    public String toString() {
        return name + ": " + formula;
    }
}
