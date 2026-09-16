package function;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

public final class FunctionChoose {
    private final String name;
    private final DoubleUnaryOperator function;
    private final Double singularPoint;
    private final double singularPower;

    private FunctionChoose(String name, DoubleUnaryOperator function, Double singularPoint, double singularPower) {
        this.name = name;
        this.function = function;
        this.singularPoint = singularPoint;
        this.singularPower = singularPower;
    }

    private FunctionChoose(String name, DoubleUnaryOperator function) {
        this(name, function, null, 0.0);
    }

    public static List<FunctionChoose> getFunctions() {
        return List.of(
                new FunctionChoose("x^2", x -> x * x),
                new FunctionChoose("x^3 - 2x + 1", x -> x * x * x - 2 * x + 1),
                new FunctionChoose("e^x", Math::exp),
                new FunctionChoose("1 / (x^2 + 1)", x -> 1.0 / (x * x + 1.0)),
                new FunctionChoose("1 / sqrt(x)", x -> 1.0 / Math.sqrt(x), 0.0, 0.5),
                new FunctionChoose("1 / sqrt(1 - x)", x -> 1.0 / Math.sqrt(1.0 - x), 1.0, 0.5),
                new FunctionChoose("1 / sqrt(|x - 0.5|)", x -> 1.0 / Math.sqrt(Math.abs(x - 0.5)), 0.5, 0.5),
                new FunctionChoose("(x^2 - 1) / (x - 1), x != 1", x -> (x * x - 1.0) / (x - 1.0), 1.0, 0.0),
                new FunctionChoose("1 / |x - 0.5|", x -> 1.0 / Math.abs(x - 0.5), 0.5, 1.0)
        );
    }

    public double calculate(double x) {
        return function.applyAsDouble(x);
    }

    public String getName() {
        return name;
    }

    public FunctionBreak whereGap(double a, double b) {
        if (singularPoint == null) {
            return FunctionBreak.NOTHING;
        }
        double left = Math.min(a, b);
        double right = Math.max(a, b);
        double eps = 1e-12 * Math.max(1.0, Math.max(Math.abs(left), Math.abs(right)));
        if (singularPoint < left - eps || singularPoint > right + eps) {
            return FunctionBreak.NOTHING;
        }
        if (Math.abs(singularPoint - left) <= eps) {
            return FunctionBreak.A;
        }
        if (Math.abs(singularPoint - right) <= eps) {
            return FunctionBreak.B;
        }
        return FunctionBreak.INSIDE;
    }

    public boolean isDesicion(double a, double b) {
        FunctionBreak singularityCase = whereGap(a, b);
        if (singularityCase == FunctionBreak.NOTHING) {
            return true;
        }
        return singularPower < 1.0;
    }

    public Double getGapPoint() {
        return singularPoint;
    }
}
