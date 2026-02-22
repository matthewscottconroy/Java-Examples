import javax.swing.*;
import java.awt.*;

public class DrawShapes {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Draw Shapes");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    g.drawRect(50, 50, 100, 80);
                    g.drawOval(200, 50, 100, 80);
                }
            };

            panel.setPreferredSize(new Dimension(400, 300));
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
