import javax.swing.*;
import java.awt.*;

public class LayoutGrid {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GridLayout Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 200);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));

            JLabel label = new JLabel("Type something, then click OK", JLabel.CENTER);
            JTextField field = new JTextField();

            JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
            JButton ok = new JButton("OK");
            JButton clear = new JButton("Clear");
            buttonRow.add(ok);
            buttonRow.add(clear);

            ok.addActionListener(e -> label.setText("You typed: " + field.getText()));
            clear.addActionListener(e -> { field.setText(""); label.setText("Type something, then click OK"); });

            panel.add(label);
            panel.add(field);
            panel.add(buttonRow);

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
