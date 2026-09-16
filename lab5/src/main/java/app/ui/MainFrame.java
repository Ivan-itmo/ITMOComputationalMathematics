package app.ui;

import app.model.DataSet;
import app.model.FunctionItem;
import app.model.InterpolationReport;
import app.model.MethodResult;
import app.service.DataSetInput;
import app.service.FileInput;
import app.service.FunctionList;
import app.service.InterpolationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

public class MainFrame extends JFrame {
    private final JTextArea manualInputArea = new JTextArea();
    private final JTextArea filePreviewArea = new JTextArea();
    private final JLabel filePathLabel = new JLabel("Файл не выбран");
    private final JTabbedPane sourceTabs = new JTabbedPane();
    private final JComboBox<FunctionItem> functionBox = new JComboBox<>(FunctionList.functions().toArray(FunctionItem[]::new));
    private final JTextField leftBorderField = new JTextField("-3.14", 8);
    private final JTextField rightBorderField = new JTextField("3.14", 8);
    private final JTextField countField = new JTextField("7", 6);
    private final JTextField argumentField = new JTextField("1", 10);
    private final JTable methodTable = new JTable();
    private final JTable differenceTable = new JTable();
    private final JTextArea analysisArea = new JTextArea();
    private final PlotPanel plotPanel = new PlotPanel();
    private final JTabbedPane plotTabs = new JTabbedPane();
    private final InterpolationService interpolationService = new InterpolationService();

    private Path selectedFile;

    public MainFrame() {
        super("Лабораторная 5: Интерполяция");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 980));

        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        manualInputArea.setLineWrap(true);
        filePreviewArea.setEditable(false);
        filePreviewArea.setLineWrap(true);
        filePreviewArea.setWrapStyleWord(true);

        methodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        differenceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setContentPane(buildContent());
        setSize(1280, 980);
        setLocationRelativeTo(null);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildTopPanel(), BorderLayout.NORTH);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setResizeWeight(0.42);
        verticalSplit.setTopComponent(buildCenterPanel());
        verticalSplit.setBottomComponent(buildPlotTabs());

        root.add(verticalSplit, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Расчет"));
        panel.add(new JLabel("x для интерполяции:"));
        panel.add(argumentField);

        JButton calculateButton = new JButton("Построить и вычислить");
        calculateButton.addActionListener(event -> calculate());
        panel.add(calculateButton);

        JLabel hint = new JLabel("Равномерная сетка нужна для Ньютона и Гаусса; Стирлинг требует нечетное число узлов, Бессель — четное.");
        hint.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(hint);
        return panel;
    }

    private JSplitPane buildCenterPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.33);
        splitPane.setLeftComponent(buildSourceTabs());
        splitPane.setRightComponent(buildResultPanel());
        return splitPane;
    }

    private JTabbedPane buildSourceTabs() {
        sourceTabs.addTab("Ввод с клавиатуры", buildManualPanel());
        sourceTabs.addTab("Из файла", buildFilePanel());
        sourceTabs.addTab("По функции", buildFunctionPanel());
        return sourceTabs;
    }

    private JPanel buildManualPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Точки в формате x y"));
        panel.add(new JScrollPane(manualInputArea), BorderLayout.CENTER);

        JTextArea info = new JTextArea("""
                Введите точки построчно в формате x y.
                Пустые строки и строки с # игнорируются.
                """);
        info.setEditable(false);
        info.setOpaque(false);
        panel.add(info, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFilePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton openButton = new JButton("Выбрать файл");
        openButton.addActionListener(event -> chooseFile());
        top.add(openButton);
        top.add(filePathLabel);
        panel.add(top, BorderLayout.NORTH);

        filePreviewArea.setText("""
                Формат файла:
                x y
                x y
                ...
                """);
        panel.add(new JScrollPane(filePreviewArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFunctionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Генерация узлов по функции"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Функция:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(functionBox, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Левая граница:"), gbc);

        gbc.gridx = 1;
        panel.add(leftBorderField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Правая граница:"), gbc);

        gbc.gridx = 1;
        panel.add(rightBorderField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Количество точек:"), gbc);

        gbc.gridx = 1;
        panel.add(countField, gbc);

        return panel;
    }

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setResizeWeight(0.45);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createTitledBorder("Сравнение методов"));
        top.add(new JScrollPane(methodTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(BorderFactory.createTitledBorder("Таблица разностей и анализ"));
        JSplitPane innerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        innerSplit.setResizeWeight(0.6);
        innerSplit.setTopComponent(new JScrollPane(differenceTable));
        innerSplit.setBottomComponent(new JScrollPane(analysisArea));
        bottom.add(innerSplit, BorderLayout.CENTER);

        verticalSplit.setTopComponent(top);
        verticalSplit.setBottomComponent(bottom);
        panel.add(verticalSplit, BorderLayout.CENTER);
        return panel;
    }

    private JTabbedPane buildPlotTabs() {
        plotTabs.addTab("Все методы", plotPanel);
        return plotTabs;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(Path.of(".").toAbsolutePath().normalize().toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Текстовые данные", "txt", "dat"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile().toPath();
            filePathLabel.setText(selectedFile.toString());
            try {
                filePreviewArea.setText(FileInput.readText(selectedFile));
            } catch (IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        }
    }

    private void calculate() {
        try {
            double argument = parseDouble(argumentField.getText(), "x для интерполяции");
            DataSet dataSet = buildSelectedDataSet();
            InterpolationReport report = interpolationService.analyze(dataSet, argument);
            fillMethodTable(report.results());
            fillDifferenceTable(report);
            analysisArea.setText(report.analysis());
            updatePlotTabs(report);
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void updatePlotTabs(InterpolationReport report) {
        plotTabs.removeAll();
        plotPanel.setReport(report);
        plotTabs.addTab("Все методы", plotPanel);

        DoubleUnaryOperator sourceFunction = report.plotFunctions().get("Исходная функция");
        for (Map.Entry<String, DoubleUnaryOperator> entry : report.plotFunctions().entrySet()) {
            if (entry.getKey().startsWith("Исходная")) {
                continue;
            }

            PlotPanel methodPanel = new PlotPanel();
            Map<String, DoubleUnaryOperator> functions = new LinkedHashMap<>();
            functions.put(entry.getKey(), entry.getValue());
            if (sourceFunction != null) {
                functions.put("Исходная функция", sourceFunction);
            }
            methodPanel.setPlot(report.dataSet(), functions);
            plotTabs.addTab(entry.getKey(), methodPanel);
        }
    }

    private DataSet buildSelectedDataSet() {
        int tabIndex = sourceTabs.getSelectedIndex();
        if (tabIndex == 0) {
            return DataSetInput.fromText(manualInputArea.getText(), "Ручной ввод");
        }
        if (tabIndex == 1) {
            if (selectedFile == null) {
                throw new IllegalArgumentException("Сначала выберите файл с данными.");
            }
            return FileInput.readDataSet(selectedFile);
        }

        FunctionItem function = (FunctionItem) functionBox.getSelectedItem();
        double left = parseDouble(leftBorderField.getText(), "левая граница");
        double right = parseDouble(rightBorderField.getText(), "правая граница");
        int count = parseInt(countField.getText(), "количество точек");
        return DataSetInput.fromFunction(function, left, right, count);
    }

    private void fillMethodTable(List<MethodResult> results) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Метод", "Значение"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (MethodResult result : results) {
            model.addRow(new Object[]{result.methodName(), result.value()});
        }
        methodTable.setModel(model);
    }

    private void fillDifferenceTable(InterpolationReport report) {
        int n = report.dataSet().points().size();
        DefaultTableModel model = new DefaultTableModel(report.differenceHeaders(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (int row = 0; row < n; row++) {
            Object[] values = new Object[n + 1];
            values[0] = InterpolationService.formatDouble(report.dataSet().points().get(row).x());
            for (int column = 0; column < n; column++) {
                if (row < n - column) {
                    values[column + 1] = InterpolationService.formatDouble(report.differenceTable()[row][column]);
                } else {
                    values[column + 1] = "";
                }
            }
            model.addRow(values);
        }
        differenceTable.setModel(model);
    }

    private double parseDouble(String text, String fieldName) {
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Некорректно задано поле \"" + fieldName + "\".");
        }
    }

    private int parseInt(String text, String fieldName) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Некорректно задано поле \"" + fieldName + "\".");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

}
