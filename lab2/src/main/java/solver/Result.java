package solver;

public class Result {
    private double root;
    private double functionValue;
    private double error;
    private int iterations;
    private boolean success;
    private String methodName;
    private String message;
    private boolean systemResult;

    public Result(double root, double value, int iterations, boolean success, String methodName) {
        this(root, value, iterations, success, methodName, "", false);
    }

    public Result(double root, double value, int iterations, boolean success, String methodName, String message) {
        this(root, value, iterations, success, methodName, message, false);
    }

    public static Result system(double root, double error, int iterations, boolean success, String methodName) {
        return new Result(root, error, iterations, success, methodName, "", true);
    }

    public static Result system(double root, double error, int iterations, boolean success, String methodName, String message) {
        return new Result(root, error, iterations, success, methodName, message, true);
    }

    private Result(double root, double value, int iterations, boolean success, String methodName, String message, boolean systemResult) {
        this.root = root;
        this.iterations = iterations;
        this.success = success;
        this.methodName = methodName;
        this.message = message;
        this.systemResult = systemResult;
        if (systemResult) {
            this.error = value;
            this.functionValue = 0;
        } else {
            this.functionValue = value;
            this.error = 0;
        }
    }

    public double getRoot() { return root; }
    public double getFunctionValue() { return functionValue; }
    public double getError() { return error; }
    public int getIterations() { return iterations; }
    public boolean isSuccess() { return success; }
    public String getMethodName() { return methodName; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        if (success) {
            if (systemResult) {
                return String.format("%s: значение = %.6f, Δ = %.6f, итераций: %d", methodName, root, error, iterations);
            } else {
                return String.format("%s: x* = %.6f, f(x*) = %.6f, итераций: %d", methodName, root, functionValue, iterations);
            }
        } else {
            return String.format("%s: %s, итераций: %d", methodName, message, iterations);
        }
    }
}
