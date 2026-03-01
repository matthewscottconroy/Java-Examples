import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CounterAppWithComposition {
    private int count = 0;
    private final JFrame frame;
    private final JLabel label;
    private final JButton button;

    public CounterAppWithComposition() {
        frame = new JFrame("Counter (Composition Example)");
        label = new JLabel("Count: 0");
        button = new JButton("Increment");

        // Layout
        frame.setLayout(new FlowLayout());
        frame.add(label);
        frame.add(button);

        // Behavior
        button.addActionListener(e -> {
            count++;
            label.setText("Count: " + count);
        });

        // Frame configuration
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 100);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CounterAppWithComposition::new);
    }
}

