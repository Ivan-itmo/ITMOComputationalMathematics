package integration;

public class IntegrationResult {
    private final double value;
    private final int parts;
    private final double errorEpsilon;

    public IntegrationResult(double value, int parts, double errorEpsilon) {
        this.value = value;
        this.parts = parts;
        this.errorEpsilon = errorEpsilon;
    }

    public double getValue() {
        return value;
    }

    public int getParts() {
        return parts;
    }

    public double getErrorEpsilon() {
        return errorEpsilon;
    }
}
