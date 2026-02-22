import javax.swing.*;
import java.awt.*;

public class ResponsiveDrawing {

    static class CenterCirclePanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth();
            int h = getHeight();

            int size = 100;
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            g.fillOval(x, y, size, size);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Responsive Drawing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CenterCirclePanel panel = new CenterCirclePanel();
            panel.setPreferredSize(new Dimension(400, 300));

            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
