import javax.swing.*;
import java.awt.*;

public class DrawText {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Draw Text");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    g.setFont(new Font("SansSerif", Font.BOLD, 24));
                    g.drawString("Hello, Graphics!", 80, 150);
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
