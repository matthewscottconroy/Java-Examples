import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class GUIWithPanel {

    private static void createAndShowUI() {

        // Create the frame (window)
        JFrame frame = new JFrame("GUI with Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        // Create a panel (container for components)
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create components
        JLabel label = new JLabel("Hello, World!", JLabel.CENTER);
        JButton button = new JButton("Click Me");

        // Add a simple interaction
        button.addActionListener(e -> label.setText("Button Clicked!"));

        // Add components to the panel
        panel.add(label, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);

        // Add panel to frame
        frame.setContentPane(panel);

        // Show window
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIWithPanel::createAndShowUI);
    }
}
