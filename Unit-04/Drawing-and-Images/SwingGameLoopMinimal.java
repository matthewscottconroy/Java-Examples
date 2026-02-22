import javax.swing.*;
import java.awt.*;

public class SwingGameLoopMinimal {

    static final class GamePanel extends JPanel {
        // "World" state in doubles so motion is smooth even if pixels are ints.
        private double x = 50;
        private double vx = 140; // pixels/sec

        private long lastNanos = System.nanoTime();

        GamePanel() {
            setPreferredSize(new Dimension(640, 360));
            setBackground(Color.WHITE);

            // 60-ish updates per second; not guaranteed.
            Timer timer = new Timer(16, e -> tick());
            timer.setCoalesce(true); // coalesce queued events if EDT is behind
            timer.start();
        }

        private void tick() {
            long now = System.nanoTime();
            double dt = (now - lastNanos) / 1_000_000_000.0; // seconds
            lastNanos = now;

            // Clamp dt to avoid huge jumps if the app hiccups (GC, resize, debugger, etc.)
            dt = Math.min(dt, 0.05);

            x += vx * dt;

            // Bounce
            int r = 20;
            if (x < r) { x = r; vx = Math.abs(vx); }
            if (x > getWidth() - r) { x = getWidth() - r; vx = -Math.abs(vx); }

            repaint(); // schedule paint on EDT
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int r = 20;
            int cx = (int) Math.round(x);
            int cy = getHeight() / 2;

            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Swing Minimal Game Loop");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new GamePanel());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
