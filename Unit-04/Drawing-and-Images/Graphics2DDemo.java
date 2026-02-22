import javax.swing.*;
import java.awt.*;

public class Graphics2DDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graphics2D Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    Graphics2D g2 = (Graphics2D) g;

                    g2.setStroke(new BasicStroke(4));
                    g2.setColor(Color.BLACK);
                    g2.drawRect(100, 100, 150, 100);
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
