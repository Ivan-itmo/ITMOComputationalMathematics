package ru.itmo.lab6.model;

import java.util.List;

public class MethodResult {
    private final String methodName;
    private final List<Double> values;
    private final double methodError;
    private final String errorDescription;

    public MethodResult(String methodName, List<Double> values, double methodError, String errorDescription) {
        this.methodName = methodName;
        this.values = values;
        this.methodError = methodError;
        this.errorDescription = errorDescription;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Double> getValues() {
        return values;
    }

    public double getMethodError() {
        return methodError;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
