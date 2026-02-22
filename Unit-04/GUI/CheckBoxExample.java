import javax.swing.*;
import java.awt.*;

public class CheckBoxExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("Checkbox Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Select your interests:", JLabel.CENTER);

        // Create checkboxes
        JCheckBox musicBox = new JCheckBox("Music");
        JCheckBox mathBox = new JCheckBox("Math");
        JCheckBox programmingBox = new JCheckBox("Programming");

        // Panel to hold checkboxes
        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new GridLayout(3, 1));
        checkPanel.add(musicBox);
        checkPanel.add(mathBox);
        checkPanel.add(programmingBox);

        // Common listener for all checkboxes
        Runnable updateLabel = () -> {
            StringBuilder selected = new StringBuilder("Selected: ");

            if (musicBox.isSelected()) selected.append("Music ");
            if (mathBox.isSelected()) selected.append("Math ");
            if (programmingBox.isSelected()) selected.append("Programming ");

            if (selected.toString().equals("Selected: "))
                selected.append("None");

            label.setText(selected.toString());
        };

        musicBox.addActionListener(e -> updateLabel.run());
        mathBox.addActionListener(e -> updateLabel.run());
        programmingBox.addActionListener(e -> updateLabel.run());

        panel.add(label, BorderLayout.NORTH);
        panel.add(checkPanel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CheckBoxExample::createAndShowUI);
    }
}
