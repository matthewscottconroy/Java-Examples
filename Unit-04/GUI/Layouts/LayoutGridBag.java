import javax.swing.*;
import java.awt.*;

public class LayoutGridBag {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GridBagLayout Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 220);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(10, 10, 10, 10);
            c.fill = GridBagConstraints.HORIZONTAL;

            JLabel label = new JLabel("Type something, then click OK", JLabel.CENTER);
            JTextField field = new JTextField();
            JButton ok = new JButton("OK");
            JButton clear = new JButton("Clear");

            ok.addActionListener(e -> label.setText("You typed: " + field.getText()));
            clear.addActionListener(e -> { field.setText(""); label.setText("Type something, then click OK"); });

            // Row 0: label spans 2 columns
            c.gridx = 0; c.gridy = 0;
            c.gridwidth = 2;
            c.weightx = 1.0;
            panel.add(label, c);

            // Row 1: field spans 2 columns
            c.gridx = 0; c.gridy = 1;
            c.gridwidth = 2;
            c.weightx = 1.0;
            panel.add(field, c);

            // Row 2: buttons
            c.gridy = 2;
            c.gridwidth = 1;
            c.weightx = 0.5;

            c.gridx = 0;
            panel.add(ok, c);

            c.gridx = 1;
            panel.add(clear, c);

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
