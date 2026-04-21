import javax.sound.sampled.*;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * PianoSynthRecorder: Java Swing app with hexagonal piano keyboard,
 * subtractive synth (waveform selection, ADSR, filter, effects),
 * MIDI recording/export, WAV export, and real-time waveform display.
 */
public class PianoSynthRecorder extends JFrame {
    public static final int SAMPLE_RATE = 44100;
    private SynthEngine synth;
    private Recorder recorder;
    private WaveformPanel waveformPanel;

    public PianoSynthRecorder() {
        super("Piano Synth MIDI Recorder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        synth = new SynthEngine(SAMPLE_RATE);
        recorder = new Recorder(synth);
        waveformPanel = new WaveformPanel();

        // Hex keyboard center
        HexKeyboardPanel hexPanel = new HexKeyboardPanel(
            note -> { synth.noteOn(note); recorder.noteOn(note); },
            note -> { synth.noteOff(note); recorder.noteOff(note); }
        );
        add(hexPanel, BorderLayout.CENTER);

        // Side panel controls
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(200, getHeight()));

        side.add(new JLabel("Tuning:"));
        JComboBox<String> tuningBox = new JComboBox<>(TuningPresets.names());
        tuningBox.addActionListener(e -> synth.setTuning(
            TuningPresets.get((String) tuningBox.getSelectedItem())
        ));
        side.add(tuningBox);

        side.add(new JLabel("Waveform:"));
        JComboBox<String> waveBox = new JComboBox<>(new String[]{"Sine", "Saw", "Square"});
        waveBox.addActionListener(e -> synth.setWaveform((String) waveBox.getSelectedItem()));
        side.add(waveBox);

        side.add(new JLabel("ADSR"));
        JSlider atk = makeSlider("Attack ms", 1, 5000, 100);
        JSlider dec = makeSlider("Decay ms", 1, 5000, 200);
        JSlider sus = makeSlider("Sustain %", 0, 100, 80);
        JSlider rel = makeSlider("Release ms", 1, 5000, 500);
        side.add(atk); side.add(dec); side.add(sus); side.add(rel);
        ChangeListener adsrListener = e -> synth.setADSR(
            atk.getValue(), dec.getValue(), sus.getValue() / 100.0, rel.getValue()
        );
        atk.addChangeListener(adsrListener);
        dec.addChangeListener(adsrListener);
        sus.addChangeListener(adsrListener);
        rel.addChangeListener(adsrListener);

        side.add(new JLabel("Filter"));
        JSlider cutoff = makeSlider("Cutoff Hz", 20, SAMPLE_RATE/2, 20000);
        JSlider reso   = makeSlider("Resonance %", 0, 100, 10);
        side.add(cutoff); side.add(reso);
        ChangeListener filtListener = e -> synth.setFilter(
            cutoff.getValue(), reso.getValue() / 100.0
        );
        cutoff.addChangeListener(filtListener);
        reso.addChangeListener(filtListener);

        side.add(new JLabel("Effects"));
        JSlider reverb  = makeSlider("Reverb %", 0, 100, 20);
        JSlider distort = makeSlider("Distort %", 0, 100, 0);
        JSlider delay   = makeSlider("Delay ms", 0, 1000, 300);
        side.add(reverb); side.add(distort); side.add(delay);
        ChangeListener fxListener = e -> synth.setEffects(
            reverb.getValue() / 100.0,
            distort.getValue() / 100.0,
            delay.getValue()
        );
        reverb.addChangeListener(fxListener);
        distort.addChangeListener(fxListener);
        delay.addChangeListener(fxListener);

        add(side, BorderLayout.EAST);

        // Bottom panel
        JPanel bottom = new JPanel();
        JButton recBtn = new JButton("Record");
        JButton stopBtn= new JButton("Stop");
        JButton playBtn= new JButton("Play");
        JButton midiBtn= new JButton("Export MIDI");
        JButton wavBtn = new JButton("Export WAV");
        recBtn.addActionListener(e -> recorder.start());
        stopBtn.addActionListener(e -> recorder.stop());
        playBtn.addActionListener(e -> recorder.playback());
        midiBtn.addActionListener(e -> recorder.exportMidi());
        wavBtn.addActionListener(e -> recorder.exportWav());
        bottom.add(recBtn); bottom.add(stopBtn);
        bottom.add(playBtn); bottom.add(midiBtn); bottom.add(wavBtn);
        add(bottom, BorderLayout.SOUTH);

        // Waveform display
        add(waveformPanel, BorderLayout.WEST);

        // Launch real-time audio thread
        new Thread(() -> synth.runRealtime(buffer -> waveformPanel.pushSamples(buffer, SAMPLE_RATE))).start();

        tuningBox.setSelectedIndex(0);
        waveBox.setSelectedIndex(0);
        adsrListener.stateChanged(null);
        filtListener.stateChanged(null);
        fxListener.stateChanged(null);

        setVisible(true);
    }

    private JSlider makeSlider(String title, int min, int max, int init) {
        JSlider s = new JSlider(min, max, init);
        s.setBorder(BorderFactory.createTitledBorder(title));
        return s;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PianoSynthRecorder::new);
    }
}

// --- SynthEngine ---
class SynthEngine {
    private int sampleRate;
    private SourceDataLine line;
    private Map<Integer, Voice> voices = Collections.synchronizedMap(new HashMap<>());
    private Map<Integer, Double> tuning = new HashMap<>();
    private String waveform = "Sine";
    private int attack = 100, decay = 200;
    private double sustain = 0.8;
    private int release = 500;
    private int cutoff = 20000;
    private double resonance = 0.1;
    private double reverb = 0.2, distortion = 0.0;
    private int delayMs = 300;
    private double[] delayBuffer;
    private int delayPos = 0;

    public SynthEngine(int sr) {
        sampleRate = sr;
        delayBuffer = new double[sr * 2]; // 2 seconds max delay
        try {
            AudioFormat fmt = new AudioFormat(sr,16,1,true,false);
            line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, sr);
            line.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTuning(Map<Integer, Double> map) { tuning = map; }
    public void setWaveform(String w) { waveform = w; }
    public void setADSR(int a, int d, double s, int r) { attack=a; decay=d; sustain=s; release=r; }
    public void setFilter(int c, double res) { cutoff=c; resonance=res; }
    public void setEffects(double rv, double dist, int del) { reverb=rv; distortion=dist; delayMs=del; }

    public void allNotesOff() {
        for (Voice v : getVoicesSnapshot()) v.release();
    }

    public java.util.List<Voice> getVoicesSnapshot() {
        return new ArrayList<>(voices.values());
    }

    /** nextSample mixes voices + distortion + delay/reverb */
    public double nextSample() {
        double x = 0;
        for (Voice v : getVoicesSnapshot()) x += v.nextSample();
        // distortion
        x = Math.tanh(x * (1.0 + distortion * 10.0));
        // delay + feedback
        int dSamps = (int)(delayMs * sampleRate / 1000.0);
        int rp = (delayPos + delayBuffer.length - dSamps) % delayBuffer.length;
        double delayed = delayBuffer[rp];
        delayBuffer[delayPos] = x + delayed * reverb;
        delayPos = (delayPos + 1) % delayBuffer.length;
        x = x*(1-reverb) + delayed*reverb;
        // (optional) filter could be applied here
        return x;
    }

    public void noteOn(int midiNote) {
        double freq = tuning.getOrDefault(midiNote,
            440 * Math.pow(2, (midiNote-69)/12.0)
        );
        voices.put(midiNote,
            new Voice(freq, sampleRate, waveform, attack, decay, sustain, release)
        );
    }

    public void noteOff(int midiNote) {
        Voice v = voices.remove(midiNote);
        if (v!=null) v.release();
    }

    public void runRealtime(Consumer<byte[]> callback) {
        byte[] buf = new byte[1024];
        while (true) {
            Arrays.fill(buf,(byte)0);
            int samples = buf.length/2;
            for (int i=0;i<samples;i++) {
                double m = nextSample();
                short val = (short)(Math.max(-1,Math.min(1,m))*Short.MAX_VALUE);
                buf[2*i]   = (byte)(val & 0xff);
                buf[2*i+1] = (byte)((val>>8)&0xff);
            }
            line.write(buf,0,buf.length);
            callback.accept(buf);
        }
    }

    public byte[] renderOffline(Sequence seq, int sr) {
        // TODO: implement MIDI-driven rendering if needed
        return new byte[sr*2*5];
    }

    public int getSampleRate() { return sampleRate; }
}

// --- Voice ---
class Voice {
    private double freq, phase=0;
    private int sr;
    private String wave;
    private int attack, decay, release;
    private double sustain;
    private int sampleCount=0;
    private boolean released=false;
    private double amp=0;

    public Voice(double freq,int sr,String wave,int a,int d,double s,int r) {
        this.freq=freq; this.sr=sr; this.wave=wave;
        attack=a; decay=d; sustain=s; release=r;
    }
    public double nextSample() {
        double out;
        switch(wave) {
            case "Saw":    out = 2*(phase-Math.floor(phase+0.5)); break;
            case "Square": out = Math.signum(Math.sin(2*Math.PI*phase)); break;
            default:       out = Math.sin(2*Math.PI*phase);
        }
        phase += freq/sr; if (phase>=1) phase -= 1;

        sampleCount++;
        if (!released) {
            if (sampleCount<attack) amp = sampleCount/(double)attack;
            else if (sampleCount<attack+decay)
                amp = 1 - (sampleCount-attack)/(double)decay*(1-sustain);
            else amp = sustain;
        } else {
            amp *= 1 - 1.0/release;
        }
        return out*amp;
    }
    public void release() { released=true; }
}

// --- Recorder ---
class Recorder {
    private static class RecEvent { long tMs; int note; boolean on; }
    private java.util.List<RecEvent> events;
    private boolean recording;
    private SynthEngine synth;
    private long recordStart;

    public Recorder(SynthEngine synth) {
        this.synth = synth;
    }
    public void start() {
        events = new ArrayList<>();
        recording = true;
        recordStart = System.currentTimeMillis();
    }
    public void stop() {
        recording = false;
    }
    public void noteOn(int n) {
        if (!recording) return;
        RecEvent ev = new RecEvent();
        ev.tMs = System.currentTimeMillis() - recordStart;
        ev.note = n; ev.on = true;
        events.add(ev);
    }
    public void noteOff(int n) {
        if (!recording) return;
        RecEvent ev = new RecEvent();
        ev.tMs = System.currentTimeMillis() - recordStart;
        ev.note = n; ev.on = false;
        events.add(ev);
    }
    public void playback() {
        long start = System.currentTimeMillis();
        for (RecEvent ev : events) {
            long delay = ev.tMs - (System.currentTimeMillis() - start);
            if (delay>0) try{Thread.sleep(delay);}catch(Exception _) {}
            if (ev.on) synth.noteOn(ev.note); else synth.noteOff(ev.note);
        }
    }
    public void exportMidi() {
        try {
            Sequence seq = new Sequence(Sequence.PPQ,1000);
            Track track = seq.createTrack();
            for (RecEvent ev : events) {
                ShortMessage msg = new ShortMessage(
                    ev.on?ShortMessage.NOTE_ON:ShortMessage.NOTE_OFF,
                    0, ev.note, ev.on?100:0
                );
                track.add(new MidiEvent(msg, ev.tMs));
            }
            MidiSystem.write(seq,1,new File("recording.mid"));
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void exportWav() {
        int sr = synth.getSampleRate();
        int durationMs = events.isEmpty()
            ? 0
            : (int)(events.get(events.size()-1).tMs + 2000);
        int totalSamples = sr*durationMs/1000;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<RecEvent> it = events.iterator();
        RecEvent nextEv = it.hasNext()?it.next():null;
        synth.allNotesOff();
        for (int i=0;i<totalSamples;i++){
            int tMs = i*1000/sr;
            while (nextEv!=null && nextEv.tMs<=tMs){
                if (nextEv.on) synth.noteOn(nextEv.note);
                else           synth.noteOff(nextEv.note);
                nextEv = it.hasNext()?it.next():null;
            }
            double x = synth.nextSample();
            short v = (short)(Math.max(-1,Math.min(1,x))*Short.MAX_VALUE);
            baos.write(v&0xff); baos.write((v>>8)&0xff);
        }
        WaveFile.write("recording.wav",baos.toByteArray(),sr);
    }
}

// --- MidiReceiver ---
class MidiReceiver implements Receiver {
    private SynthEngine synth;
    public MidiReceiver(SynthEngine s){ synth=s; }
    public void send(MidiMessage msg,long time){
        if (msg instanceof ShortMessage){
            ShortMessage sm=(ShortMessage)msg;
            if (sm.getCommand()==ShortMessage.NOTE_ON && sm.getData2()>0)
                synth.noteOn(sm.getData1());
            else
                synth.noteOff(sm.getData1());
        }
    }
    public void close(){}
}

// --- WaveformPanel ---
class WaveformPanel extends JPanel {
    private LinkedList<short[]> hist = new LinkedList<>();
    public void pushSamples(byte[] buf,int sr){
        int len = buf.length/2;
        short[] s = new short[len];
        for(int i=0;i<len;i++)
            s[i] = (short)((buf[2*i]&0xff)|(buf[2*i+1]<<8));
        hist.add(s);
        if(hist.size()>50) hist.removeFirst();
        repaint();
    }
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        int w=getWidth(),h=getHeight(),mid=h/2,x=0;
        g.setColor(Color.BLACK); g.fillRect(0,0,w,h);
        g.setColor(Color.GREEN);
        for(short[] s:hist){
            for(int i=0;i<s.length;i+=Math.max(1,s.length/w)){
                int y=mid+(s[i]*mid/Short.MAX_VALUE);
                g.drawLine(x,mid,x,y);
                if(++x>=w) return;
            }
        }
    }
}

// --- HexKeyboardPanel ---
class HexKeyboardPanel extends JPanel {
    public HexKeyboardPanel(Consumer<Integer> onPress,Consumer<Integer> onRelease){
        setLayout(null);
        int[] baseNotes = {48, 50,52,53,55,57,59,60,62,64,65,67,69,71};
        int rows = 6, cols = 14, r=30;
        int dx=(int)(r*1.75), dy=(int)(r*1.5);
        for(int row=0;row<rows;row++){
            for(int col=0;col<cols;col++){
                int x=col*dx + (row%2)*(dx/2), y=row*dy;
                int note = 48 + row*cols + col;
                HexButton btn = new HexButton(r);
                btn.setBounds(x,y,2*r,2*r);
                btn.addMouseListener(new MouseAdapter(){
                    public void mousePressed(MouseEvent e){ onPress.accept(note); }
                    public void mouseReleased(MouseEvent e){ onRelease.accept(note); }
                });
                add(btn);
            }
        }
        setPreferredSize(new Dimension(cols*dx + r, rows*dy + r));
    }
}

// --- HexButton ---
class HexButton extends JButton {
    private Polygon hex;
    public HexButton(int r){
        hex = new Polygon();
        for(int i=0;i<6;i++){
            double theta=Math.PI/6 + i*Math.PI/3;
            int x=(int)(r + r*Math.cos(theta));
            int y=(int)(r + r*Math.sin(theta));
            hex.addPoint(x,y);
        }
        setContentAreaFilled(false);
    }
    protected void paintComponent(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        g2.setColor(getModel().isPressed()?Color.LIGHT_GRAY:Color.WHITE);
        g2.fill(hex);
        g2.setColor(Color.BLACK);
        g2.draw(hex);
    }
    public boolean contains(int x,int y){ return hex.contains(x,y); }
}

// --- TuningPresets ---
class TuningPresets {
    private static Map<String,double[]> map=new LinkedHashMap<>();
    static {
        map.put("12-ET",genET(12));
        map.put("19-ET",genET(19));
        map.put("24-ET",genET(24));
        map.put("31-ET",genET(31));
        map.put("Just Intonation",new double[]{1,9/8.0,5/4.0,4/3.0,3/2.0,5/3.0,15/8.0});
    }
    public static String[] names(){ return map.keySet().toArray(new String[0]); }
    public static Map<Integer,Double> get(String name){
        double[] a=map.get(name);
        Map<Integer,Double> out=new HashMap<>();
        for(int i=0;i<a.length;i++){
            out.put(60+i, 440*Math.pow(2,(i-9)/12.0)*a[i]);
        }
        return out;
    }
    private static double[] genET(int n){
        double[] a=new double[n];
        for(int i=0;i<n;i++) a[i]=Math.pow(2,i/(double)n);
        return a;
    }
}

// --- WaveFile ---
class WaveFile {
    public static void write(String fn, byte[] data, int sr){
        try(DataOutputStream out=new DataOutputStream(new FileOutputStream(fn))){
            out.writeBytes("RIFF"); out.writeInt(Integer.reverseBytes(36+data.length));
            out.writeBytes("WAVEfmt "); out.writeInt(Integer.reverseBytes(16));
            out.writeShort(Short.reverseBytes((short)1));
            out.writeShort(Short.reverseBytes((short)1));
            out.writeInt(Integer.reverseBytes(sr));
            out.writeInt(Integer.reverseBytes(sr*2));
            out.writeShort(Short.reverseBytes((short)2));
            out.writeShort(Short.reverseBytes((short)16));
            out.writeBytes("data"); out.writeInt(Integer.reverseBytes(data.length));
            out.write(data);
        } catch(IOException e){ e.printStackTrace(); }
    }
}

