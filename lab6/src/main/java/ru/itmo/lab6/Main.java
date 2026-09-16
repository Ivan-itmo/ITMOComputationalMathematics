package ru.itmo.lab6;

import javax.swing.SwingUtilities;
import ru.itmo.lab6.ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

// Метод пристрелки