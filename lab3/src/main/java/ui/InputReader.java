package ui;

import function.FunctionChoose;
import integration.IntegrationMethod;

import java.util.List;
import java.util.Scanner;

public class InputReader {
    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public FunctionChoose chooseFunction(List<FunctionChoose> functions) {
        System.out.println("Выберите функцию:");
        for (int i = 0; i < functions.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, functions.get(i).getName());
        }

        while (true) {
            System.out.print("Введите номер функции: ");
            if (scanner.hasNextInt()) {
                int index = scanner.nextInt();
                if (index >= 1 && index <= functions.size()) {
                    return functions.get(index - 1);
                }
            } else {
                scanner.next();
            }
            System.out.printf("Введите целое число от %d до %d.%n", 1, functions.size());
        }
    }

    public IntegrationMethod chooseMethod() {
        IntegrationMethod[] methods = IntegrationMethod.values();

        System.out.println();
        System.out.println("Выберите метод интегрирования:");
        for (int i = 0; i < methods.length; i++) {
            System.out.printf("%d. %s%n", i + 1, methods[i].getName());
        }

        while (true) {
            System.out.print("Введите номер метода: ");
            if (scanner.hasNextInt()) {
                int index = scanner.nextInt();
                if (index >= 1 && index <= methods.length) {
                    return methods[index - 1];
                }
            } else {
                scanner.next();
            }
            System.out.printf("Введите целое число от %d до %d.%n", 1, methods.length);
        }
    }

    public double readAB(String input) {
        while (true) {
            System.out.print(input);
            if (scanner.hasNextDouble()) {
                return scanner.nextDouble();
            }
            scanner.next();
            System.out.println("Введите корректное число.");
        }
    }

    public double readEpsilon(String input) {
        while (true) {
            double value = readAB(input);
            if (value > 0) {
                return value;
            }
            System.out.println("Точность должна быть положительным числом.");
        }
    }
}
