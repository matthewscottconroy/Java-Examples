import javax.swing.*;
import java.awt.*;

public class ColorSliderExample {

    private static void createAndShowUI() {

        JFrame frame = new JFrame("RGB Color Mixer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Preview panel (shows selected color)
        JPanel previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(100, 150));
        previewPanel.setBackground(new Color(0, 0, 0));

        // Sliders
        JSlider redSlider = createSlider();
        JSlider greenSlider = createSlider();
        JSlider blueSlider = createSlider();

        // Labels
        JLabel redLabel = new JLabel("Red");
        JLabel greenLabel = new JLabel("Green");
        JLabel blueLabel = new JLabel("Blue");

        JPanel sliderPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        sliderPanel.add(redLabel);
        sliderPanel.add(redSlider);
        sliderPanel.add(greenLabel);
        sliderPanel.add(greenSlider);
        sliderPanel.add(blueLabel);
        sliderPanel.add(blueSlider);

        // Common update logic
        Runnable updateColor = () -> {
            int r = redSlider.getValue();
            int g = greenSlider.getValue();
            int b = blueSlider.getValue();
            previewPanel.setBackground(new Color(r, g, b));
        };

        redSlider.addChangeListener(e -> updateColor.run());
        greenSlider.addChangeListener(e -> updateColor.run());
        blueSlider.addChangeListener(e -> updateColor.run());

        mainPanel.add(previewPanel, BorderLayout.CENTER);
        mainPanel.add(sliderPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private static JSlider createSlider() {
        JSlider slider = new JSlider(0, 255, 0);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        return slider;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorSliderExample::createAndShowUI);
    }
}
