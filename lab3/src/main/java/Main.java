import function.FunctionChoose;
import integration.IntegrationMethod;
import integration.IntegrationResult;
import integration.Integrator;
import ui.InputReader;
import ui.OutputWriter;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    private static final int startParts = 4;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        try (Scanner scanner = new Scanner(System.in)) {
            List<FunctionChoose> functions = FunctionChoose.getFunctions();
            InputReader inputReader = new InputReader(scanner);
            OutputWriter outputWriter = new OutputWriter();

            outputWriter.printTitle();

            FunctionChoose function = inputReader.chooseFunction(functions);
            IntegrationMethod method = inputReader.chooseMethod();

            double a;
            double b;
            while (true) {
                a = inputReader.readAB("Введите нижний предел интегрирования a: ");
                b = inputReader.readAB("Введите верхний предел интегрирования b: ");
                if (a <= b) {
                    break;
                }
                System.out.println("Нижний предел интегрирования не может быть больше верхнего.");
            }
            double epsilon = inputReader.readEpsilon("Введите требуемую точность: ");

            IntegrationResult result = Integrator.integrate(function, a, b, method, epsilon, startParts);

            outputWriter.printResult(function, method, a, b, result);
        } catch (IllegalArgumentException ex) {
            if ("Интеграл не существует.".equals(ex.getMessage())) {
                System.out.println("Интеграл не существует.");
            } else {
                System.out.println("Ошибка: " + ex.getMessage());
            }
        }
    }
}
