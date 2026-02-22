import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class MenuExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("Menu Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);

        JLabel label = new JLabel("Use the menu above", JLabel.CENTER);
        frame.add(label);

        // ----- Menu Bar -----
        JMenuBar menuBar = new JMenuBar();

        // ----- File Menu -----
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newItem.addActionListener(e -> label.setText("New selected"));

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // ----- Help Menu -----
        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(frame,
                        "Menu Example\nBuilt with Swing",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE)
        );

        helpMenu.add(aboutItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        // Attach menu bar to frame
        frame.setJMenuBar(menuBar);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MenuExample::createAndShowUI);
    }
}
