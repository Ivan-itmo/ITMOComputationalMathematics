package ui;

import function.FunctionChoose;
import integration.IntegrationMethod;
import integration.IntegrationResult;

public class OutputWriter {
    public void printTitle() {System.out.println("Численное интегрирование");
        System.out.println();
    }

    public void printResult(FunctionChoose function, IntegrationMethod method, double a, double b, IntegrationResult result) {
        System.out.println();
        System.out.println("Результат:");
        System.out.println("Функция: " + function.getName());
        System.out.println("Метод: " + method.getName());
        System.out.printf("Интеграл на [%.6f; %.6f] = %.10f%n", a, b, result.getValue());
        System.out.println("Число разбиений n = " + result.getParts());
        System.out.printf("Оценка погрешности по правилу Рунге: %.10e%n", result.getErrorEpsilon());
    }
}
