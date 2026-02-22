import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageViewer {

    private final BufferedImage image;

    public ImageViewer(String path) {
        try {
            image = ImageIO.read(new File(path));
            if (image == null) throw new IllegalArgumentException("ImageIO.read returned null");
            System.out.println("Loaded " + image.getWidth() + "x" + image.getHeight());
        } catch (IOException e) {
            throw new RuntimeException("Could not load image: " + e.getMessage(), e);
        }
    }

    public void show() {
        JFrame frame = new JFrame("Pixel Art Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;

                // Make the drawing surface obvious
                g2.setColor(new Color(240, 240, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.RED);
                g2.drawLine(0, 0, getWidth(), getHeight());
                g2.drawLine(getWidth(), 0, 0, getHeight());

                // Scale image to fit panel
                int pw = getWidth();
                int ph = getHeight();

                double sx = (double) pw / image.getWidth();
                double sy = (double) ph / image.getHeight();
                double s = Math.min(sx, sy);

                int drawW = (int) Math.round(image.getWidth() * s);
                int drawH = (int) Math.round(image.getHeight() * s);

                int x = (pw - drawW) / 2;
                int y = (ph - drawH) / 2;

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                g2.drawImage(image, x, y, drawW, drawH, null);

                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, drawW - 1, drawH - 1);
                g2.drawString("panel=" + pw + "x" + ph + "  image=" + image.getWidth() + "x" + image.getHeight(), 10, 20);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(600, 600);
            }
        };

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageViewer("space-invader.png").show());
    }
}
