import javax.swing.*;
import java.awt.*;

public class RadioButtonExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("Radio Button Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Select your favorite language:", JLabel.CENTER);

        // Radio buttons
        JRadioButton javaButton = new JRadioButton("Java");
        JRadioButton pythonButton = new JRadioButton("Python");
        JRadioButton rustButton = new JRadioButton("Rust");

        // Group them so only one can be selected
        ButtonGroup group = new ButtonGroup();
        group.add(javaButton);
        group.add(pythonButton);
        group.add(rustButton);

        // Set default selection
        javaButton.setSelected(true);

        // Panel to hold radio buttons
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(3, 1));
        radioPanel.add(javaButton);
        radioPanel.add(pythonButton);
        radioPanel.add(rustButton);

        // Event handling
        javaButton.addActionListener(e -> label.setText("You selected Java"));
        pythonButton.addActionListener(e -> label.setText("You selected Python"));
        rustButton.addActionListener(e -> label.setText("You selected Rust"));

        panel.add(label, BorderLayout.NORTH);
        panel.add(radioPanel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RadioButtonExample::createAndShowUI);
    }
}
