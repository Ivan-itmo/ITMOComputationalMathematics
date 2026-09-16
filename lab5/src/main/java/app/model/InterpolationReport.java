package app.model;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

public record InterpolationReport(
        DataSet dataSet,
        double argument,
        List<MethodResult> results,
        double[][] differenceTable,
        String[] differenceHeaders,
        String analysis,
        Map<String, DoubleUnaryOperator> plotFunctions
) {
}
