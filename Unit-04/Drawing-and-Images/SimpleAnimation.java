import javax.swing.*;
import java.awt.*;

public class SimpleAnimation {

    static class AnimationPanel extends JPanel {

        private int x = 0;

        public AnimationPanel() {
            Timer timer = new Timer(16, e -> {
                x += 2;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.fillOval(x, 120, 40, 40);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Animation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            AnimationPanel panel = new AnimationPanel();
            panel.setPreferredSize(new Dimension(500, 300));

            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
