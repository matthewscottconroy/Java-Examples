import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ManualImagePainting {

    private final BufferedImage image;
    private final int scale = 20;

    public ManualImagePainting() {

        int width = 8;
        int height = 8;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Define some colors
        int YELLOW = Color.YELLOW.getRGB();
        int BLACK = Color.BLACK.getRGB();
        int WHITE = Color.WHITE.getRGB();

        // Fill background yellow
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, YELLOW);
            }
        }

        // Eyes
        image.setRGB(2, 2, BLACK);
        image.setRGB(5, 2, BLACK);

        // Smile
        image.setRGB(2, 5, BLACK);
        image.setRGB(3, 6, BLACK);
        image.setRGB(4, 6, BLACK);
        image.setRGB(5, 5, BLACK);

        // Highlight
        image.setRGB(1, 1, WHITE);
    }

    public void show() {
        JFrame frame = new JFrame("Pixel Art Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                g2.drawImage(image, 0, 0,
                        image.getWidth() * scale,
                        image.getHeight() * scale,
                        null);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(
                        image.getWidth() * scale,
                        image.getHeight() * scale
                );
            }
        };

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManualImagePainting app = new ManualImagePainting();
            app.show();
        });
    }
}
