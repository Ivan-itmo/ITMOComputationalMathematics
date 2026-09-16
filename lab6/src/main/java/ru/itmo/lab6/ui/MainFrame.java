package ru.itmo.lab6.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import ru.itmo.lab6.model.DifferentialEquation;
import ru.itmo.lab6.model.Equations;
import ru.itmo.lab6.model.InputData;
import ru.itmo.lab6.model.MethodResult;
import ru.itmo.lab6.model.Solutions;
import ru.itmo.lab6.solver.DifferentialEquationSolver;

public class MainFrame extends JFrame {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000000");
    private final JComboBox<DifferentialEquation> equationBox = new JComboBox<>();
    private final JTextField x0Field = new JTextField("0", 8);
    private final JTextField y0Field = new JTextField("1", 8);
    private final JTextField xnField = new JTextField("1", 8);
    private final JTextField hField = new JTextField("0.1", 8);
    private final JTextField epsilonField = new JTextField("0.0001", 8);
    private final JTextArea infoArea = new JTextArea(5, 30);
    private final JTextArea analysisArea = new JTextArea();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"i", "x", "y точн.", "Эйлер", "РК4", "Милн"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable resultTable = new JTable(tableModel);
    private final PlotPanel eulerPlotPanel = new PlotPanel("Эйлер", new Color(192, 57, 43), Solutions::improvedEuler);
    private final PlotPanel rk4PlotPanel = new PlotPanel("РК4", new Color(41, 128, 185), Solutions::rungeKutta);
    private final PlotPanel milnePlotPanel = new PlotPanel("Милн", new Color(243, 156, 18), Solutions::milne);
    private final ShootingMethodPanel shootingMethodPanel = new ShootingMethodPanel();
    private final DifferentialEquationSolver solver = new DifferentialEquationSolver();

    public MainFrame() {
        super("Лабораторная работа 6: Решение ОДУ");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1360, 900));
        initEquationBox();
        buildUi();
        setSize(1400, 920);
        setLocationRelativeTo(null);
    }

    private void initEquationBox() {
        for (DifferentialEquation equation : Equations.getEquations()) {
            equationBox.addItem(equation);
        }
        equationBox.addActionListener(event -> updateEquationInfo());
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        JTabbedPane methodTabs = new JTabbedPane();
        methodTabs.addTab("Задача Коши", buildInitialValueProblemPanel());
        methodTabs.addTab("Метод пристрелки", shootingMethodPanel);
        add(methodTabs, BorderLayout.CENTER);

        updateEquationInfo();
    }

    private JPanel buildInitialValueProblemPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        topPanel.add(buildInputPanel(), BorderLayout.NORTH);
        topPanel.add(buildInfoPanel(), BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(buildCenterPanel(), BorderLayout.CENTER);
        panel.add(buildBottomPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Исходные данные"));

        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        fieldsPanel.add(new JLabel("Уравнение:"));
        fieldsPanel.add(equationBox);
        fieldsPanel.add(new JLabel("x0:"));
        fieldsPanel.add(x0Field);
        fieldsPanel.add(new JLabel("y0:"));
        fieldsPanel.add(y0Field);
        fieldsPanel.add(new JLabel("xn:"));
        fieldsPanel.add(xnField);
        fieldsPanel.add(new JLabel("h:"));
        fieldsPanel.add(hField);
        fieldsPanel.add(new JLabel("epsilon:"));
        fieldsPanel.add(epsilonField);

        JButton solveButton = new JButton("Вычислить");
        solveButton.addActionListener(event -> solveEquation());
        solveButton.setPreferredSize(new Dimension(150, 34));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        buttonPanel.add(solveButton);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Справка по выбранному уравнению"));
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
        tableScroll.setBorder(BorderFactory.createTitledBorder("Таблица приближенных значений"));
        tableScroll.setPreferredSize(new Dimension(1240, 280));

        JTabbedPane plotTabs = new JTabbedPane();
        plotTabs.addTab("Эйлер", wrapPlotPanel(eulerPlotPanel));
        plotTabs.addTab("РК4", wrapPlotPanel(rk4PlotPanel));
        plotTabs.addTab("Милн", wrapPlotPanel(milnePlotPanel));

        JPanel plotContainer = new JPanel(new BorderLayout());
        plotContainer.setBorder(BorderFactory.createTitledBorder("Графики решений"));
        plotContainer.add(plotTabs, BorderLayout.CENTER);

        panel.add(tableScroll);
        panel.add(plotContainer);
        return panel;
    }

    private JPanel wrapPlotPanel(PlotPanel plotPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(plotPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Оценка точности и анализ"));

        analysisArea.setRows(6);
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(analysisArea);
        scrollPane.setPreferredSize(new Dimension(1240, 150));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void updateEquationInfo() {
        DifferentialEquation equation = (DifferentialEquation) equationBox.getSelectedItem();
        if (equation == null) {
            infoArea.setText("");
            return;
        }
        infoArea.setText(
                "Выбрано: " + equation.getName() + "\n"
                        + "ОДУ: " + equation.getFormula() + "\n"
                        + "Точное решение: " + equation.getExactFormula()
        );
        analysisArea.setText("После нажатия кнопки \"Вычислить\" здесь появятся оценки погрешности и краткий анализ методов.");
    }

    private void solveEquation() {
        try {
            InputData input = readInput();
            Solutions solution = solver.solve(input);
            fillTable(solution);
            eulerPlotPanel.setSolution(solution);
            rk4PlotPanel.setSolution(solution);
            milnePlotPanel.setSolution(solution);
            updateAnalysis(solution, input.epsilon());
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Не удалось выполнить расчет: " + exception.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private InputData readInput() {
        DifferentialEquation equation = (DifferentialEquation) equationBox.getSelectedItem();
        double x0 = parseField(x0Field, "x0");
        double y0 = parseField(y0Field, "y0");
        double xn = parseField(xnField, "xn");
        double h = parseField(hField, "h");
        double epsilon = parseField(epsilonField, "epsilon");
        return new InputData(equation, x0, y0, xn, h, epsilon);
    }

    private double parseField(JTextField field, String fieldName) {
        try {
            return Double.parseDouble(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Поле " + fieldName + " должно содержать число.");
        }
    }

    private void fillTable(Solutions solution) {
        tableModel.setRowCount(0);
        List<Double> xValues = solution.xValues();
        List<Double> exactValues = solution.exactValues();
        List<Double> euler = solution.improvedEuler().getValues();
        List<Double> rk4 = solution.rungeKutta().getValues();
        List<Double> milne = solution.milne().getValues();

        for (int i = 0; i < xValues.size(); i++) {
            tableModel.addRow(new Object[]{
                    i,
                    FORMAT.format(xValues.get(i)),
                    FORMAT.format(exactValues.get(i)),
                    FORMAT.format(euler.get(i)),
                    FORMAT.format(rk4.get(i)),
                    FORMAT.format(milne.get(i))
            });
        }
    }

    private void updateAnalysis(Solutions solution, double epsilon) {
        analysisArea.setText(buildAnalysisText(solution.improvedEuler(), solution.rungeKutta(), solution.milne(), epsilon));
        analysisArea.setCaretPosition(0);
    }

    private String buildAnalysisText(MethodResult euler, MethodResult rk4, MethodResult milne, double epsilon) {
        return euler.getMethodName() + ": " + euler.getErrorDescription() + " = " + FORMAT.format(euler.getMethodError())
                + verdict(euler.getMethodError(), epsilon) + "\n"
                + rk4.getMethodName() + ": " + rk4.getErrorDescription() + " = " + FORMAT.format(rk4.getMethodError())
                + verdict(rk4.getMethodError(), epsilon) + "\n"
                + milne.getMethodName() + ": " + milne.getErrorDescription() + " = " + FORMAT.format(milne.getMethodError())
                + verdict(milne.getMethodError(), epsilon) + "\n";
    }

    private String verdict(double error, double epsilon) {
        return error <= epsilon ? " (удовлетворяет epsilon)" : " (не удовлетворяет epsilon)";
    }
}
