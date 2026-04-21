# HexSynth — VST3 / Standalone Plugin

HexSynth is a polyphonic synthesizer built with [JUCE](https://juce.com/).  
It uses a **Wicki-Hayden hexagonal keyboard** layout (every interval is the same finger shape regardless of key), supports multiple waveforms and tuning systems, and exposes a full ADSR+filter+effects signal chain as automatable VST3 parameters.

---

## Requirements

| Tool | Minimum version |
|------|----------------|
| CMake | 3.22 |
| C++ compiler | C++17 (GCC 11+, Clang 14+, MSVC 2019+) |
| JUCE | 7.0.9 (fetched automatically if not present) |
| Linux extra | `libasound2-dev` (ALSA), `libx11-dev`, `libxinerama-dev`, `libxrandr-dev`, `libxcursor-dev`, `libfreetype-dev`, `libwebkit2gtk-4.0-dev` |

Install Linux dependencies on Ubuntu/Debian:
```bash
sudo apt install libasound2-dev libx11-dev libxinerama-dev libxrandr-dev \
                 libxcursor-dev libfreetype-dev libwebkit2gtk-4.0-dev \
                 cmake build-essential git
```

---

## Building

All commands run from inside the `HexSynth/` directory.

### First-time setup (fetches JUCE automatically, ~500 MB)

```bash
cd HexSynth
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --parallel
```

### Faster subsequent builds (JUCE already downloaded)

```bash
cmake --build build --parallel
```

### Build only the VST3

```bash
cmake --build build --target HexSynth_VST3 --parallel
```

### Build only the Standalone app

```bash
cmake --build build --target HexSynth_Standalone --parallel
```

### Optional: use a local JUCE clone instead of fetching

```bash
git clone --depth 1 --branch 7.0.9 https://github.com/juce-framework/JUCE.git HexSynth/JUCE
cmake -B build -DCMAKE_BUILD_TYPE=Release   # will detect JUCE/ automatically
cmake --build build --parallel
```

---

## Output artifacts

After a successful build:

| Artifact | Path |
|----------|------|
| VST3 plugin | `build/HexSynth_artefacts/Release/VST3/HexSynth.vst3/` |
| Standalone app | `build/HexSynth_artefacts/Release/Standalone/HexSynth` |

### Running the standalone app

```bash
./build/HexSynth_artefacts/Release/Standalone/HexSynth
```

### Installing the VST3 (Linux)

Copy the entire `.vst3` bundle to your DAW's VST3 search path:

```bash
# Per-user install (recommended)
mkdir -p ~/.vst3
cp -r build/HexSynth_artefacts/Release/VST3/HexSynth.vst3 ~/.vst3/

# System-wide install
sudo cp -r build/HexSynth_artefacts/Release/VST3/HexSynth.vst3 /usr/lib/vst3/
```

Then rescan plugins in your DAW (Ardour, Reaper, Bitwig, LMMS, etc.).

---

## Clean build

```bash
rm -rf build
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --parallel
```

---

## Plugin parameters

All parameters are automatable in a DAW and saved with the project via JUCE's `AudioProcessorValueTreeState`.

### Waveform
| Value | Shape |
|-------|-------|
| Sine | Pure sinusoid |
| Saw | Sawtooth (bright, rich harmonics) |
| Square | Square wave (hollow, odd harmonics) |
| Triangle | Triangle (mellow, soft odd harmonics) |

### ADSR envelope
| Parameter | Range | Default | Description |
|-----------|-------|---------|-------------|
| Attack | 1–5000 ms | 100 ms | Time to reach full amplitude after note-on |
| Decay | 1–5000 ms | 200 ms | Time to fall from peak to sustain level |
| Sustain | 0.0–1.0 | 0.8 | Amplitude held while key is pressed |
| Release | 1–5000 ms | 500 ms | Fade-out time after note-off |

The sliders use a power-law (`skew=0.4`) so the low end (short times) has fine resolution.

### Filter
| Parameter | Range | Default | Description |
|-----------|-------|---------|-------------|
| Cutoff | 20–20000 Hz | 18000 Hz | Low-pass filter cutoff frequency |
| Resonance | 0.5–10.0 | 0.7 | Filter resonance (Q factor) |

The filter is a TPT (Topology-Preserving Transform) State Variable Filter — zero-delay feedback, no cramping at high cutoffs.

### Effects
| Parameter | Range | Default | Description |
|-----------|-------|---------|-------------|
| Delay Mix | 0.0–0.95 | 0.2 | Wet/dry ratio for the delay |
| Delay Time | 10–1000 ms | 300 ms | Delay time (max 1.1 s buffer) |
| Distortion | 0.0–1.0 | 0.0 | Tanh soft-clipping drive |

---

## Tuning presets

| Index | Name | Description |
|-------|------|-------------|
| 0 | 12-ET | Standard 12-tone equal temperament (A4 = 440 Hz) |
| 1 | 19-ET | 19-tone equal temperament — smoother thirds |
| 2 | 24-ET | Quarter-tone (24-ET) — microtonal half-steps |
| 3 | 31-ET | 31-tone ET — excellent approximation of 5-limit JI |
| 4 | 5-limit JI | 5-limit just intonation — pure octaves, fifths, thirds |

All 128 MIDI notes are mapped for every preset; octave repetition is computed from the preset's ratio structure so no notes are left at 12-ET by accident.

---

## Keyboard layout

HexSynth uses the **Wicki-Hayden** isomorphic layout. Every interval looks the same regardless of starting note:

```
Row offset per column: +2 semitones
Row offset per row:    +7 semitones (perfect fifth)

Starting note: MIDI 48 (C3) at row=0, col=0
```

Because the layout is isomorphic, a C major scale fingering is identical to a D major scale fingering — just shifted one column to the right.

---

## Voice architecture

- **16 voices** (polyphony). Voice stealing takes the longest-held voice.
- Each voice runs its own oscillator + per-voice ADSR entirely in the audio thread (`processBlock`).
- Voices in release phase continue until their amplitude falls below the release-end threshold — no abrupt cutoffs.
- The oscilloscope FIFO (`scopeFifo` / `scopeData`) is lock-free; the GUI timer reads from it at 30 Hz without blocking the audio thread.

---

## Source files

```
HexSynth/
├── CMakeLists.txt          Build configuration
└── Source/
    ├── PluginProcessor.h   Voice, DelayLine, HexSynthProcessor declarations
    ├── PluginProcessor.cpp Voice DSP, parameter layout, MIDI handling, tuning
    ├── PluginEditor.h      HexKeyboardComponent, OscilloscopeComponent, HexSynthEditor
    └── PluginEditor.cpp    GUI layout and controls
```

---

## Troubleshooting

### CMake can't find JUCE
Make sure you have internet access on the first build (FetchContent downloads ~500 MB). Alternatively clone JUCE manually into `HexSynth/JUCE/` as shown above.

### Linux: `fatal error: alsa/asoundlib.h: No such file`
```bash
sudo apt install libasound2-dev
```

### Linux: `fatal error: X11/Xlib.h: No such file`
```bash
sudo apt install libx11-dev libxinerama-dev libxrandr-dev libxcursor-dev
```

### Linux: `fatal error: freetype2/ft2build.h: No such file`
```bash
sudo apt install libfreetype-dev
```

### Plugin not appearing in DAW
1. Confirm the `.vst3` bundle was copied to the correct path (see Installation above).
2. The bundle must contain `Contents/x86_64-linux/HexSynth.so` — verify with `ls ~/.vst3/HexSynth.vst3/Contents/x86_64-linux/`.
3. Rescan plugins in your DAW's plugin manager.
4. Some DAWs require the plugin to declare a specific category — HexSynth declares `IS_SYNTH TRUE` and `NEEDS_MIDI_INPUT TRUE`.

### Standalone: no audio output
Open the audio settings dialog (usually accessible via the hamburger menu in the standalone app) and select your audio device and sample rate.
