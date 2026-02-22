import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;

public class ToneGeneratorDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToneGeneratorDemo::createAndShow);
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Tone Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton button = new JButton("Play 440Hz Tone");
        button.addActionListener(e -> new Thread(() -> playTone(440, 500)).start());

        frame.add(button, BorderLayout.CENTER);
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Generates and plays a sine wave.
     *
     * @param freqHz frequency in Hertz
     * @param durationMs duration in milliseconds
     */
    private static void playTone(double freqHz, int durationMs) {
        final float sampleRate = 44100f;

        try {
            AudioFormat format = new AudioFormat(
                    sampleRate,     // Sample rate
                    16,             // Sample size in bits
                    1,              // Mono
                    true,           // Signed
                    false           // Little endian
            );

            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            int numSamples = (int) (durationMs * sampleRate / 1000);
            byte[] buffer = new byte[2]; // 16-bit = 2 bytes per sample

            for (int i = 0; i < numSamples; i++) {

                double time = i / sampleRate;
                double amplitude = Math.sin(2 * Math.PI * freqHz * time);

                short sample = (short) (amplitude * 32767);

                buffer[0] = (byte) (sample & 0xFF);
                buffer[1] = (byte) ((sample >> 8) & 0xFF);

                line.write(buffer, 0, 2);
            }

            line.drain();
            line.stop();
            line.close();

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
}
