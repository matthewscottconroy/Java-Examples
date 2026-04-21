# PianoSynthRecorder

A polyphonic software synthesizer with a **Wicki-Hayden isomorphic hexagonal
keyboard**, ADSR envelope, four waveforms, multiple tuning systems, real-time
waveform display, and export to MIDI and WAV files.  Single Java source file,
no build tool required.

---

## Requirements

- Java 11 or later

---

## Compile and run

```bash
javac PianoSynthRecorder.java
java PianoSynthRecorder
```

---

## Running the tests

The `TestRunner` class is compiled into the same file.  No audio hardware or
display is required — tests use offline rendering.

```bash
javac PianoSynthRecorder.java && java TestRunner
```

Expected output:

```
=== PianoSynthRecorder Tests ===
  PASS: SynthEngine: sine samples in [-1, 1]
  PASS: ADSR: attack ramps to 1, sustain holds, release decays
  PASS: TuningPresets: 12-ET A4 (MIDI 69) = 440.0 Hz
  PASS: TuningPresets: all 128 MIDI notes have positive frequencies
  PASS: Wicki-Hayden: note at row=0, col=0 is 36 (C2)
  PASS: WAV header: first 4 bytes are 'RIFF'

6 passed, 0 failed.
```

If the system audio device is unavailable (e.g. in a headless CI
environment), the first test is automatically skipped rather than
failed.

---

## The Wicki-Hayden isomorphic keyboard layout

An **isomorphic** keyboard is one where every interval always has the same
physical shape, regardless of the starting note (key).  This means a C major
chord and a D major chord are fingered identically — just shifted.

The **Wicki-Hayden** layout places notes on a hexagonal grid with two rules:

| Direction | Interval |
|-----------|----------|
| One column right | +2 semitones (a whole tone) |
| One row up | +7 semitones (a perfect fifth) |

This gives every note six neighbours that are musically meaningful (minor
second, major second, minor third, major third, perfect fourth, perfect
fifth).

In this implementation the grid starts at MIDI note 36 (C2) and spans
6 rows × 12 columns.  The formula for any button is:

```
note = 36 + row * 7 + col * 2
```

Odd rows are offset by half a button width to produce the staggered hex grid.

---

## ADSR controls

The **ADSR envelope** controls how the volume of each note evolves over time.

| Stage | Slider | Description |
|-------|--------|-------------|
| **A** ttack | Attack ms (1–2000) | Time to ramp from 0 to peak amplitude after key press |
| **D** ecay | Decay ms (1–2000) | Time to fall from peak down to the sustain level |
| **S** ustain | Sustain % (0–100) | Volume level held while the key is held |
| **R** elease | Release ms (1–3000) | Time to fade from sustain level to silence after key release |

Typical settings:

| Sound type | Attack | Decay | Sustain | Release |
|------------|--------|-------|---------|---------|
| Piano | 5 ms | 200 ms | 40% | 400 ms |
| Organ | 10 ms | 0 ms | 100% | 50 ms |
| Pad | 500 ms | 300 ms | 70% | 800 ms |
| Pluck | 2 ms | 150 ms | 0% | 200 ms |

---

## Available waveforms

| Waveform | Timbre | Harmonic content |
|----------|--------|-----------------|
| **Sine** | Pure, flute-like | Fundamental only |
| **Saw** | Bright, string/brass-like | All harmonics (1/n amplitude) |
| **Square** | Hollow, clarinet-like | Odd harmonics only |
| **Triangle** | Soft, mellow | Odd harmonics (1/n² amplitude) |

---

## Tuning presets

| Preset | Description |
|--------|-------------|
| **12-ET** | Standard 12-tone equal temperament; A4 = 440 Hz |
| **19-ET** | 19 equal divisions of the octave; slightly flatter semitones |
| **24-ET (quarter)** | Quarter-tone equal temperament; 24 notes per octave |
| **31-ET** | 31 equal divisions; close approximation of just intonation |
| **Just Intonation** | Pure integer ratios (5-limit); pure major thirds and fifths |
| **Pythagorean** | Built from stacked pure 3:2 fifths; pure fifths, wide thirds |

All presets map all 128 MIDI notes using the selected temperament's ratios
with octave transposition from C4 (MIDI 60) = 261.63 Hz.

---

## MIDI export

Click **Export MIDI** after recording.  A Standard MIDI File (Type 1) named
`recording.mid` is written to the working directory.  The timing resolution
is 1 ms per tick (PPQ = 1000).

---

## WAV export

Click **Export WAV** after recording.  The engine renders the recording
offline (no audio hardware needed for export) to a 16-bit mono PCM WAV file
named `recording.wav`.  The render includes a 2-second tail after the last
note for the release envelope to finish.

The WAV format is standard PCM with a 44-byte RIFF header.  All multi-byte
header fields are written in little-endian byte order as required by the WAV
specification.

---

## Effects

| Slider | Range | Description |
|--------|-------|-------------|
| Cutoff Hz | 100–22050 | One-pole low-pass filter cutoff frequency |
| Resonance % | 0–100 | Currently informational; not yet connected to filter Q |
| Reverb % | 0–100 | Wet/dry mix for the feedback delay reverb approximation |
| Distort % | 0–100 | Drive for tanh soft-clipping before the filter |
| Delay ms | 0–800 | Feedback delay time |

---

## Bugs fixed (vs. original)

Six bugs were identified and fixed:

1. **ADSR timing** — attack/decay/release durations were compared to a raw
   sample counter but stored in milliseconds.  Fixed: convert ms → samples
   in the `Voice` constructor (`ms * sampleRate / 1000`).

2. **Inaudible release tail** — `noteOff()` removed the voice from the active
   map before the release could play, causing immediate silence.  Fixed:
   released voices are kept in a separate `releasingVoices` list and pruned
   only when amplitude falls below 10⁻⁵.

3. **Frozen UI during playback** — `recorder.playback()` was called directly
   on the Swing EDT, blocking all repaints for the playback duration.  Fixed:
   wrapped in a `SwingWorker`.

4. **WAV little-endian confirmation** — `DataOutputStream.writeInt` is
   big-endian but WAV requires little-endian.  Confirmed that custom
   `writeLE32`/`writeLE16` helpers are used throughout; no bug in the
   current code.

5. **Tuning table incomplete** — `TuningPresets.get()` only mapped MIDI notes
   60 through 60+n.  Fixed: fills all 128 MIDI notes using octave-repeating
   ratios.

6. **Wicki-Hayden note layout** — buttons were placed at a fixed grid that
   did not follow the +7/+2 semitone Wicki-Hayden offsets, producing
   duplicate and skipped notes.  Fixed: `note = baseNote + row * 7 + col * 2`.
