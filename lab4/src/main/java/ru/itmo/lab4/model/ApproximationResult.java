package ru.itmo.lab4.model;

public class ApproximationResult {
    private final String name;
    private final boolean valid;
    private final String invalidReason;
    private final String formula;
    private final double[] coefficients;
    private final double[] phi;
    private final double[] epsilon;
    private final double s;
    private final double sigma;
    private final double r2;
    private final String r2Message;
    private final Double pearson;
    private final ModelFunction model;

    public ApproximationResult(
            String name,
            boolean valid,
            String invalidReason,
            String formula,
            double[] coefficients,
            double[] phi,
            double[] epsilon,
            double s,
            double sigma,
            double r2,
            String r2Message,
            Double pearson,
            ModelFunction model
    ) {
        this.name = name;
        this.valid = valid;
        this.invalidReason = invalidReason;
        this.formula = formula;
        this.coefficients = coefficients;
        this.phi = phi;
        this.epsilon = epsilon;
        this.s = s;
        this.sigma = sigma;
        this.r2 = r2;
        this.r2Message = r2Message;
        this.pearson = pearson;
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return valid;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public String getFormula() {
        return formula;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public double[] getPhi() {
        return phi;
    }

    public double[] getEpsilon() {
        return epsilon;
    }

    public double getS() {
        return s;
    }

    public double getSigma() {
        return sigma;
    }

    public double getR2() {
        return r2;
    }

    public String getR2Message() {
        return r2Message;
    }

    public Double getPearson() {
        return pearson;
    }

    public ModelFunction getModel() {
        return model;
    }
}
