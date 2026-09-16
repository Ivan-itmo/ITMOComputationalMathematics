package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BurningResult extends Result {
    private final List<Double> sampledPoints;
    private final List<Double> acceptedPoints;

    public BurningResult(double root, double value, int iterations, boolean success, String methodName, String message) {
        this(root, value, iterations, success, methodName, message,
                Collections.emptyList(), Collections.emptyList());
    }

    public BurningResult(double root, double value, int iterations, boolean success, String methodName, String message, List<Double> sampledPoints, List<Double> acceptedPoints) {
        super(root, value, iterations, success, methodName, message);
        this.sampledPoints = Collections.unmodifiableList(new ArrayList<>(sampledPoints));
        this.acceptedPoints = Collections.unmodifiableList(new ArrayList<>(acceptedPoints));
    }

    public List<Double> getSampledPoints() {
        return sampledPoints;
    }

    public List<Double> getAcceptedPoints() {
        return acceptedPoints;
    }
}
