import javax.swing.*;
import java.awt.*;

public class JListExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("JList Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Select an item:", JLabel.CENTER);

        // Model (recommended approach)
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("Java");
        model.addElement("Python");
        model.addElement("Rust");
        model.addElement("Go");

        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Listen for selection changes
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = list.getSelectedValue();
                label.setText("Selected: " + selected);
            }
        });

        // Put list inside scroll pane (important)
        JScrollPane scrollPane = new JScrollPane(list);

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JListExample::createAndShowUI);
    }
}
