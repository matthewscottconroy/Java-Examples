import javax.swing.*;
import java.awt.*;

public class TableExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("JTable Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 350);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel label = new JLabel("Select a row:", JLabel.CENTER);

        // Column names
        String[] columns = { "ID", "Name", "Language" };

        // Row data
        Object[][] data = {
                { 1, "Alice", "Java" },
                { 2, "Bob", "Python" },
                { 3, "Carol", "Rust" },
                { 4, "Dave", "Go" }
        };

        JTable table = new JTable(data, columns);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Put table inside scroll pane (important)
        JScrollPane scrollPane = new JScrollPane(table);

        // Listen for row selection
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    Object name = table.getValueAt(selectedRow, 1);
                    label.setText("Selected: " + name);
                }
            }
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TableExample::createAndShowUI);
    }
}
