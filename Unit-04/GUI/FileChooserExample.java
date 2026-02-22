import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileChooserExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("File Chooser Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel label = new JLabel("No file selected", JLabel.CENTER);
        JButton button = new JButton("Open File");

        button.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                label.setText("Selected: " + selectedFile.getAbsolutePath());
            } else {
                label.setText("Selection cancelled");
            }
        });

        panel.add(label, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileChooserExample::createAndShowUI);
    }
}
