import javax.swing.*;
import java.awt.*;

public class LayoutBox {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BoxLayout Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 220);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel label = new JLabel("Type something, then click OK");
            label.setAlignmentX(Component.CENTER_ALIGNMENT);

            JTextField field = new JTextField();
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            JButton ok = new JButton("OK");
            JButton clear = new JButton("Clear");
            buttonPanel.add(ok);
            buttonPanel.add(clear);

            ok.addActionListener(e -> label.setText("You typed: " + field.getText()));
            clear.addActionListener(e -> { field.setText(""); label.setText("Type something, then click OK"); });

            panel.add(label);
            panel.add(Box.createVerticalStrut(10));
            panel.add(field);
            panel.add(Box.createVerticalStrut(10));
            panel.add(buttonPanel);

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
