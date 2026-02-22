import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class FixedTimestepInterpolated {

    static final class GameCanvas extends Canvas implements Runnable {
        private volatile boolean running = true;

        // Simulation states: previous and current.
        private double prevX = 50, currX = 50;
        private double vx = 140; // px/sec

        GameCanvas() {
            setPreferredSize(new Dimension(640, 360));
            setIgnoreRepaint(true);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            createBufferStrategy(2);
            new Thread(this, "game-loop").start();
        }

        @Override
        public void run() {
            final double dt = 1.0 / 60.0;          // fixed update step (seconds)
            double accumulator = 0.0;

            long previousTime = System.nanoTime();

            while (running) {
                long now = System.nanoTime();
                double frameTime = (now - previousTime) / 1_000_000_000.0;
                previousTime = now;

                // Avoid "spiral of death" if the app hiccups.
                frameTime = Math.min(frameTime, 0.25);

                accumulator += frameTime;

                // Fixed-timestep updates (can run multiple times per frame)
                while (accumulator >= dt) {
                    prevX = currX;
                    step(dt);
                    accumulator -= dt;
                }

                // alpha in [0,1): how far we are between prev and curr
                double alpha = accumulator / dt;
                render(alpha);

                // small sleep to reduce CPU burn (not required)
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
            }
        }

        private void step(double dt) {
            currX += vx * dt;

            int r = 20;
            int w = getWidth();

            // Bounce logic on "current" state
            if (currX < r) {
                currX = r;
                vx = Math.abs(vx);
            } else if (currX > w - r) {
                currX = w - r;
                vx = -Math.abs(vx);
            }
        }

        private void render(double alpha) {
            BufferStrategy bs = getBufferStrategy();
            if (bs == null) return;

            // Interpolate for smooth rendering:
            // renderX = prevX * (1-alpha) + currX * alpha
            double renderX = prevX + (currX - prevX) * alpha;

            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    try {
                        int w = getWidth();
                        int h = getHeight();

                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, w, h);

                        int r = 20;
                        int cx = (int) Math.round(renderX);
                        int cy = h / 2;

                        g.setColor(Color.BLACK);
                        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);

                        // Optional: tiny HUD so students can see what's happening
                        g.drawString("Fixed update: 60 Hz | Render: variable | alpha=" +
                                String.format("%.2f", alpha), 10, 20);
                    } finally {
                        g.dispose();
                    }
                } while (bs.contentsRestored());

                bs.show();
                Toolkit.getDefaultToolkit().sync();
            } while (bs.contentsLost());
        }

        public void stop() {
            running = false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Fixed Timestep + Interpolation");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GameCanvas canvas = new GameCanvas();
            f.add(canvas);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);

            canvas.requestFocusInWindow();
        });
    }
}
