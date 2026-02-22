import javax.swing.*;
import java.awt.*;

public class LayoutFlow {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FlowLayout Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 150);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

            JLabel label = new JLabel("Type something, then click OK");
            JTextField field = new JTextField(15);
            JButton ok = new JButton("OK");
            JButton clear = new JButton("Clear");

            ok.addActionListener(e -> label.setText("You typed: " + field.getText()));
            clear.addActionListener(e -> { field.setText(""); label.setText("Type something, then click OK"); });

            panel.add(label);
            panel.add(field);
            panel.add(ok);
            panel.add(clear);

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
