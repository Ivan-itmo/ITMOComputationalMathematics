package utils;

import functions.Equation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

public class InputFile {
    public static EquationData readEquation(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Укажите файл для чтения.");
        }

        try (Scanner scanner = new Scanner(new File(filename))) {
            scanner.useLocale(Locale.US);
            if (!scanner.hasNextInt()) {
                throw new IllegalArgumentException("В файле недостаточно данных.");
            }
            int equationNumber = scanner.nextInt();

            if (!scanner.hasNextInt()) {
                throw new IllegalArgumentException("В файле недостаточно данных.");
            }
            int methodNumber = scanner.nextInt();

            if (!scanner.hasNextDouble()) {
                throw new IllegalArgumentException("В файле недостаточно данных.");
            }
            double a = scanner.nextDouble();

            if (!scanner.hasNextDouble()) {
                throw new IllegalArgumentException("В файле недостаточно данных.");
            }
            double b = scanner.nextDouble();

            if (!scanner.hasNextDouble()) {
                throw new IllegalArgumentException("В файле недостаточно данных.");
            }
            double epsilon = scanner.nextDouble();

            if (equationNumber < 1 || equationNumber > Equation.getLength()) {
                throw new IllegalArgumentException("Номер уравнения должен быть от 1 до " + Equation.getLength() + ".");
            }
            if (methodNumber < 1 || methodNumber > 3) {
                throw new IllegalArgumentException("Номер метода должен быть от 1 до 3.");
            }

            return new EquationData(equationNumber, methodNumber, a, b, epsilon);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Файл не найден.");
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Нет прав доступа для чтения файла.");
        }
    }

    public static class EquationData {
        private final int equationNumber;
        private final int methodNumber;
        private final double a;
        private final double b;
        private final double epsilon;
        public EquationData(int equationNumber, int methodNumber, double a, double b, double epsilon) {
            this.equationNumber = equationNumber;
            this.methodNumber = methodNumber;
            this.a = a;
            this.b = b;
            this.epsilon = epsilon;
        }

        public int getEquationNumber() {
            return equationNumber;
        }

        public int getMethodNumber() {
            return methodNumber;
        }

        public double getA() {
            return a;
        }

        public double getB() {
            return b;
        }

        public double getEpsilon() {
            return epsilon;
        }
    }
}
