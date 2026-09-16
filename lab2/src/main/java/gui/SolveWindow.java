package gui;

import functions.Equation;
import functions.Systems;
import solver.BurningResult;
import solver.EquationSolver;
import solver.Result;
import solver.SystemSolver;
import solver.BurningSolver;
import utils.InputFile;
import utils.WriteFile;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

public class SolveWindow extends JFrame {
    private final JComboBox<String> modeBox;
    private final JTextField outputFileField;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    private final JComboBox<String> equationBox;
    private final JComboBox<String> equationMethodBox;
    private final JTextField equationAField;
    private final JTextField equationBField;
    private final JTextField equationEpsilonField;
    private final JTextField equationInputFileField;
    private final JCheckBox equationFileCheck;
    private final EquationGraph graphPanel;

    private final JComboBox<String> systemBox;
    private final JTextField systemX0Field;
    private final JTextField systemY0Field;
    private final JTextField systemEpsilonField;
    private final JCheckBox systemFileCheck;
    private final SystemGraph systemGraphPanel;

    private final JTextArea outputArea;

    public SolveWindow() {
        setTitle("Численные методы");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        modeBox = new JComboBox<>(new String[] {
                "Одно нелинейное уравнение",
                "Система нелинейных уравнений"
        });
        outputFileField = new JTextField("result");

        JPanel topPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(new JLabel("Режим"));
        topPanel.add(modeBox);
        topPanel.add(new JLabel(""));
        topPanel.add(new JLabel("Файл для записи"));
        topPanel.add(outputFileField);
        JButton browseOutputButton = new JButton("Обзор...");
        browseOutputButton.addActionListener(e -> chooseOutputFile());
        topPanel.add(browseOutputButton);

        equationBox = new JComboBox<>(equationItems());
        equationMethodBox = new JComboBox<>(new String[] {
                "Метод половинного деления",
                "Метод Ньютона",
                "Метод простой итерации",
                "Метод обжига"
        });
        equationAField = new JTextField("-2");
        equationBField = new JTextField("0");
        equationEpsilonField = new JTextField("0.001");
        equationInputFileField = new JTextField("input");
        equationFileCheck = new JCheckBox();
        graphPanel = new EquationGraph();

        systemBox = new JComboBox<>(systemItems());
        systemX0Field = new JTextField("0.5");
        systemY0Field = new JTextField("0.5");
        systemEpsilonField = new JTextField("0.001");
        systemFileCheck = new JCheckBox();
        systemGraphPanel = new SystemGraph();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        cardPanel.add(buildEquationPanel(), "equation");
        cardPanel.add(buildSystemPanel(), "system");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Результат"));
        scrollPane.setPreferredSize(new Dimension(800, 220));

        add(topPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        modeBox.addActionListener(e -> switchMode());
        outputFileField.addActionListener(e -> updateCheckText());
        equationBox.addActionListener(e -> refreshGraph(null));
        equationAField.addActionListener(e -> refreshGraph(null));
        equationBField.addActionListener(e -> refreshGraph(null));
        systemBox.addActionListener(e -> refreshSystemGraph(null));
        systemX0Field.addActionListener(e -> refreshSystemGraph(null));
        systemY0Field.addActionListener(e -> refreshSystemGraph(null));

        switchMode();
        updateCheckText();
        refreshGraph(null);
        refreshSystemGraph(null);
    }

    private JPanel buildEquationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel left = new JPanel(new GridLayout(10, 2, 10, 10));
        left.add(new JLabel("Функция"));
        left.add(equationBox);
        left.add(new JLabel("Метод"));
        left.add(equationMethodBox);
        left.add(new JLabel("Левая граница a"));
        left.add(equationAField);
        left.add(new JLabel("Правая граница b"));
        left.add(equationBField);
        left.add(new JLabel("Точность ε"));
        left.add(equationEpsilonField);
        left.add(new JLabel("Файл для чтения"));
        left.add(equationInputFileField);
        left.add(new JLabel("Выбор файла"));

        JButton browseInputButton = new JButton("Обзор...");
        browseInputButton.addActionListener(e -> chooseEquationInputFile());
        left.add(browseInputButton);
        left.add(equationFileCheck);

        JButton loadButton = new JButton("Считать из файла");
        loadButton.addActionListener(e -> {
            try {
                loadEquationFromFile();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        left.add(loadButton);

        JButton graphButton = new JButton("Обновить график");
        graphButton.addActionListener(e -> refreshGraph(null));
        left.add(graphButton);

        JButton solveButton = new JButton("Решить уравнение");
        solveButton.addActionListener(e -> solveEquation());
        left.add(solveButton);

        left.setPreferredSize(new Dimension(300, 0));
        panel.add(left, BorderLayout.WEST);
        panel.add(graphPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSystemPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel left = new JPanel(new GridLayout(6, 2, 10, 10));
        left.add(new JLabel("Система"));
        left.add(systemBox);
        left.add(new JLabel("Начальное приближение x0"));
        left.add(systemX0Field);
        left.add(new JLabel("Начальное приближение y0"));
        left.add(systemY0Field);
        left.add(new JLabel("Точность ε"));
        left.add(systemEpsilonField);
        left.add(systemFileCheck);

        JButton solveButton = new JButton("Решить систему");
        solveButton.addActionListener(e -> solveSystem());
        left.add(solveButton);

        left.setPreferredSize(new Dimension(300, 0));
        panel.add(left, BorderLayout.WEST);
        panel.add(systemGraphPanel, BorderLayout.CENTER);
        return panel;
    }

    private void switchMode() {
        if (modeBox.getSelectedIndex() == 0) {
            cardLayout.show(cardPanel, "equation");
        } else {
            cardLayout.show(cardPanel, "system");
        }
    }

    private void updateCheckText() {
        String name = outputFileField.getText().trim();
        if (name.isEmpty()) {
            name = "не указан";
        }
        String text = "Записать в файл: " + name;
        equationFileCheck.setText(text);
        systemFileCheck.setText(text);
    }

    private void solveEquation() {
        try {
            int funcIndex = equationBox.getSelectedIndex();
            int method = equationMethodBox.getSelectedIndex() + 1;
            double a = parseDouble(equationAField.getText(), "левая граница a");
            double b = parseDouble(equationBField.getText(), "правая граница b");
            double epsilon = parseDouble(equationEpsilonField.getText(), "точность ε");

            if (a >= b) {
                throw new IllegalArgumentException("Должно выполняться a < b.");
            }
            if (epsilon <= 0) {
                throw new IllegalArgumentException("Точность ε должна быть положительной.");
            }

            Result result;
            if (method == 1) {
                result = EquationSolver.bisection(funcIndex, a, b, epsilon);
            } else if (method == 2) {
                result = EquationSolver.newton(funcIndex, a, b, epsilon, 100);
            } else if (method == 3) {
                result = EquationSolver.simpleIteration(funcIndex, Double.NaN, a, b, epsilon, 100);
            } else {
                BurningResult burningResult = BurningSolver.solveDetailed(funcIndex, a, b, epsilon);
                result = burningResult;
                new AnnealingGraphWindow(funcIndex, a, b, burningResult).setVisible(true);
            }

            String output = equationText(result);
            outputArea.setText(output);
            refreshGraph(result);
            if (equationFileCheck.isSelected()) {
                saveOutput(output);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void loadEquationFromFile() {
        InputFile.EquationData data = InputFile.readEquation(equationInputFileField.getText().trim());
        equationBox.setSelectedIndex(data.getEquationNumber() - 1);
        equationMethodBox.setSelectedIndex(data.getMethodNumber() - 1);
        equationAField.setText(String.valueOf(data.getA()));
        equationBField.setText(String.valueOf(data.getB()));
        equationEpsilonField.setText(String.valueOf(data.getEpsilon()));
        refreshGraph(null);
    }

    private void chooseEquationInputFile() {
        JFileChooser chooser = new JFileChooser();
        String currentPath = equationInputFileField.getText().trim();
        if (!currentPath.isEmpty()) {
            chooser.setSelectedFile(new java.io.File(currentPath));
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            equationInputFileField.setText(chooser.getSelectedFile().getPath());
        }
    }

    private void chooseOutputFile() {
        JFileChooser chooser = new JFileChooser();
        String currentPath = outputFileField.getText().trim();
        if (!currentPath.isEmpty()) {
            chooser.setSelectedFile(new java.io.File(currentPath));
        }

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputFileField.setText(chooser.getSelectedFile().getPath());
            updateCheckText();
        }
    }

    private void solveSystem() {
        try {
            int sysIndex = systemBox.getSelectedIndex();
            double x0 = parseDouble(systemX0Field.getText(), "начальное приближение x0");
            double y0 = parseDouble(systemY0Field.getText(), "начальное приближение y0");
            double epsilon = parseDouble(systemEpsilonField.getText(), "точность ε");

            if (epsilon <= 0) {
                throw new IllegalArgumentException("Точность ε должна быть положительной.");
            }

            Result[] results = SystemSolver.simpleIteration(sysIndex, x0, y0, epsilon, 100);
            String output = systemText(results);
            outputArea.setText(output);
            refreshSystemGraph(results);
            if (systemFileCheck.isSelected()) {
                saveOutput(output);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private String[] equationItems() {
        String[] items = new String[Equation.getLength()];
        for (int i = 0; i < Equation.getLength(); i++) {
            items[i] = (i + 1) + ". f(x) = " + Equation.getEquation(i);
        }
        return items;
    }

    private String[] systemItems() {
        String[] items = new String[Systems.getLength()];
        for (int i = 0; i < Systems.getLength(); i++) {
            String[] system = Systems.getSystem(i);
            items[i] = (i + 1) + ". { " + system[0] + "; " + system[1] + " }";
        }
        return items;
    }

    private double parseDouble(String text, String field) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное число в поле: " + field + ".");
        }
    }

    private String equationText(Result result) {
        String output = "\nИтоги\n";
        if (result.isSuccess()) {
            output += "Корень: x* = " + String.format("%.6f", result.getRoot()) + "\n" +
                    "f(x*) = " + String.format("%.6f", result.getFunctionValue()) + "\n";
        }
        output += "Число итераций: " + result.getIterations();
        if (!result.getMessage().isEmpty()) {
            output += "\nСообщение: " + result.getMessage();
        }
        return output;
    }

    private String systemText(Result[] results) {
        return "\nИтоги\n" +
                "x = " + String.format("%.6f", results[0].getRoot()) + "\n" +
                "y = " + String.format("%.6f", results[1].getRoot()) + "\n" +
                "Число итераций: " + results[0].getIterations() + "\n" +
                "Δx = " + String.format("%.6f", results[0].getError()) + "\n" +
                "Δy = " + String.format("%.6f", results[1].getError()) +
                (results[0].getMessage().isEmpty() ? "" : "\nСообщение: " + results[0].getMessage());
    }

    private void refreshGraph(Result result) {
        int funcIndex = equationBox.getSelectedIndex();
        double a = safeDouble(equationAField.getText(), -2);
        double b = safeDouble(equationBField.getText(), 2);
        if (a >= b) {
            b = a + 1;
        }
        Double root = result != null && result.isSuccess() ? result.getRoot() : null;
        graphPanel.updatePlot(funcIndex, a, b, root);
    }

    private void refreshSystemGraph(Result[] results) {
        int sysIndex = systemBox.getSelectedIndex();
        double x0 = safeDouble(systemX0Field.getText(), 0);
        double y0 = safeDouble(systemY0Field.getText(), 0);
        Double rootX = results != null && results[0].isSuccess() ? results[0].getRoot() : null;
        Double rootY = results != null && results[1].isSuccess() ? results[1].getRoot() : null;
        systemGraphPanel.updatePlot(sysIndex, x0, y0, rootX, rootY);
    }

    private double safeDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private void saveOutput(String output) {
        String filename = outputFileField.getText().trim();
        if (filename.isEmpty()) {
            throw new IllegalArgumentException("Укажите файл для записи результата.");
        }
        WriteFile.writeToFile(filename, output);
        outputArea.setText(output + "\n\nСохранено.");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}
