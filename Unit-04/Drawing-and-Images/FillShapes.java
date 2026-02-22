import javax.swing.*;
import java.awt.*;

public class FillShapes {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Fill Shapes");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    g.setColor(Color.RED);
                    g.fillRect(50, 50, 100, 80);

                    g.setColor(Color.BLUE);
                    g.fillOval(200, 50, 100, 80);
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
