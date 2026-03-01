import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CounterAppWithInheritance extends JFrame {
    private int count = 0;
    private final JLabel label;
    private final JButton button;

    public CounterAppWithInheritance() {
        super("Counter (Inheritance Example)");
        label = new JLabel("Count: 0");
        button = new JButton("Increment");

        setLayout(new FlowLayout());
        add(label);
        add(button);

        button.addActionListener(e -> {
            count++;
            label.setText("Count: " + count);
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(250, 100);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CounterAppWithInheritance::new);
    }
}

