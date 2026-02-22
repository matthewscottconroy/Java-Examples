import javax.swing.*;
import java.awt.*;

public class TextAreaExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("TextArea Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel label = new JLabel("Enter multiple lines of text:", JLabel.CENTER);

        // Create text area
        JTextArea textArea = new JTextArea(8, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Must wrap JTextArea in a JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton button = new JButton("Print to Console");

        button.addActionListener(e -> {
            String text = textArea.getText();
            System.out.println("----- Text Entered -----");
            System.out.println(text);
            System.out.println("------------------------");
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextAreaExample::createAndShowUI);
    }
}
