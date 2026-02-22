import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class HelloWithButton {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hello World GUI");

            JLabel label = new JLabel("Click the button", JLabel.CENTER);
            JButton button = new JButton("Say Hello");

            button.addActionListener(e -> label.setText("Hello, World!"));

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(label, BorderLayout.CENTER);
            panel.add(button, BorderLayout.SOUTH);

            frame.add(panel);
            frame.setSize(300, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
