import javax.swing.*;
import java.awt.*;

public class NestedLayoutExample {

    private static void createAndShowUI() {
        JFrame frame = new JFrame("Nested Layout Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Outer layout: BorderLayout
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Nested Layout Demo", JLabel.CENTER);
        root.add(title, BorderLayout.NORTH);

        // Inner layout #1: GridLayout for a simple "form"
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField nameField = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "TA", "Professor"});
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Role:"));
        form.add(roleBox);

        // Inner layout #2: FlowLayout for buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton ok = new JButton("OK");
        JButton clear = new JButton("Clear");
        buttons.add(ok);
        buttons.add(clear);

        // Middle panel nests the form (CENTER) and buttons (SOUTH)
        JPanel middle = new JPanel(new BorderLayout(10, 10));
        middle.add(form, BorderLayout.CENTER);
        middle.add(buttons, BorderLayout.SOUTH);

        root.add(middle, BorderLayout.CENTER);

        // Simple behavior
        ok.addActionListener(e -> {
            String name = nameField.getText().trim();
            String role = (String) roleBox.getSelectedItem();
            JOptionPane.showMessageDialog(frame, "Hello " + name + " (" + role + ")");
        });

        clear.addActionListener(e -> {
            nameField.setText("");
            roleBox.setSelectedIndex(0);
            nameField.requestFocusInWindow();
        });

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NestedLayoutExample::createAndShowUI);
    }
}
