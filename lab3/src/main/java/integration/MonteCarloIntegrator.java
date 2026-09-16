package integration;

import function.FunctionChoose;

import java.util.concurrent.ThreadLocalRandom;

public final class MonteCarloIntegrator {
    private MonteCarloIntegrator() {
    }

    public static IntegrationResult integrate(FunctionChoose function, double a, double b, double epsilon, int startSamples, int maxSamples) {
        int n = Math.max(2, startSamples);
        if (n > maxSamples) {
            throw new IllegalArgumentException("Начальное число точек превышает максимально допустимое.");
        }

        double average = 0.0;
        double sumSquared = 0.0;
        double length = b - a;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 1; i <= n; i++) {
            double x = random.nextDouble(a, b);
            double y = function.calculate(x);
            double delta = y - average;
            average += delta / i;
            sumSquared += delta * (y - average);
        }

        while (n < maxSamples) {
            double sampleVariance = (n > 1) ? sumSquared / (n - 1) : 0.0;
            double standardError = Math.sqrt(sampleVariance / n) * Math.abs(length);
            double integral = average * length;

            if (standardError <= epsilon) {
                return new IntegrationResult(integral, n, standardError);
            }

            int target = Math.min(maxSamples, n * 2);
            for (int i = n + 1; i <= target; i++) {
                double x = random.nextDouble(a, b);
                double y = function.calculate(x);
                double delta = y - average;
                average += delta / i;
                sumSquared += delta * (y - average);
            }
            n = target;
        }

        double sampleVariance = (n > 1) ? sumSquared / (n - 1) : 0.0;
        double standardError = Math.sqrt(sampleVariance / n) * Math.abs(length);
        double integral = average * length;

        if (standardError <= epsilon) {
            return new IntegrationResult(integral, n, standardError);
        }

        throw new IllegalArgumentException("Не удалось достичь заданной точности. Попробуйте увеличить epsilon.");
    }
}
