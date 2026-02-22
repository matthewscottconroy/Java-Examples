import javax.swing.*;
import java.awt.*;

public class StatefulDrawing {

    static class DrawingPanel extends JPanel {

        private int x = 50;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.fillOval(x, 100, 50, 50);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Stateful Drawing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            DrawingPanel panel = new DrawingPanel();
            panel.setPreferredSize(new Dimension(400, 300));

            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
