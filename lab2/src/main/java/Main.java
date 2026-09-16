import gui.SolveWindow;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SolveWindow().setVisible(true));
    }
}
