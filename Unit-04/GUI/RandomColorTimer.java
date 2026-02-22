import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class RandomColorTimer {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("Random Color Timer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        frame.setContentPane(panel);

        Random random = new Random();

        // Timer fires every 1000 milliseconds (1 second)
        Timer timer = new Timer(1000, e -> {
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);

            panel.setBackground(new Color(r, g, b));
        });

        timer.start();

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RandomColorTimer::createAndShowUI);
    }
}
