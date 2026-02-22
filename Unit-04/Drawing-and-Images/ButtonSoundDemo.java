import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class ButtonSoundDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ButtonSoundDemo::createAndShow);
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Play Sound Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton button = new JButton("Play Sound");
        button.addActionListener(e -> playSound("/beep.wav"));

        frame.add(button, BorderLayout.CENTER);
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void playSound(String resourcePath) {
        try {
            URL url = ButtonSoundDemo.class.getResource(resourcePath);
            if (url == null) {
                System.err.println("Sound file not found: " + resourcePath);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();

        } catch (UnsupportedAudioFileException |
                 IOException |
                 LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
}
