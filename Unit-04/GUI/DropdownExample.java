import javax.swing.*;
import java.awt.*;

public class DropdownExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("Dropdown Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Select a language:", JLabel.CENTER);

        // Create dropdown
        String[] languages = { "Java", "Python", "Rust", "Go" };
        JComboBox<String> comboBox = new JComboBox<>(languages);

        // Set default selection
        comboBox.setSelectedIndex(0);

        // Event handling
        comboBox.addActionListener(e -> {
            String selected = (String) comboBox.getSelectedItem();
            label.setText("You selected: " + selected);
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(comboBox, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DropdownExample::createAndShowUI);
    }
}
