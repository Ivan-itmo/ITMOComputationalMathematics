package app.model;

import java.util.function.DoubleUnaryOperator;

public record FunctionItem(String name, DoubleUnaryOperator function) {
    @Override
    public String toString() {
        return name;
    }
}
