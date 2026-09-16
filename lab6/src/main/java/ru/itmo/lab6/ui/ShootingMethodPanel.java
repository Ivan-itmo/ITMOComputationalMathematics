package ru.itmo.lab6.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import ru.itmo.lab6.solver.shooting.SecondOrderEquation;
import ru.itmo.lab6.solver.shooting.ShootingInput;
import ru.itmo.lab6.solver.shooting.ShootingMethodSolver;
import ru.itmo.lab6.solver.shooting.ShootingResult;

public class ShootingMethodPanel extends JPanel {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000000");

    private final JComboBox<ShootingProblemDefinition> problemBox = new JComboBox<>();
    private final JTextField x0Field = new JTextField(8);
    private final JTextField xnField = new JTextField(8);
    private final JTextField y0Field = new JTextField(8);
    private final JTextField ynField = new JTextField(8);
    private final JTextField hField = new JTextField(8);
    private final JTextField epsilonField = new JTextField(8);
    private final JTextField slopeGuess1Field = new JTextField(8);
    private final JTextField slopeGuess2Field = new JTextField(8);
    private final JTextArea infoArea = new JTextArea(5, 30);
    private final JTextArea analysisArea = new JTextArea();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"i", "x", "y точн.", "y пристр.", "y'"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable resultTable = new JTable(tableModel);
    private final ShootingPlotPanel plotPanel = new ShootingPlotPanel();
    private final ShootingMethodSolver solver = new ShootingMethodSolver();

    public ShootingMethodPanel() {
        setLayout(new BorderLayout(10, 10));
        initProblemBox();
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
        updateProblemInfo();
    }

    private void initProblemBox() {
        problemBox.addItem(new ShootingProblemDefinition(
                "Задача 1",
                "y'' = y",
                "y = sinh(x)",
                0.0,
                1.0,
                0.0,
                Math.sinh(1.0),
                0.1,
                1e-6,
                0.5,
                1.5,
                (x, y, dy) -> y,
                Math::sinh
        ));
        problemBox.addItem(new ShootingProblemDefinition(
                "Задача 2",
                "y'' = -y",
                "y = sin(x)",
                0.0,
                Math.PI / 2.0,
                0.0,
                1.0,
                0.1,
                1e-6,
                0.5,
                1.5,
                (x, y, dy) -> -y,
                Math::sin
        ));
        problemBox.addActionListener(event -> updateProblemInfo());
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(buildInputPanel(), BorderLayout.NORTH);
        panel.add(buildInfoPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Исходные данные для метода пристрелки"));

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        firstRow.add(new JLabel("Задача:"));
        firstRow.add(problemBox);
        firstRow.add(new JLabel("x0:"));
        firstRow.add(x0Field);
        firstRow.add(new JLabel("xn:"));
        firstRow.add(xnField);
        firstRow.add(new JLabel("y0:"));
        firstRow.add(y0Field);
        firstRow.add(new JLabel("yn:"));
        firstRow.add(ynField);

        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        secondRow.add(new JLabel("h:"));
        secondRow.add(hField);
        secondRow.add(new JLabel("epsilon:"));
        secondRow.add(epsilonField);
        secondRow.add(new JLabel("s1:"));
        secondRow.add(slopeGuess1Field);
        secondRow.add(new JLabel("s2:"));
        secondRow.add(slopeGuess2Field);

        fieldsPanel.add(firstRow);
        fieldsPanel.add(secondRow);

        JButton solveButton = new JButton("Вычислить");
        solveButton.addActionListener(event -> solveProblem());
        solveButton.setPreferredSize(new Dimension(150, 34));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        buttonPanel.add(solveButton);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Справка по краевой задаче"));
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JScrollPane tableScroll = new JScrollPane(resultTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Таблица метода пристрелки"));
        tableScroll.setPreferredSize(new Dimension(1240, 280));

        JPanel plotContainer = new JPanel(new BorderLayout());
        plotContainer.setBorder(BorderFactory.createTitledBorder("График решения"));
        plotContainer.add(plotPanel, BorderLayout.CENTER);

        panel.add(tableScroll);
        panel.add(plotContainer);
        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Анализ метода пристрелки"));

        analysisArea.setRows(6);
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(analysisArea);
        scrollPane.setPreferredSize(new Dimension(1240, 150));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void updateProblemInfo() {
        ShootingProblemDefinition definition = (ShootingProblemDefinition) problemBox.getSelectedItem();
        if (definition == null) {
            infoArea.setText("");
            return;
        }

        x0Field.setText(FORMAT.format(definition.x0()).replace(',', '.'));
        xnField.setText(FORMAT.format(definition.xn()).replace(',', '.'));
        y0Field.setText(FORMAT.format(definition.y0()).replace(',', '.'));
        ynField.setText(FORMAT.format(definition.yn()).replace(',', '.'));
        hField.setText(FORMAT.format(definition.h()).replace(',', '.'));
        epsilonField.setText("0.000001");
        slopeGuess1Field.setText(FORMAT.format(definition.slopeGuess1()).replace(',', '.'));
        slopeGuess2Field.setText(FORMAT.format(definition.slopeGuess2()).replace(',', '.'));

        infoArea.setText(
                "Выбрано: " + definition.name() + "\n"
                        + "Краевая задача: " + definition.equationLabel() + "\n"
                        + "Точное решение: " + definition.exactLabel() + "\n"
                        + "Метод подбирает начальную производную y'(x0) так, чтобы выполнить условие y(xn) = yn."
        );
        analysisArea.setText("После нажатия кнопки \"Вычислить\" здесь появятся невязка на правой границе и найденная начальная производная.");
    }

    private void solveProblem() {
        try {
            ShootingProblemDefinition definition = (ShootingProblemDefinition) problemBox.getSelectedItem();
            if (definition == null) {
                throw new IllegalArgumentException("Выберите краевую задачу.");
            }

            ShootingInput problem = new ShootingInput(
                    parseField(x0Field, "x0"),
                    parseField(xnField, "xn"),
                    parseField(y0Field, "y0"),
                    parseField(ynField, "yn"),
                    parseField(hField, "h"),
                    parseField(epsilonField, "epsilon"),
                    parseField(slopeGuess1Field, "s1"),
                    parseField(slopeGuess2Field, "s2"),
                    definition.equation()
            );

            ShootingResult result = solver.solve(problem);
            List<Double> exactValues = calculateExactValues(result.xValues(), definition.exactSolution());
            fillTable(result, exactValues);
            plotPanel.setData(result.xValues(), exactValues, result.yValues());
            updateAnalysis(result, exactValues, parseField(epsilonField, "epsilon"));
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Не удалось выполнить расчет методом пристрелки: " + exception.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private List<Double> calculateExactValues(List<Double> xValues, DoubleUnaryOperator exactSolution) {
        List<Double> values = new ArrayList<>(xValues.size());
        for (double x : xValues) {
            values.add(exactSolution.applyAsDouble(x));
        }
        return values;
    }

    private void fillTable(ShootingResult result, List<Double> exactValues) {
        tableModel.setRowCount(0);
        for (int i = 0; i < result.xValues().size(); i++) {
            tableModel.addRow(new Object[]{
                    i,
                    FORMAT.format(result.xValues().get(i)),
                    FORMAT.format(exactValues.get(i)),
                    FORMAT.format(result.yValues().get(i)),
                    FORMAT.format(result.derivativeValues().get(i))
            });
        }
    }

    private void updateAnalysis(ShootingResult result, List<Double> exactValues, double epsilon) {
        double maxError = maxAbsoluteError(result.yValues(), exactValues);
        analysisArea.setText(
                "Найденная начальная производная y'(x0) = " + FORMAT.format(result.initialSlope()) + "\n"
                        + "Невязка на правой границе |y(xn) - yn| = " + FORMAT.format(Math.abs(result.boundaryResidual()))
                        + verdict(Math.abs(result.boundaryResidual()), epsilon) + "\n"
                        + "Максимальная ошибка относительно точного решения = " + FORMAT.format(maxError)
        );
        analysisArea.setCaretPosition(0);
    }

    private double maxAbsoluteError(List<Double> actual, List<Double> expected) {
        double max = 0.0;
        for (int i = 0; i < actual.size(); i++) {
            max = Math.max(max, Math.abs(actual.get(i) - expected.get(i)));
        }
        return max;
    }

    private String verdict(double error, double epsilon) {
        return error <= epsilon ? " (удовлетворяет epsilon)" : " (не удовлетворяет epsilon)";
    }

    private double parseField(JTextField field, String fieldName) {
        try {
            return Double.parseDouble(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Поле " + fieldName + " должно содержать число.");
        }
    }

    private record ShootingProblemDefinition(
            String name,
            String equationLabel,
            String exactLabel,
            double x0,
            double xn,
            double y0,
            double yn,
            double h,
            double epsilon,
            double slopeGuess1,
            double slopeGuess2,
            SecondOrderEquation equation,
            DoubleUnaryOperator exactSolution
    ) {
        @Override
        public String toString() {
            return name + ": " + equationLabel;
        }
    }
}
