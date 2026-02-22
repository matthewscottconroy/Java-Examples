import javax.swing.*;
import java.awt.*;

public class LayoutBorder {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BorderLayout Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 180);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JLabel label = new JLabel("Type something, then click OK", JLabel.CENTER);
            JTextField field = new JTextField();

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            JButton ok = new JButton("OK");
            JButton clear = new JButton("Clear");
            buttonPanel.add(ok);
            buttonPanel.add(clear);

            ok.addActionListener(e -> label.setText("You typed: " + field.getText()));
            clear.addActionListener(e -> { field.setText(""); label.setText("Type something, then click OK"); });

            panel.add(label, BorderLayout.NORTH);
            panel.add(field, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
