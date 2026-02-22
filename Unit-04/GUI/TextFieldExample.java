import javax.swing.*;
import java.awt.*;

public class TextFieldExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("TextField Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel label = new JLabel("Enter your name:", JLabel.CENTER);

        JTextField textField = new JTextField();
        JButton button = new JButton("Submit");

        // When Enter is pressed inside the text field
        textField.addActionListener(e -> {
            String input = textField.getText();
            label.setText("Hello, " + input + "!");
        });

        // When button is clicked
        button.addActionListener(e -> {
            String input = textField.getText();
            label.setText("Hello, " + input + "!");
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextFieldExample::createAndShowUI);
    }
}
