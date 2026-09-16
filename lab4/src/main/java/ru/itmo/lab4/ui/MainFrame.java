package ru.itmo.lab4.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import ru.itmo.lab4.model.AnalysisReport;
import ru.itmo.lab4.model.Point;
import ru.itmo.lab4.service.ApproximationService;
import ru.itmo.lab4.service.ChartService;
import ru.itmo.lab4.service.InputService;

public class MainFrame extends JFrame {
    private static final Object[][] DEFAULT_DATA = {
            {"1", "0.0", "0.0"},
            {"2", "0.2", "1.4988009592"},
            {"3", "0.4", "2.9620853081"},
            {"4", "0.6", "4.2261463256"},
            {"5", "0.8", "4.9800796813"},
            {"6", "1.0", "5.0"},
            {"7", "1.2", "4.4186959930"},
            {"8", "1.4", "3.5949064887"},
            {"9", "1.6", "2.8058361392"},
            {"10", "1.8", "2.1604148714"},
            {"11", "2.0", "1.6666666667"}
    };

    private final InputService inputService = new InputService();
    private final ApproximationService approximationService = new ApproximationService();
    private final ChartService chartService = new ChartService();

    private final JButton analyzeButton = new JButton("Рассчитать");
    private final JTextArea outputArea = new JTextArea();
    private final ChartPanel chartPanel = new ChartPanel();
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"№", "x", "y"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column != 0;
        }
    };
    private final JTable table = new JTable(tableModel);

    private String lastReportText;

    public MainFrame() {
        super("Лабораторная работа 4 - Аппроксимация функций");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1700, 980));
        setSize(new Dimension(1750, 1020));
        setLocationRelativeTo(null);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildMainSplit(), BorderLayout.CENTER);

        configureTable();
        fillDefaultData();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton loadButton = new JButton("Загрузить из файла");
        JButton addRowButton = new JButton("Добавить строку");
        JButton removeRowButton = new JButton("Удалить строку");
        JButton saveReportButton = new JButton("Сохранить отчет");

        loadButton.addActionListener(e -> loadFromFile());
        addRowButton.addActionListener(e -> addRow());
        removeRowButton.addActionListener(e -> removeRow());
        analyzeButton.addActionListener(e -> runApproximation());
        saveReportButton.addActionListener(e -> saveReport());

        panel.add(new JLabel("Точки вводятся в таблицу:"));
        panel.add(loadButton);
        panel.add(addRowButton);
        panel.add(removeRowButton);
        panel.add(analyzeButton);
        panel.add(saveReportButton);
        return panel;
    }

    private JSplitPane buildMainSplit() {
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Исходные точки"));

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Результаты"));

        JScrollPane chartScroll = new JScrollPane(chartPanel);
        chartScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("График"));

        JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, outputScroll);
        left.setResizeWeight(0.32);

        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, chartScroll);
        main.setResizeWeight(0.42);
        return main;
    }

    private void configureTable() {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    private void fillDefaultData() {
        for (Object[] row : DEFAULT_DATA) {
            tableModel.addRow(row);
        }
    }

    private void addRow() {
        tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, "", ""});
    }

    private void removeRow() {
        int lastRow = tableModel.getRowCount() - 1;
        if (lastRow >= 0) {
            tableModel.removeRow(lastRow);
            renumberRows();
        }
    }

    private void renumberRows() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(String.valueOf(i + 1), i, 0);
        }
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Текстовые файлы", "txt", "dat", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            List<Point> points = inputService.readFromFile(chooser.getSelectedFile().toPath());
            tableModel.setRowCount(0);
            for (int i = 0; i < points.size(); i++) {
                Point p = points.get(i);
                tableModel.addRow(new Object[]{i + 1, String.valueOf(p.x()), String.valueOf(p.y())});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void runApproximation() {
        try {
            stopEditing();
            List<Point> points = readPointsFromTable();
            prepareForRun();
            makeWorker(points).execute();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void stopEditing() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private void prepareForRun() {
        setBusy(true);
        outputArea.setText("Выполняется расчёт...");
        chartPanel.setImage(null);
    }

    private SwingWorker<ResultData, Void> makeWorker(List<Point> points) {
        return new SwingWorker<>() {
            @Override
            protected ResultData doInBackground() {
                AnalysisReport report = approximationService.analyze(points);
                String reportText = approximationService.formatReport(report);
                BufferedImage chart = chartService.buildChart(report, 900, 520);
                return new ResultData(reportText, chart);
            }

            @Override
            protected void done() {
                getResult(this);
            }
        };
    }

    private void getResult(SwingWorker<ResultData, Void> worker) {
        try {
            ResultData result = worker.get();
            lastReportText = result.reportText();
            outputArea.setText(result.reportText());
            chartPanel.setImage(result.chart());
        } catch (InterruptedException | ExecutionException ex) {
            outputArea.setText("");
            showError(ex);
        } finally {
            setBusy(false);
        }
    }

    private List<Point> readPointsFromTable() {
        List<Point> points = new ArrayList<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String x = valueAt(row, 1);
            String y = valueAt(row, 2);
            if (x.isBlank() && y.isBlank()) {
                continue;
            }
            if (x.isBlank() || y.isBlank()) {
                throw new IllegalArgumentException("В строке " + (row + 1) + " не хватает значения x или y.");
            }
            points.add(new Point(parseNumber(x), parseNumber(y)));
        }
        return points;
    }

    private String valueAt(int row, int column) {
        Object value = tableModel.getValueAt(row, column);
        return value == null ? "" : value.toString().trim();
    }

    private double parseNumber(String raw) {
        return Double.parseDouble(raw.replace(',', '.'));
    }

    private void saveReport() {
        if (lastReportText == null || lastReportText.isBlank()) {
            JOptionPane.showMessageDialog(this, "Сначала выполните расчёт.", "Нет данных", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("approximation_report.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Path path = chooser.getSelectedFile().toPath();
            validateSavePath(path);
            Files.writeString(path, lastReportText, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void validateSavePath(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Путь для сохранения отчёта не указан.");
        }
        if (Files.exists(path) && Files.isDirectory(path)) {
            throw new IllegalArgumentException("Нельзя сохранить отчёт: указан путь к папке, а не к файлу.");
        }
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            throw new IllegalArgumentException("Папка для сохранения отчёта не существует.");
        }
        if (parent != null && !Files.isWritable(parent)) {
            throw new AccessDeniedException(parent.toString());
        }
        if (Files.exists(path) && !Files.isWritable(path)) {
            throw new AccessDeniedException(path.toString());
        }
    }

    private void setBusy(boolean busy) {
        analyzeButton.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, localizeError(ex), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private String localizeError(Exception ex) {
        if (ex instanceof ExecutionException executionException && executionException.getCause() instanceof Exception cause) {
            return localizeError(cause);
        }
        if (ex instanceof NumberFormatException) {
            return "Одно из введённых значений не является числом. Проверьте поля x и y.";
        }
        if (ex instanceof NoSuchFileException) {
            return "Файл не найден.";
        }
        if (ex instanceof AccessDeniedException) {
            return "Нет прав на чтение или запись файла.";
        }
        if (ex instanceof SecurityException) {
            return "Операция запрещена настройками безопасности системы.";
        }
        if (ex instanceof IOException) {
            return "Не удалось прочитать или сохранить файл.";
        }
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "Произошла неизвестная ошибка." : ex.getMessage();
    }

    private record ResultData(String reportText, BufferedImage chart) {
    }
}
