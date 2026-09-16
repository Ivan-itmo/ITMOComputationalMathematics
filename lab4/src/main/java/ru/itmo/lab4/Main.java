package ru.itmo.lab4;

import javax.swing.SwingUtilities;

import ru.itmo.lab4.ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
