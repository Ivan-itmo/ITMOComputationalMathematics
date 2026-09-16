package gui;

import logic.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.Random;
import java.util.function.Consumer;

public class StartWindow extends JFrame {
    private int nMax = 20;
    private JTextField nField;
    private JButton inputNBtn;
    private JPanel handInput;
    private JTextField[][] aFields;
    private JTextField[] bFields;
    private int nCur = -1;
    private BigDecimal[][] curA;
    private BigDecimal[] curB;

    public StartWindow() {
        setTitle("Решение СЛАУ — Метод Гаусса");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 750);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel nPanel = new JPanel();
        nPanel.add(new JLabel("Размерность 1–20:"));
        nField = new JTextField(5);
        nPanel.add(nField);
        inputNBtn = new JButton("Применить");
        inputNBtn.addActionListener(e -> getInputN());
        nPanel.add(inputNBtn);
        add(nPanel, BorderLayout.NORTH);
        handInput = new JPanel(new GridBagLayout());
        add(new JScrollPane(handInput), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton solveHandBtn = new JButton("Решить (вручную)");
        JButton solveFileBtn = new JButton("Решить (из файла)");
        JButton solveGenBtn = new JButton("Решить (сгенерировать)");
        solveHandBtn.addActionListener(this::inputHand);
        solveFileBtn.addActionListener(this::inputFile);
        solveGenBtn.addActionListener(this::inputGenerate);
        buttonPanel.add(solveHandBtn);
        buttonPanel.add(solveFileBtn);
        buttonPanel.add(solveGenBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createHandInput(int n) {
        handInput.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        for (int j = 0; j < n; j++) {
            JLabel label = new JLabel("x" + (j + 1), JLabel.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            gbc.gridx = j;
            gbc.gridy = 0;
            handInput.add(label, gbc);
        }
        JLabel bLabel = new JLabel("= b", JLabel.CENTER);
        bLabel.setFont(bLabel.getFont().deriveFont(Font.BOLD));
        gbc.gridx = n;
        handInput.add(bLabel, gbc);
        aFields = new JTextField[n][n];
        bFields = new JTextField[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                aFields[i][j] = new JTextField(8);
                aFields[i][j].setHorizontalAlignment(JTextField.RIGHT);
                gbc.gridx = j;
                gbc.gridy = i + 1;
                handInput.add(aFields[i][j], gbc);
            }
            bFields[i] = new JTextField(8);
            bFields[i].setHorizontalAlignment(JTextField.RIGHT);
            gbc.gridx = n;
            handInput.add(bFields[i], gbc);
        }
        handInput.revalidate();
        handInput.repaint();
    }

    private void getInputN() {
        try {
            int n = Integer.parseInt(nField.getText().trim());
            if (n < 1 || n > nMax) {
                throw new IllegalArgumentException("n должно быть от 1 до 20");
            }
            nCur = n;
            createHandInput(n);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ошибка: введите целое число");
        } catch (IllegalArgumentException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: неправильное n");
        }
    }

    private void inputHand(ActionEvent e) {
        try {
            if (nCur <= 0) {
                throw new IllegalArgumentException("Ввести n надо");
            }
            curA = new BigDecimal[nCur][nCur];
            curB = new BigDecimal[nCur];
            for (int i = 0; i < nCur; i++) {
                for (int j = 0; j < nCur; j++) {
                    String s = aFields[i][j].getText().trim();
                    if (s.isEmpty()) throw new IllegalArgumentException("Поле A пусто");
                    curA[i][j] = new BigDecimal(s.replace(',', '.'));
                }
                String s = bFields[i].getText().trim();
                if (s.isEmpty()) throw new IllegalArgumentException("Поле b пусто");
                curB[i] = new BigDecimal(s.replace(',', '.'));
            }
            send();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void inputFile(ActionEvent e) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Выберите файл с системой");
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File file = chooser.getSelectedFile();
            FileData data = MatrixIO.readFromFile(file);
            nCur = data.n;
            curA = data.A;
            curB = data.b;
            send();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void inputGenerate(ActionEvent e) {
        try {
            if (nCur <= 0) {
                throw new IllegalArgumentException("Сначала примените размерность");
            }
            generateSystem();
            for (int i = 0; i < nCur; i++) {
                for (int j = 0; j < nCur; j++) {
                    aFields[i][j].setText(curA[i][j].toString());
                }
                bFields[i].setText(curB[i].toString());
            }
            send();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void send() {
        try {
            IterationVisualizer visualizer;
            Consumer<BigDecimal[]> iterationCallback = null;
            if (nCur == 2) {
                visualizer = new IterationVisualizer(curA, curB);
                iterationCallback = x -> {SwingUtilities.invokeLater(() -> visualizer.addPoint(x[0], x[1]));
                };
            }
            GaussResult myResult = SimpleIterationSolver.solve(nCur, curA, curB, iterationCallback);
            double[] libX = LibrarySolver.solve(curA, curB);
            double libDet = LibrarySolver.determinant(curA);
            new ResultWindow(nCur, myResult, libX, libDet).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void generateSystem() {
        Random rand = new Random();
        curA = new BigDecimal[nCur][nCur];
        for (int i = 0; i < nCur; i++) {
            BigDecimal rowSum = BigDecimal.ZERO;
            for (int j = 0; j < nCur; j++) {
                if (i != j) {
                    double d = rand.nextDouble() * 2 - 1;
                    BigDecimal val = BigDecimal.valueOf(d);
                    curA[i][j] = val;
                    rowSum = rowSum.add(val.abs());
                }
            }
            curA[i][i] = rowSum.add(BigDecimal.ONE).add(BigDecimal.valueOf(rand.nextDouble()));
        }
        BigDecimal[] xTrue = new BigDecimal[nCur];
        for (int i = 0; i < nCur; i++) {
            xTrue[i] = new BigDecimal(i + 1);
        }
        curB = new BigDecimal[nCur];
        for (int i = 0; i < nCur; i++) {
            curB[i] = BigDecimal.ZERO;
            for (int j = 0; j < nCur; j++) {
                curB[i] = curB[i].add(curA[i][j].multiply(xTrue[j]));
            }
        }
    }
}