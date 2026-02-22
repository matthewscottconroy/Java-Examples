import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;

public class MoveCircleCanvas {

    static final class GameCanvas extends Canvas implements Runnable {
        private volatile boolean running = true;

        private double x = 200, y = 150;
        private double vx = 0, vy = 0;
        private final int r = 20;
        private final double speed = 220; // px/sec

        GameCanvas() {
            setPreferredSize(new Dimension(640, 360));
            setIgnoreRepaint(true);
            setBackground(Color.WHITE);

            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) { onKey(e, true); }
                @Override public void keyReleased(KeyEvent e) { onKey(e, false); }
            });
        }

        private void onKey(KeyEvent e, boolean down) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT  -> vx = down ? -speed : (vx < 0 ? 0 : vx);
                case KeyEvent.VK_RIGHT -> vx = down ?  speed : (vx > 0 ? 0 : vx);
                case KeyEvent.VK_UP    -> vy = down ? -speed : (vy < 0 ? 0 : vy);
                case KeyEvent.VK_DOWN  -> vy = down ?  speed : (vy > 0 ? 0 : vy);
            }
        }

        @Override public void addNotify() {
            super.addNotify();
            createBufferStrategy(2);
            requestFocusInWindow(); // IMPORTANT for KeyListener
            new Thread(this, "game-loop").start();
        }

        @Override public void run() {
            long last = System.nanoTime();
            while (running) {
                long now = System.nanoTime();
                double dt = (now - last) / 1_000_000_000.0;
                last = now;
                dt = Math.min(dt, 0.05);

                update(dt);
                render();

                try { Thread.sleep(2); } catch (InterruptedException ignored) {}
            }
        }

        private void update(double dt) {
            x += vx * dt;
            y += vy * dt;

            int w = getWidth(), h = getHeight();
            x = Math.max(r, Math.min(w - r, x));
            y = Math.max(r, Math.min(h - r, y));
        }

        private void render() {
            BufferStrategy bs = getBufferStrategy();
            if (bs == null) return;

            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    try {
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, getWidth(), getHeight());

                        g.setColor(Color.BLACK);
                        int cx = (int) Math.round(x);
                        int cy = (int) Math.round(y);
                        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                    } finally {
                        g.dispose();
                    }
                } while (bs.contentsRestored());

                bs.show();
                Toolkit.getDefaultToolkit().sync();
            } while (bs.contentsLost());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Move Circle (Canvas + KeyListener)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GameCanvas c = new GameCanvas();
            f.add(c);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);

            c.requestFocusInWindow();
        });
    }
}
