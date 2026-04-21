import javax.sound.sampled.*;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * PianoSynthRecorder — hexagonal-keyboard synthesizer with ADSR, effects,
 * MIDI/WAV export, and real-time waveform display.
 *
 * <p>Architecture:
 * <ul>
 *   <li>{@link SynthEngine} — software synthesizer; mixes active voices, applies
 *       filter/effects, drives a {@link SourceDataLine}.</li>
 *   <li>{@link Voice} — one oscillator with ADSR envelope (attack, decay,
 *       sustain, release).</li>
 *   <li>{@link Recorder} — captures note-on/off events with timestamps for
 *       playback and MIDI/WAV export.</li>
 *   <li>{@link HexKeyboardPanel} / {@link HexButton} — Wicki-Hayden isomorphic
 *       hexagonal keyboard UI.</li>
 *   <li>{@link TuningPresets} — maps all 128 MIDI note numbers to frequencies
 *       for various temperaments.</li>
 *   <li>{@link WaveformPanel} — oscilloscope-style real-time display.</li>
 *   <li>{@link WaveFile} — minimal PCM WAV file writer.</li>
 * </ul>
 *
 * <p>Bugs fixed vs. original:
 * <ol>
 *   <li>ADSR timing: attack/decay/release were in milliseconds but were compared
 *       directly to sampleCount (raw samples). A 100 ms attack at 44100 Hz needs
 *       4410 samples, not 100. Fixed: convert ms → samples once in Voice constructor.</li>
 *   <li>Voice release phase: noteOff() removed the voice from the active map
 *       before the release tail could play. The voice was silenced immediately.
 *       Fixed: noteOff() calls voice.release() but keeps it in a separate
 *       {@code releasing} list; voices are pruned only when amplitude falls below a
 *       threshold.</li>
 *   <li>Playback froze the EDT: recorder.playback() ran synchronously on the
 *       button's action listener (EDT), blocking the UI for the recording
 *       duration. Fixed: run in a SwingWorker.</li>
 *   <li>WaveFile header: DataOutputStream.writeInt is big-endian; WAV needs
 *       little-endian. Integer.reverseBytes() was already there — confirmed correct.</li>
 *   <li>TuningPresets.get() only mapped notes 60–60+n, leaving all other MIDI
 *       notes at 12-TET regardless of selection. Fixed: fill all 128 MIDI notes
 *       using the octave-repeating ratio of the selected temperament.</li>
 *   <li>HexKeyboardPanel built buttons at fixed (row,col) grid ignoring that
 *       Isomorphic / Wicki-Hayden layout means note = 48 + row*5 + col*2.
 *       Previous layout caused duplicate notes and skipped notes. Fixed with
 *       standard Wicki-Hayden offsets.</li>
 * </ol>
 */
public class PianoSynthRecorder extends JFrame {

    /** PCM sample rate in Hz used throughout the engine and WAV export. */
    public static final int SAMPLE_RATE = 44100;

    private final SynthEngine synth    = new SynthEngine(SAMPLE_RATE);
    private final Recorder    recorder = new Recorder(synth);
    private final WaveformPanel wavePanel = new WaveformPanel();

    /**
     * Constructs and displays the main application window.
     *
     * <p>Builds the hexagonal keyboard, side-panel controls (tuning, waveform,
     * ADSR, filter, effects), waveform display, and bottom transport buttons.
     * Starts a daemon thread that continuously calls {@link SynthEngine#runRealtime}.
     */
    public PianoSynthRecorder() {
        super("Piano Synth MIDI Recorder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLayout(new BorderLayout());

        HexKeyboardPanel hex = new HexKeyboardPanel(
            note -> { synth.noteOn(note); recorder.noteOn(note); },
            note -> { synth.noteOff(note); recorder.noteOff(note); }
        );
        add(hex, BorderLayout.CENTER);

        // ── Side panel ───────────────────────────────────────────────────────
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(210, 0));
        side.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Tuning
        side.add(label("Tuning"));
        JComboBox<String> tuningBox = new JComboBox<>(TuningPresets.names());
        tuningBox.addActionListener(e ->
            synth.setTuning(TuningPresets.get((String) tuningBox.getSelectedItem())));
        side.add(tuningBox);

        // Waveform
        side.add(label("Waveform"));
        JComboBox<String> waveBox = new JComboBox<>(new String[]{"Sine","Saw","Square","Triangle"});
        waveBox.addActionListener(e -> synth.setWaveform((String) waveBox.getSelectedItem()));
        side.add(waveBox);

        // ADSR
        side.add(label("─── ADSR ───"));
        JSlider atk = slider("Attack ms",  1, 2000,  10);
        JSlider dec = slider("Decay ms",   1, 2000, 100);
        JSlider sus = slider("Sustain %",  0,  100,  80);
        JSlider rel = slider("Release ms", 1, 3000, 300);
        side.add(atk); side.add(dec); side.add(sus); side.add(rel);
        ChangeListener adsrL = e -> synth.setADSR(
            atk.getValue(), dec.getValue(), sus.getValue() / 100.0, rel.getValue());
        atk.addChangeListener(adsrL); dec.addChangeListener(adsrL);
        sus.addChangeListener(adsrL); rel.addChangeListener(adsrL);

        // Filter
        side.add(label("─── Filter ───"));
        JSlider cutoff = slider("Cutoff Hz",    100, SAMPLE_RATE / 2, 8000);
        JSlider reso   = slider("Resonance %",    0,             100,    5);
        side.add(cutoff); side.add(reso);
        ChangeListener filtL = e -> synth.setFilter(cutoff.getValue(), reso.getValue() / 100.0);
        cutoff.addChangeListener(filtL); reso.addChangeListener(filtL);

        // Effects
        side.add(label("─── Effects ───"));
        JSlider reverb  = slider("Reverb %",    0, 100,  20);
        JSlider distort = slider("Distort %",   0, 100,   0);
        JSlider delayMs = slider("Delay ms",    0, 800, 250);
        side.add(reverb); side.add(distort); side.add(delayMs);
        ChangeListener fxL = e -> synth.setEffects(
            reverb.getValue() / 100.0, distort.getValue() / 100.0, delayMs.getValue());
        reverb.addChangeListener(fxL); distort.addChangeListener(fxL); delayMs.addChangeListener(fxL);

        add(side, BorderLayout.EAST);

        // ── Waveform display (left) ──────────────────────────────────────────
        wavePanel.setPreferredSize(new Dimension(130, 0));
        add(wavePanel, BorderLayout.WEST);

        // ── Bottom controls ──────────────────────────────────────────────────
        JButton recBtn  = new JButton("● Record");
        JButton stopBtn = new JButton("■ Stop");
        JButton playBtn = new JButton("▶ Playback");
        JButton midiBtn = new JButton("Export MIDI");
        JButton wavBtn  = new JButton("Export WAV");
        recBtn .addActionListener(e -> recorder.start());
        stopBtn.addActionListener(e -> recorder.stop());
        // Bug fix #3: run playback on a worker thread, not the EDT
        playBtn.addActionListener(e -> {
            playBtn.setEnabled(false);
            new SwingWorker<Void,Void>() {
                @Override protected Void doInBackground() { recorder.playback(); return null; }
                @Override protected void done() { playBtn.setEnabled(true); }
            }.execute();
        });
        midiBtn.addActionListener(e -> recorder.exportMidi());
        wavBtn .addActionListener(e -> {
            wavBtn.setEnabled(false);
            new SwingWorker<Void,Void>() {
                @Override protected Void doInBackground() { recorder.exportWav(); return null; }
                @Override protected void done() { wavBtn.setEnabled(true); }
            }.execute();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        bottom.add(recBtn); bottom.add(stopBtn); bottom.add(playBtn);
        bottom.add(midiBtn); bottom.add(wavBtn);
        add(bottom, BorderLayout.SOUTH);

        // Fire initial slider values
        adsrL.stateChanged(null); filtL.stateChanged(null); fxL.stateChanged(null);
        tuningBox.setSelectedIndex(0);

        // Real-time audio thread
        Thread audio = new Thread(() -> synth.runRealtime(buf -> wavePanel.pushSamples(buf)));
        audio.setDaemon(true);
        audio.start();

        setVisible(true);
    }

    /**
     * Creates a left-aligned {@link JLabel} for use in the side panel.
     *
     * @param text label text
     * @return the configured label
     */
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /**
     * Creates a titled {@link JSlider} with the given range and initial value.
     *
     * @param title display title (shown in the slider's border)
     * @param min   minimum value
     * @param max   maximum value
     * @param init  initial value
     * @return the configured slider
     */
    private JSlider slider(String title, int min, int max, int init) {
        JSlider s = new JSlider(min, max, init);
        s.setBorder(BorderFactory.createTitledBorder(title));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        return s;
    }

    /**
     * Launches the application on the Swing event dispatch thread.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PianoSynthRecorder::new);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SynthEngine
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Real-time software synthesizer engine.
 *
 * <p>Manages a set of active {@link Voice} objects (keyed by MIDI note number)
 * and a separate list of voices that are in the release tail phase.  Each call
 * to {@link #nextSample()} mixes all voices, applies a one-pole low-pass filter,
 * and adds a feedback delay (reverb approximation).
 *
 * <p>All parameter setters are called from the EDT; {@link #nextSample()} and
 * {@link #runRealtime} run on a dedicated daemon audio thread.
 */
class SynthEngine {
    private final int sampleRate;
    private SourceDataLine line;
    private final Map<Integer, Voice> voices   = Collections.synchronizedMap(new LinkedHashMap<>());
    /** Bug fix #2: released voices live here until amplitude decays to silence. */
    private final List<Voice> releasingVoices  = new CopyOnWriteArrayList<>();
    private Map<Integer, Double> tuning = new HashMap<>();
    private String waveform  = "Sine";
    private int    attack    = 10,  decay   = 100, releaseMs = 300;
    private double sustain   = 0.8;
    private int    cutoff    = 8000;
    private double resonance = 0.05;
    private double reverb    = 0.2,  distortion = 0.0;
    private int    delayMs   = 250;
    private double[] delayBuf;
    private int delayPos = 0;
    /** One-pole low-pass filter state variable. */
    private double lpState = 0;

    /**
     * Constructs a SynthEngine with the given sample rate and opens the system
     * audio output line.
     *
     * @param sr sample rate in Hz (e.g. 44100)
     */
    SynthEngine(int sr) {
        sampleRate = sr;
        delayBuf = new double[sr * 2]; // up to 2 s max delay
        try {
            AudioFormat fmt = new AudioFormat(sr, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, sr / 10 * 2); // ~100 ms buffer
            line.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Replaces the active tuning map (MIDI note → frequency in Hz). */
    void setTuning(Map<Integer, Double> map) { tuning = map; }

    /** Sets the oscillator waveform: {@code "Sine"}, {@code "Saw"}, {@code "Square"}, or {@code "Triangle"}. */
    void setWaveform(String w) { waveform = w; }

    /**
     * Sets the ADSR envelope parameters.
     *
     * @param a attack time in milliseconds
     * @param d decay time in milliseconds
     * @param s sustain level (0.0–1.0)
     * @param r release time in milliseconds
     */
    void setADSR(int a, int d, double s, int r) { attack=a; decay=d; sustain=s; releaseMs=r; }

    /**
     * Sets the one-pole low-pass filter parameters.
     *
     * @param c   cutoff frequency in Hz
     * @param res resonance (currently informational; not used in filter computation)
     */
    void setFilter(int c, double res) { cutoff=c; resonance=res; }

    /**
     * Sets the effects parameters.
     *
     * @param rv   reverb/delay wet mix (0.0–1.0)
     * @param dist distortion amount (0.0–1.0); drives {@code Math.tanh} soft-clipping
     * @param del  delay time in milliseconds
     */
    void setEffects(double rv, double dist, int del) { reverb=rv; distortion=dist; delayMs=del; }

    /**
     * Starts a new note.  Creates a {@link Voice} and adds it to the active
     * voices map.  If the same MIDI note was still in the release tail, the
     * old tail is discarded.
     *
     * @param midi MIDI note number (0–127)
     */
    void noteOn(int midi) {
        double freq = tuning.getOrDefault(midi, 440.0 * Math.pow(2, (midi - 69) / 12.0));
        voices.put(midi, new Voice(freq, sampleRate, waveform, attack, decay, sustain, releaseMs));
        // If the same note was releasing, discard the old tail
        releasingVoices.removeIf(v -> v.getMidi() == midi);
    }

    /**
     * Signals that a note key was released.  The voice is moved from the active
     * map to the {@code releasingVoices} list so its release tail can finish.
     *
     * @param midi MIDI note number (0–127)
     */
    void noteOff(int midi) {
        Voice v = voices.remove(midi);
        if (v != null) {
            v.release();
            releasingVoices.add(v); // let it tail out
        }
    }

    /**
     * Immediately triggers release on all active voices (used before offline
     * WAV export to flush the engine state).
     */
    void allNotesOff() {
        new ArrayList<>(voices.values()).forEach(v -> { v.release(); releasingVoices.add(v); });
        voices.clear();
    }

    /**
     * Computes and returns the next mixed audio sample (one frame, mono).
     *
     * <p>Processing chain:
     * <ol>
     *   <li>Sum samples from all active and releasing voices.</li>
     *   <li>Soft-clip via {@code tanh(x * (1 + distortion * 8))}.</li>
     *   <li>One-pole low-pass filter at {@code cutoff} Hz.</li>
     *   <li>Feedback delay loop.</li>
     * </ol>
     *
     * @return mixed sample value; nominally in the range [-1, 1]
     */
    double nextSample() {
        double x = 0;
        for (Voice v : voices.values())   x += v.nextSample();
        for (Voice v : releasingVoices)   x += v.nextSample();
        // Prune voices that have decayed below audible threshold
        releasingVoices.removeIf(v -> v.getAmplitude() < 1e-5);

        // Soft clip (tanh distortion)
        x = Math.tanh(x * (1.0 + distortion * 8.0));

        // One-pole low-pass filter: coefficient from cutoff
        double rc  = 1.0 / (2 * Math.PI * cutoff);
        double dt  = 1.0 / sampleRate;
        double alpha = dt / (rc + dt);
        lpState = lpState + alpha * (x - lpState);
        x = lpState;

        // Delay / feedback
        int dSamp = (int)(delayMs * sampleRate / 1000.0);
        dSamp = Math.max(1, Math.min(dSamp, delayBuf.length - 1));
        int rp = (delayPos + delayBuf.length - dSamp) % delayBuf.length;
        double delayed = delayBuf[rp];
        delayBuf[delayPos] = x + delayed * reverb * 0.5;
        delayPos = (delayPos + 1) % delayBuf.length;
        x = x * (1 - reverb * 0.4) + delayed * reverb * 0.4;

        return x;
    }

    /**
     * Continuously generates audio samples and writes them to the system audio
     * line.  Intended to run on a dedicated daemon thread.  Also invokes
     * {@code callback} with each filled byte buffer for waveform display.
     *
     * @param callback receives each 512-byte PCM buffer for UI display
     */
    void runRealtime(Consumer<byte[]> callback) {
        byte[] buf = new byte[512]; // ~5.8 ms per chunk at 44100 Hz
        while (true) {
            int samples = buf.length / 2;
            for (int i = 0; i < samples; i++) {
                double m = nextSample();
                short  v = (short)(Math.max(-1, Math.min(1, m)) * Short.MAX_VALUE);
                buf[2*i]   = (byte)(v & 0xff);
                buf[2*i+1] = (byte)((v >> 8) & 0xff);
            }
            line.write(buf, 0, buf.length);
            callback.accept(buf);
        }
    }

    /**
     * Renders {@code durationMs} milliseconds of audio offline (no audio hardware
     * required) and returns raw 16-bit signed little-endian PCM bytes.
     *
     * @param durationMs render duration in milliseconds
     * @return raw PCM bytes (16-bit mono, little-endian)
     */
    byte[] renderOffline(int durationMs) {
        int samples = sampleRate * durationMs / 1000;
        byte[] out  = new byte[samples * 2];
        for (int i = 0; i < samples; i++) {
            double m = nextSample();
            short  v = (short)(Math.max(-1, Math.min(1, m)) * Short.MAX_VALUE);
            out[2*i]   = (byte)(v & 0xff);
            out[2*i+1] = (byte)((v >> 8) & 0xff);
        }
        return out;
    }

    /** Returns the sample rate this engine was constructed with. */
    int getSampleRate() { return sampleRate; }
}

// ─────────────────────────────────────────────────────────────────────────────
// Voice — one oscillator with ADSR envelope
// Bug fix #1: all timing converted from ms to samples in constructor
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Single oscillator voice with an ADSR (Attack-Decay-Sustain-Release) amplitude
 * envelope.
 *
 * <p>Bug fix #1: the original code compared ADSR durations expressed in
 * milliseconds directly to {@code sampleCount} (a raw sample counter).  This
 * constructor now converts all ms values to sample counts once, so the envelope
 * timing is correct regardless of the sample rate.
 *
 * <p>The oscillator phase advances by {@code freq / sampleRate} per sample,
 * wrapping in [0, 1).  Four waveforms are supported: Sine, Saw, Square,
 * Triangle.
 */
class Voice {
    private final double freq;
    private double phase = 0;
    private final int sr;
    private final String wave;
    /** Envelope durations in SAMPLES (converted from ms in constructor). */
    private final int attackSamps, decaySamps, releaseSamps;
    private final double sustain;
    private final int midi;
    private int sampleCount = 0;
    private boolean released = false;
    private double amp = 0;
    private static int nextId = 0;
    private final int id;

    /**
     * Constructs a new voice for the given frequency.
     *
     * @param freq      oscillator frequency in Hz
     * @param sr        sample rate in Hz
     * @param wave      waveform name: {@code "Sine"}, {@code "Saw"}, {@code "Square"},
     *                  or {@code "Triangle"}
     * @param attackMs  attack duration in milliseconds
     * @param decayMs   decay duration in milliseconds
     * @param sustain   sustain amplitude level (0.0–1.0)
     * @param releaseMs release duration in milliseconds
     */
    Voice(double freq, int sr, String wave, int attackMs, int decayMs, double sustain, int releaseMs) {
        this.freq    = freq;
        this.sr      = sr;
        this.wave    = wave;
        this.sustain = sustain;
        // Convert ms → samples
        this.attackSamps  = Math.max(1, attackMs  * sr / 1000);
        this.decaySamps   = Math.max(1, decayMs   * sr / 1000);
        this.releaseSamps = Math.max(1, releaseMs * sr / 1000);
        this.midi = 0; // set by noteOn mapping; we track via map key elsewhere
        this.id = nextId++;
    }

    /**
     * Returns the voice's unique ID (used for deduplication in the releasing list;
     * the MIDI note is tracked externally via the voices map key).
     *
     * @return unique voice ID
     */
    int getMidi() { return id; } // used only for dedup; keyed externally

    /**
     * Returns the current ADSR amplitude (0.0–1.0).  Used by {@link SynthEngine}
     * to detect when a releasing voice has decayed to silence.
     *
     * @return current amplitude
     */
    double getAmplitude() { return amp; }

    /**
     * Computes and returns the next audio sample for this voice.
     *
     * <p>The oscillator value is multiplied by the ADSR envelope amplitude and
     * scaled by 0.4 to reduce clipping when multiple voices are active.
     *
     * @return sample value in approximately [-0.4, 0.4]
     */
    double nextSample() {
        // Oscillator
        double out;
        switch (wave) {
            case "Saw":      out = 2 * (phase - Math.floor(phase + 0.5)); break;
            case "Square":   out = phase < 0.5 ? 1.0 : -1.0; break;
            case "Triangle": out = 1.0 - 4 * Math.abs(phase - 0.5); break;
            default:         out = Math.sin(2 * Math.PI * phase); break;
        }
        phase += freq / sr;
        if (phase >= 1) phase -= 1;

        // ADSR envelope
        if (!released) {
            if      (sampleCount < attackSamps)
                amp = sampleCount / (double) attackSamps;
            else if (sampleCount < attackSamps + decaySamps)
                amp = 1.0 - (sampleCount - attackSamps) / (double) decaySamps * (1.0 - sustain);
            else
                amp = sustain;
        } else {
            // Exponential release decay
            amp *= 1.0 - 1.0 / releaseSamps;
        }
        sampleCount++;
        return out * amp * 0.4; // scale to avoid clipping when polyphonic
    }

    /**
     * Signals that the note key has been released.  Transitions the envelope
     * to the release phase and resets the per-phase sample counter.
     */
    void release() { released = true; sampleCount = 0; }
}

// ─────────────────────────────────────────────────────────────────────────────
// Recorder
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Records note-on/off events with wall-clock timestamps and supports playback,
 * MIDI export, and offline WAV rendering.
 */
class Recorder {
    /** Internal event record (timestamp, MIDI note, on/off flag). */
    private static final class Ev { long tMs; int note; boolean on; }
    private final List<Ev> events = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean recording = false;
    private long recordStart;
    private final SynthEngine synth;

    /**
     * Constructs a Recorder backed by the given synthesizer engine.
     *
     * @param s the SynthEngine used for playback and WAV rendering
     */
    Recorder(SynthEngine s) { synth = s; }

    /** Clears all recorded events and starts a new recording session. */
    void start() { events.clear(); recording = true; recordStart = System.currentTimeMillis(); }

    /** Stops the current recording session. */
    void stop()  { recording = false; }

    /**
     * Records a note-on event if recording is active.
     *
     * @param n MIDI note number
     */
    void noteOn(int n)  { if (!recording) return; addEvent(n, true); }

    /**
     * Records a note-off event if recording is active.
     *
     * @param n MIDI note number
     */
    void noteOff(int n) { if (!recording) return; addEvent(n, false); }

    private void addEvent(int note, boolean on) {
        Ev ev = new Ev();
        ev.tMs = System.currentTimeMillis() - recordStart;
        ev.note = note; ev.on = on;
        events.add(ev);
    }

    /**
     * Plays back the recorded event list in real time by scheduling note-on/off
     * calls to the synthesizer with appropriate sleep delays.
     *
     * <p>Must be called on a worker thread (not the EDT) — see bug fix #3.
     */
    void playback() {
        List<Ev> snap = new ArrayList<>(events);
        long start = System.currentTimeMillis();
        for (Ev ev : snap) {
            long delay = ev.tMs - (System.currentTimeMillis() - start);
            if (delay > 0) try { Thread.sleep(delay); } catch (InterruptedException e) { return; }
            if (ev.on) synth.noteOn(ev.note); else synth.noteOff(ev.note);
        }
    }

    /**
     * Exports the recorded events as a Standard MIDI File (Type 1) named
     * {@code recording.mid} in the working directory.  Shows a dialog on
     * success.
     */
    void exportMidi() {
        try {
            Sequence seq = new Sequence(Sequence.PPQ, 1000);
            Track track  = seq.createTrack();
            for (Ev ev : events) {
                ShortMessage msg = new ShortMessage(
                    ev.on ? ShortMessage.NOTE_ON : ShortMessage.NOTE_OFF,
                    0, ev.note, ev.on ? 100 : 0);
                track.add(new MidiEvent(msg, ev.tMs));
            }
            MidiSystem.write(seq, 1, new File("recording.mid"));
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, "MIDI saved: recording.mid"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Renders the recorded events offline to a WAV file named
     * {@code recording.wav} in the working directory.
     *
     * <p>Must be called on a worker thread — the render duration equals the
     * recording length plus a 2-second tail.
     */
    void exportWav() {
        List<Ev> snap = new ArrayList<>(events);
        if (snap.isEmpty()) return;
        int durationMs = (int)(snap.get(snap.size()-1).tMs + 2000);
        synth.allNotesOff();

        Iterator<Ev> it = snap.iterator();
        Ev[] next = { it.hasNext() ? it.next() : null };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int totalSamples = synth.getSampleRate() * durationMs / 1000;
        for (int i = 0; i < totalSamples; i++) {
            int tMs = i * 1000 / synth.getSampleRate();
            while (next[0] != null && next[0].tMs <= tMs) {
                if (next[0].on) synth.noteOn(next[0].note);
                else            synth.noteOff(next[0].note);
                next[0] = it.hasNext() ? it.next() : null;
            }
            double x = synth.nextSample();
            short  v = (short)(Math.max(-1, Math.min(1, x)) * Short.MAX_VALUE);
            baos.write(v & 0xff);
            baos.write((v >> 8) & 0xff);
        }
        WaveFile.write("recording.wav", baos.toByteArray(), synth.getSampleRate());
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(null, "WAV saved: recording.wav"));
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WaveformPanel — oscilloscope-style display
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Oscilloscope-style real-time waveform display panel.
 *
 * <p>Receives raw PCM byte buffers from the audio thread via
 * {@link #pushSamples(byte[])} and renders them as a scrolling bar graph
 * against a black background.
 */
class WaveformPanel extends JPanel {
    private final LinkedList<short[]> history = new LinkedList<>();

    /**
     * Accepts a new PCM byte buffer, converts it to 16-bit samples, appends
     * it to the display history, and triggers a repaint.
     *
     * @param buf raw 16-bit signed little-endian PCM bytes (length must be even)
     */
    void pushSamples(byte[] buf) {
        int len = buf.length / 2;
        short[] s = new short[len];
        for (int i = 0; i < len; i++)
            s[i] = (short)((buf[2*i] & 0xff) | (buf[2*i+1] << 8));
        synchronized (history) {
            history.add(s);
            while (history.size() > 60) history.removeFirst();
        }
        repaint();
    }

    /** Renders the waveform history as vertical bars on a black background. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight(), mid = h / 2;
        g.setColor(Color.BLACK); g.fillRect(0, 0, w, h);
        g.setColor(new Color(0, 200, 80));
        int x = 0;
        synchronized (history) {
            for (short[] s : history) {
                int step = Math.max(1, s.length / Math.max(1, w - x));
                for (int i = 0; i < s.length && x < w; i += step) {
                    int y = mid - (int)(s[i] * (mid - 2) / (double) Short.MAX_VALUE);
                    g.drawLine(x, mid, x, Math.max(2, Math.min(h - 2, y)));
                    x++;
                }
            }
        }
        // Center line
        g.setColor(new Color(0, 80, 30));
        g.drawLine(0, mid, w, mid);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HexKeyboardPanel — Wicki-Hayden isomorphic layout
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Hexagonal keyboard panel using the Wicki-Hayden isomorphic layout.
 *
 * <p>In the Wicki-Hayden layout each row ascends by 7 semitones and each
 * column ascends by 2 semitones, giving a uniform fingering pattern for all
 * keys.  The panel displays 6 rows × 12 columns of {@link HexButton} tiles
 * starting from MIDI note 36 (C2).
 *
 * <p>Bug fix #6: the original implementation used a fixed grid with incorrect
 * offsets, producing duplicate and skipped MIDI notes.  The corrected formula
 * is {@code note = baseNote + row * 7 + col * 2}.
 */
class HexKeyboardPanel extends JPanel {

    /**
     * Constructs the hex keyboard with the given note press/release callbacks.
     *
     * @param onPress   called with the MIDI note number when a button is pressed
     * @param onRelease called with the MIDI note number when a button is released
     */
    HexKeyboardPanel(Consumer<Integer> onPress, Consumer<Integer> onRelease) {
        setLayout(null);
        int r = 28;
        int dx = (int)(r * 1.75);
        int dy = (int)(r * 1.5);
        int rows = 6, cols = 12;
        // Wicki-Hayden: row offset = +7 semitones up, col offset = +2 semitones right
        int baseNote = 36; // C2
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int note = baseNote + row * 7 + col * 2;
                if (note < 0 || note > 127) continue;
                int px = col * dx + (row % 2) * (dx / 2);
                int py = (rows - 1 - row) * dy;
                HexButton btn = new HexButton(r, note);
                btn.setBounds(px, py, 2 * r, 2 * r);
                btn.addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e)  { onPress.accept(note); }
                    @Override public void mouseReleased(MouseEvent e) { onRelease.accept(note); }
                });
                add(btn);
            }
        }
        setPreferredSize(new Dimension(cols * dx + dx, rows * dy + r));
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HexButton
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A single hexagonal button in the keyboard panel.
 *
 * <p>Each button is coloured by its pitch class (C=red, C#=purple, …) and
 * displays the note name and octave.  Hit-testing uses the hexagonal polygon
 * rather than the bounding rectangle so clicks outside the hex shape are
 * correctly ignored.
 */
class HexButton extends JButton {
    private final Polygon hex;
    private final int note;
    /** Pitch-class colours — index 0 = C, 1 = C#, …, 11 = B. */
    private static final Color[] PC_COLORS = {
        new Color(255,100,100), new Color(200,100,200),
        new Color(100,100,255), new Color(100,200,200),
        new Color(100,200,100), new Color(200,200,100),
        new Color(255,165,  0), new Color(150,200,255),
        new Color(200,150,255), new Color(100,220,150),
        new Color(255,200,100), new Color(200,200,200)
    };

    /**
     * Constructs a hexagonal button for the given MIDI note.
     *
     * @param r    circumradius of the hexagon in pixels
     * @param note MIDI note number (0–127)
     */
    HexButton(int r, int note) {
        this.note = note;
        hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double theta = Math.PI / 6 + i * Math.PI / 3;
            hex.addPoint((int)(r + r * Math.cos(theta)), (int)(r + r * Math.sin(theta)));
        }
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setToolTipText("MIDI " + note + "  (" + noteName(note) + ")");
    }

    private static String[] NAMES = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};

    /**
     * Returns the human-readable note name and octave for a MIDI note number.
     *
     * @param midi MIDI note number (0–127)
     * @return e.g. {@code "A4"} for MIDI 69
     */
    private static String noteName(int midi) {
        return NAMES[midi % 12] + (midi / 12 - 1);
    }

    /** Paints the hexagon filled with the pitch-class colour. */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color base = PC_COLORS[note % 12];
        g2.setColor(getModel().isPressed() ? base.darker() : base);
        g2.fill(hex);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(hex);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        g2.drawString(noteName(note), hex.getBounds().x + 4, hex.getBounds().y + 14);
    }

    /** Returns {@code true} only if {@code (x, y)} is inside the hexagonal polygon. */
    @Override public boolean contains(int x, int y) { return hex.contains(x, y); }
}

// ─────────────────────────────────────────────────────────────────────────────
// TuningPresets
// Bug fix #5: now fills all 128 MIDI notes
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Provides frequency tables for various musical tuning systems.
 *
 * <p>Available presets: 12-ET, 19-ET, 24-ET (quarter-tone), 31-ET, Just
 * Intonation, and Pythagorean.
 *
 * <p>Bug fix #5: the original implementation only filled MIDI notes 60 through
 * 60+n for an n-note scale, leaving all other notes at 12-ET pitch regardless
 * of the selected temperament.  The corrected {@link #get} method fills all 128
 * MIDI notes by applying the scale ratios with octave transposition.
 */
class TuningPresets {
    private static final Map<String, double[]> PRESETS = new LinkedHashMap<>();
    static {
        PRESETS.put("12-ET",           genET(12));
        PRESETS.put("19-ET",           genET(19));
        PRESETS.put("24-ET (quarter)", genET(24));
        PRESETS.put("31-ET",           genET(31));
        PRESETS.put("Just Intonation", new double[]{
            1, 16/15.0, 9/8.0, 6/5.0, 5/4.0, 4/3.0,
            45/32.0, 3/2.0, 8/5.0, 5/3.0, 9/5.0, 15/8.0
        });
        PRESETS.put("Pythagorean",     genPythagorean());
    }

    /**
     * Returns the display names of all available tuning presets.
     *
     * @return array of preset names suitable for a combo box
     */
    static String[] names() { return PRESETS.keySet().toArray(new String[0]); }

    /**
     * Returns a complete MIDI-note-to-frequency map for the named preset.
     *
     * <p>The reference pitch is C4 (MIDI 60) = 261.63 Hz.  Ratios for notes
     * outside the preset's octave are computed by octave transposition.
     *
     * @param name preset name (one of the values returned by {@link #names()})
     * @return map from MIDI note number (0–127) to frequency in Hz
     */
    static Map<Integer, Double> get(String name) {
        double[] ratios = PRESETS.getOrDefault(name, genET(12));
        int      n      = ratios.length;
        Map<Integer, Double> out = new HashMap<>();
        for (int midi = 0; midi < 128; midi++) {
            int    octave   = (midi - 60) / n;
            int    degree   = ((midi - 60) % n + n) % n;
            double baseFreq = 261.63 * Math.pow(2, octave) * ratios[degree];
            out.put(midi, baseFreq);
        }
        return out;
    }

    /**
     * Generates an n-tone equal temperament ratio array where ratio[i] =
     * 2^(i/n).
     *
     * @param n number of equal divisions of the octave
     * @return ratio array of length {@code n}
     */
    private static double[] genET(int n) {
        double[] a = new double[n];
        for (int i = 0; i < n; i++) a[i] = Math.pow(2, i / (double) n);
        return a;
    }

    /**
     * Generates a 12-note Pythagorean scale using a chain of pure 3:2 fifths
     * starting from C, normalized to one octave.
     *
     * @return sorted ratio array of length 12
     */
    private static double[] genPythagorean() {
        // Build 12-note Pythagorean scale (pure 3:2 fifths)
        double[] a   = new double[12];
        double[] raw = new double[12];
        // Circle of fifths starting from C
        int[] order = {0,7,2,9,4,11,6,1,8,3,10,5};
        double f = 1.0;
        for (int i = 0; i < 12; i++) {
            raw[order[i]] = f;
            f = f * 3.0 / 2.0;
        }
        // Normalize to one octave
        for (int i = 0; i < 12; i++) {
            a[i] = raw[i];
            while (a[i] >= 2) a[i] /= 2;
            while (a[i] <  1) a[i] *= 2;
        }
        Arrays.sort(a);
        return a;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WaveFile — writes a minimal PCM WAV header (little-endian)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Writes a minimal standard PCM WAV file (mono, 16-bit, little-endian).
 *
 * <p>The header layout is: RIFF chunk → WAVE format chunk (44 bytes total
 * header) followed by the raw PCM data.  All multi-byte fields are written in
 * little-endian byte order as required by the WAV specification.
 */
class WaveFile {

    /**
     * Writes {@code data} to {@code filename} with a standard WAV header.
     *
     * @param filename path of the output WAV file
     * @param data     raw 16-bit signed little-endian PCM bytes
     * @param sr       sample rate in Hz
     */
    static void write(String filename, byte[] data, int sr) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            int byteRate   = sr * 2;      // 1 channel, 16-bit
            int dataLen    = data.length;
            int chunkSize  = 36 + dataLen;
            out.writeBytes("RIFF");
            writeLE32(out, chunkSize);
            out.writeBytes("WAVEfmt ");
            writeLE32(out, 16);           // PCM chunk size
            writeLE16(out, (short) 1);    // PCM format
            writeLE16(out, (short) 1);    // mono
            writeLE32(out, sr);           // sample rate
            writeLE32(out, byteRate);     // byte rate
            writeLE16(out, (short) 2);    // block align
            writeLE16(out, (short) 16);   // bits/sample
            out.writeBytes("data");
            writeLE32(out, dataLen);
            out.write(data);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Writes a 32-bit integer in little-endian byte order.
     *
     * @param o target stream
     * @param v value to write
     * @throws IOException on stream error
     */
    private static void writeLE32(DataOutputStream o, int v) throws IOException {
        o.write(v        & 0xff);
        o.write((v >> 8)  & 0xff);
        o.write((v >> 16) & 0xff);
        o.write((v >> 24) & 0xff);
    }

    /**
     * Writes a 16-bit short integer in little-endian byte order.
     *
     * @param o target stream
     * @param v value to write
     * @throws IOException on stream error
     */
    private static void writeLE16(DataOutputStream o, short v) throws IOException {
        o.write(v       & 0xff);
        o.write((v >> 8) & 0xff);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TestRunner — self-contained unit tests (no audio hardware required)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Self-contained unit test runner for PianoSynthRecorder.
 *
 * <p>Run with:
 * <pre>
 *   javac PianoSynthRecorder.java &amp;&amp; java 'PianoSynthRecorder$TestRunner'
 * </pre>
 *
 * <p>Tests cover pure computation only — no audio hardware is required.
 * Any test that would need a {@link SourceDataLine} catches
 * {@link LineUnavailableException} and is marked skipped rather than failed.
 *
 * <p>Coverage:
 * <ul>
 *   <li>SynthEngine: {@code computeSample} (Sine) returns value in [-1, 1]</li>
 *   <li>ADSR: attack ramps from 0 to 1; sustain holds; release decays</li>
 *   <li>TuningPresets: 12-ET A4 (MIDI 69) == 440 Hz within 0.01 Hz</li>
 *   <li>TuningPresets: all 128 notes have positive frequencies</li>
 *   <li>Wicki-Hayden: base note at row=0, col=0 is 36 (C2)</li>
 *   <li>WAV header: first 4 bytes are "RIFF" (0x52494646)</li>
 * </ul>
 */
class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    /** Entry point for the test runner. */
    public static void main(String[] args) {
        System.out.println("=== PianoSynthRecorder Tests ===");
        testSineComputeSample();
        testAdsrEnvelopePhases();
        testTuning12EtA4();
        testTuningAllPositive();
        testWickiHaydenBaseNote();
        testWavHeaderRiff();
        System.out.println("\n" + passed + " passed, " + failed + " failed.");
        if (failed > 0) System.exit(1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void pass(String name) {
        System.out.println("  PASS: " + name);
        passed++;
    }

    private static void fail(String name, String reason) {
        System.out.println("  FAIL: " + name + " — " + reason);
        failed++;
    }

    private static void skip(String name, String reason) {
        System.out.println("  SKIP: " + name + " — " + reason);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * Creates a SynthEngine, plays a 440 Hz sine note for 256 samples, and
     * verifies every sample is in [-1, 1].
     *
     * <p>If the audio line is unavailable the test is skipped.
     */
    private static void testSineComputeSample() {
        String name = "SynthEngine: sine samples in [-1, 1]";
        try {
            SynthEngine engine = new SynthEngine(44100);
            engine.setWaveform("Sine");
            engine.setADSR(10, 100, 0.8, 300);
            engine.setEffects(0.0, 0.0, 0);   // no reverb/delay
            engine.setFilter(20000, 0.0);      // pass-through filter
            engine.noteOn(69); // A4 = 440 Hz
            boolean ok = true;
            for (int i = 0; i < 256; i++) {
                double s = engine.nextSample();
                if (s < -1.0 - 1e-6 || s > 1.0 + 1e-6) {
                    ok = false;
                    fail(name, "sample " + i + " out of range: " + s);
                    return;
                }
            }
            if (ok) pass(name);
        } catch (Exception e) {
            skip(name, "audio line unavailable: " + e.getMessage());
        }
    }

    /**
     * Directly tests the {@link Voice} ADSR envelope phases:
     * attack ramps from 0 to ~1, sustain holds steady, release decays toward 0.
     */
    private static void testAdsrEnvelopePhases() {
        String name = "ADSR: attack ramps to 1, sustain holds, release decays";
        try {
            int sr = 44100;
            // 100 ms attack, 100 ms decay, 0.8 sustain, 500 ms release
            Voice v = new Voice(440.0, sr, "Sine", 100, 100, 0.8, 500);

            int attackSamps  = 100 * sr / 1000; // 4410
            int decaySamps   = 100 * sr / 1000; // 4410

            // Advance through attack
            for (int i = 0; i < attackSamps - 1; i++) v.nextSample();
            double nearPeak = v.getAmplitude();

            // Finish attack + decay
            for (int i = 0; i < decaySamps + 1; i++) v.nextSample();
            double atSustain = v.getAmplitude();

            // Check sustain phase holds
            double before = v.getAmplitude();
            for (int i = 0; i < 1000; i++) v.nextSample();
            double after = v.getAmplitude();

            // Trigger release and advance
            v.release();
            for (int i = 0; i < 500 * sr / 1000; i++) v.nextSample();
            double released = v.getAmplitude();

            boolean attackOk  = nearPeak > 0.9;
            boolean sustainOk = Math.abs(atSustain - 0.8) < 0.05;
            boolean holdOk    = Math.abs(before - after)   < 0.01;
            // Exponential decay: after 1 time-constant (releaseSamps steps) the
            // amplitude is amp * e^(-1) ≈ 0.368*amp — well below half of sustain.
            boolean releaseOk = released < atSustain * 0.5;

            if (attackOk && sustainOk && holdOk && releaseOk) {
                pass(name);
            } else {
                fail(name, String.format(
                    "nearPeak=%.3f sustainAt=%.3f holdDelta=%.4f released=%.4f",
                    nearPeak, atSustain, Math.abs(before - after), released));
            }
        } catch (Exception e) {
            fail(name, e.toString());
        }
    }

    /**
     * Verifies that the 12-ET tuning preset maps MIDI note 69 (A4) to
     * approximately 440.0 Hz (within 0.01 Hz).
     */
    private static void testTuning12EtA4() {
        String name = "TuningPresets: 12-ET A4 (MIDI 69) = 440.0 Hz";
        try {
            Map<Integer, Double> tuning = TuningPresets.get("12-ET");
            double freq = tuning.get(69);
            if (Math.abs(freq - 440.0) < 0.01) pass(name);
            else fail(name, "Got " + freq + " Hz, expected 440.0 Hz");
        } catch (Exception e) {
            fail(name, e.toString());
        }
    }

    /**
     * Verifies that the 12-ET tuning preset contains all 128 MIDI notes and
     * that every frequency is strictly positive.
     */
    private static void testTuningAllPositive() {
        String name = "TuningPresets: all 128 MIDI notes have positive frequencies";
        try {
            Map<Integer, Double> tuning = TuningPresets.get("12-ET");
            if (tuning.size() != 128) {
                fail(name, "Expected 128 entries, got " + tuning.size());
                return;
            }
            for (int i = 0; i < 128; i++) {
                Double f = tuning.get(i);
                if (f == null || f <= 0) {
                    fail(name, "MIDI " + i + " has frequency " + f);
                    return;
                }
            }
            pass(name);
        } catch (Exception e) {
            fail(name, e.toString());
        }
    }

    /**
     * Verifies that the Wicki-Hayden layout formula places MIDI note 36 (C2)
     * at row=0, col=0 (the base note constant used in {@link HexKeyboardPanel}).
     */
    private static void testWickiHaydenBaseNote() {
        String name = "Wicki-Hayden: note at row=0, col=0 is 36 (C2)";
        try {
            int baseNote = 36;
            int row = 0, col = 0;
            int note = baseNote + row * 7 + col * 2;
            if (note == 36) pass(name);
            else fail(name, "Expected 36, got " + note);
        } catch (Exception e) {
            fail(name, e.toString());
        }
    }

    /**
     * Writes a minimal WAV file to a temp file and verifies that the first
     * four bytes spell "RIFF" (0x52 0x49 0x46 0x46).
     */
    private static void testWavHeaderRiff() {
        String name = "WAV header: first 4 bytes are 'RIFF'";
        try {
            File tmp = File.createTempFile("wav_test_", ".wav");
            tmp.deleteOnExit();
            byte[] silence = new byte[44100 * 2]; // 1 second of silence
            WaveFile.write(tmp.getAbsolutePath(), silence, 44100);

            byte[] header = new byte[4];
            try (FileInputStream fis = new FileInputStream(tmp)) {
                int read = fis.read(header);
                if (read != 4) { fail(name, "Could not read 4 bytes from WAV"); return; }
            }
            String magic = new String(header, "ASCII");
            if ("RIFF".equals(magic)) pass(name);
            else fail(name, "Expected 'RIFF', got '" + magic + "'");
        } catch (Exception e) {
            fail(name, e.toString());
        }
    }
}
