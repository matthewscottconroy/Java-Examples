import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;

public class MouseTracker {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("Mouse Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Move the mouse inside the window", JLabel.CENTER);

        panel.add(label, BorderLayout.SOUTH);

        // Track mouse movement
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                label.setText("Mouse at (" + e.getX() + ", " + e.getY() + ")");
            }
        });

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MouseTracker::createAndShowUI);
    }
}
