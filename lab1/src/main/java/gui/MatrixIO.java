package gui;

import java.io.*;
import java.math.BigDecimal;
import java.util.Scanner;

public class MatrixIO {
    public static FileData readFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Файл не найден");
        }
        if (!file.canRead()) {
            throw new IOException("Нет доступа к файлу (права на чтение)");
        }
        try (Scanner in = new Scanner(file)) {
            if (!in.hasNextInt()) {
                throw new IOException("Первая строка должна содержать целое число n");
            }
            int n = in.nextInt();
            in.nextLine();
            if (n <= 0 || n > 20) {
                throw new IOException("Размер системы должен быть от 1 до 20");
            }
            BigDecimal[][] A = new BigDecimal[n][n];
            BigDecimal[] b = new BigDecimal[n];
            for (int i = 0; i < n; i++) {
                if (!in.hasNextLine()) {
                    throw new IOException("Не хватает строки");
                }
                String line = in.nextLine().trim();
                if (line.isEmpty()) {
                    throw new IOException("Пустая строка");
                }
                String[] tokens = line.split("\\s+");
                int expectedTokens = n + 1;
                if (tokens.length < expectedTokens) {
                    throw new IOException("В строке недостаточно чисел");
                }
                for (int j = 0; j < n; j++) {
                    String token = tokens[j].replace(',', '.');
                    A[i][j] = new BigDecimal(token);
                }
                String bToken = tokens[n].replace(',', '.');
                b[i] = new BigDecimal(bToken);
            }
            return new FileData(n, A, b);
        } catch (NumberFormatException e) {
            throw new IOException("Некорректное число в файле", e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Некорректный файл: " + e.getMessage());
        }
    }
}