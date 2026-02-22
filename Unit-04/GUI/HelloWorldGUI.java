import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class HelloWorldGUI {

    public static void main(String[] args) {
        // Always start Swing apps on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hello World");

            JLabel label = new JLabel("Hello, World!", JLabel.CENTER);

            frame.add(label);
            frame.setSize(300, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null); // center on screen
            frame.setVisible(true);
        });
    }
}
