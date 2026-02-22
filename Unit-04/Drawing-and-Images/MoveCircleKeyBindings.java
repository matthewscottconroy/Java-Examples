import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MoveCircleKeyBindings {

    static final class GamePanel extends JPanel {
        // Circle state
        private double x = 200, y = 150;
        private double vx = 0, vy = 0;
        private final int r = 20;
        private final double speed = 220; // pixels/sec

        private long lastNanos = System.nanoTime();

        GamePanel() {
            setPreferredSize(new Dimension(640, 360));
            setBackground(Color.WHITE);
            setFocusable(true);

            installKeyBindings();

            Timer timer = new Timer(16, e -> tick());
            timer.setCoalesce(true);
            timer.start();
        }

        private void installKeyBindings() {
            int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;
            InputMap im = getInputMap(cond);
            ActionMap am = getActionMap();

            bind(im, am, "pressed LEFT",  KeyStroke.getKeyStroke("pressed LEFT"),  () -> vx = -speed);
            bind(im, am, "released LEFT", KeyStroke.getKeyStroke("released LEFT"), () -> { if (vx < 0) vx = 0; });

            bind(im, am, "pressed RIGHT",  KeyStroke.getKeyStroke("pressed RIGHT"),  () -> vx = speed);
            bind(im, am, "released RIGHT", KeyStroke.getKeyStroke("released RIGHT"), () -> { if (vx > 0) vx = 0; });

            bind(im, am, "pressed UP",  KeyStroke.getKeyStroke("pressed UP"),  () -> vy = -speed);
            bind(im, am, "released UP", KeyStroke.getKeyStroke("released UP"), () -> { if (vy < 0) vy = 0; });

            bind(im, am, "pressed DOWN",  KeyStroke.getKeyStroke("pressed DOWN"),  () -> vy = speed);
            bind(im, am, "released DOWN", KeyStroke.getKeyStroke("released DOWN"), () -> { if (vy > 0) vy = 0; });

            // WASD too (optional but nice)
            bind(im, am, "pressed A",  KeyStroke.getKeyStroke("pressed A"),  () -> vx = -speed);
            bind(im, am, "released A", KeyStroke.getKeyStroke("released A"), () -> { if (vx < 0) vx = 0; });

            bind(im, am, "pressed D",  KeyStroke.getKeyStroke("pressed D"),  () -> vx = speed);
            bind(im, am, "released D", KeyStroke.getKeyStroke("released D"), () -> { if (vx > 0) vx = 0; });

            bind(im, am, "pressed W",  KeyStroke.getKeyStroke("pressed W"),  () -> vy = -speed);
            bind(im, am, "released W", KeyStroke.getKeyStroke("released W"), () -> { if (vy < 0) vy = 0; });

            bind(im, am, "pressed S",  KeyStroke.getKeyStroke("pressed S"),  () -> vy = speed);
            bind(im, am, "released S", KeyStroke.getKeyStroke("released S"), () -> { if (vy > 0) vy = 0; });
        }

        private static void bind(InputMap im, ActionMap am, String name, KeyStroke ks, Runnable r) {
            im.put(ks, name);
            am.put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { r.run(); }
            });
        }

        private void tick() {
            long now = System.nanoTime();
            double dt = (now - lastNanos) / 1_000_000_000.0;
            lastNanos = now;
            dt = Math.min(dt, 0.05);

            x += vx * dt;
            y += vy * dt;

            // Clamp inside bounds
            int w = getWidth();
            int h = getHeight();
            x = Math.max(r, Math.min(w - r, x));
            y = Math.max(r, Math.min(h - r, y));

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int cx = (int) Math.round(x);
            int cy = (int) Math.round(y);
            g.setColor(Color.BLACK);
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Move Circle (Key Bindings)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GamePanel panel = new GamePanel();
            f.setContentPane(panel);

            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);

            // Not strictly necessary with WHEN_IN_FOCUSED_WINDOW, but fine:
            panel.requestFocusInWindow();
        });
    }
}
