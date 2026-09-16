package app.service;

import app.model.FunctionItem;

import java.util.List;

public final class FunctionList {
    private FunctionList() {
    }

    public static List<FunctionItem> functions() {
        return List.of(
                new FunctionItem("sin(x)", Math::sin),
                new FunctionItem("cos(x)", Math::cos),
                new FunctionItem("x^3 - 2x + 1", x -> x * x * x - 2.0 * x + 1.0),
                new FunctionItem("exp(x)", Math::exp)
        );
    }
}
