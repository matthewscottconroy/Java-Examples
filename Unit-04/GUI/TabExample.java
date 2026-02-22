import javax.swing.*;
import java.awt.*;

public class TabExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("Tab Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);
        frame.setLocationRelativeTo(null);

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // ----- Tab 1 -----
        JPanel panel1 = new JPanel();
        panel1.add(new JLabel("Welcome to the Home tab"));

        // ----- Tab 2 -----
        JPanel panel2 = new JPanel();
        panel2.add(new JButton("Click Me"));

        // ----- Tab 3 -----
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.add(new JTextArea("This is a text area inside a tab"), BorderLayout.CENTER);

        // Add tabs
        tabbedPane.addTab("Home", panel1);
        tabbedPane.addTab("Actions", panel2);
        tabbedPane.addTab("Notes", panel3);

        // Listen for tab changes
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            System.out.println("Selected tab: " + tabbedPane.getTitleAt(index));
        });

        frame.setContentPane(tabbedPane);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TabExample::createAndShowUI);
    }
}
