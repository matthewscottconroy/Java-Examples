# Unit 08: Input, Image Processing, and Audio in Java

This unit explores four Java applications that demonstrate how to handle real-world I/O in Java: capturing mouse and keyboard input, manipulating image data at the pixel level, and synthesizing audio in real time. Each project is a standalone, runnable program built entirely with the Java standard library.

---

## Projects

### Bug Blaster 3000 — Exterminator Edition (`bugblaster/`)

An arcade-style game in which the player defends "Mom's Pie" from waves of invading insects using a variety of mouse-driven weapons.

**Focus: Mouse Input**

Bug Blaster is a comprehensive showcase of every Swing mouse event type in action. Each weapon maps to a different mouse interaction:

| Mouse Action | Weapon |
|---|---|
| Single left click | Spray (small radius, 1 damage) |
| Double left click | Stomp (large radius, 3 damage) |
| Left hold ≥ 400 ms, then release | Charge Blast (radius and damage scale with hold time) |
| Left drag | Drag Spray (continuous damage trail) |
| Right click | Place glue trap |
| Right drag on trap | Reposition trap |
| Mouse move | Fly AI dodges cursor proportionally to distance |

The game demonstrates all five Swing listener interfaces: `MouseListener` (click, press, release, enter, exit), `MouseMotionListener` (move, drag), and charge-timing via hold duration. The `GameController` class translates raw mouse events into game actions, keeping input-handling code decoupled from rendering and game logic.

The game loop runs at 60 fps using `javax.swing.Timer` on the Swing Event Dispatch Thread.

---

### Steganography (`Steganography/`)

A command-line tool that hides AES-encrypted messages (or arbitrary binary files) inside PNG images by modifying the least significant bits of pixel color channels. The resulting image is visually indistinguishable from the original.

**Focus: Image Processing**

This project demonstrates how images are represented in Java as grids of 32-bit ARGB pixel values and how individual bits within those values can be manipulated programmatically.

Key image processing concepts covered:
- Reading and writing PNG files with `ImageIO` and `BufferedImage`
- Accessing and modifying individual pixel values with `getRGB` / `setRGB`
- Encoding data into pixel channels by flipping LSBs (3 bits per pixel across R, G, B channels)
- Traversing pixels in a deterministic but unpredictable order using a Fisher-Yates shuffle seeded from the passphrase hash — this spreads the payload across the image and makes it harder to detect

The message is AES-128-CBC encrypted before embedding, with a SHA-256-derived key and a random 16-byte IV stored as part of the payload. A CRC32 checksum validates correct decryption.

Usage:
```
java Steganography encode input.png output.png "secret message" passphrase
java Steganography decode output.png passphrase
java Steganography info image.png   # show embedding capacity
```

---

### PianoSynthRecorder (`Synthesizer/`)

A polyphonic software synthesizer featuring a Wicki-Hayden isomorphic hexagonal keyboard, ADSR envelopes, four waveforms, multiple tuning systems, real-time oscilloscope display, and export to MIDI and WAV.

**Focus: Audio Processing**

This project demonstrates how to generate, process, and output audio entirely in software using `javax.sound.sampled`.

Key audio concepts covered:
- Synthesizing raw PCM samples in real time from mathematical waveforms (sine, sawtooth, square, triangle)
- ADSR (Attack, Decay, Sustain, Release) envelope shaping for natural note onsets and tails
- Mixing multiple simultaneous voices (polyphony) by summing their sample streams
- Audio effects: one-pole low-pass filter, feedback delay loop (reverb approximation), soft-clipping via `tanh`
- Streaming samples to a `SourceDataLine` from a dedicated daemon thread so the UI stays responsive
- Offline rendering: generating a complete WAV file without requiring audio hardware, by running the synthesis engine headlessly
- MIDI export: writing a Standard MIDI File (Type 1) capturing timestamped note-on/off events

The synthesizer supports six tuning systems (12-tone equal temperament, 19-ET, 24-ET, 31-ET, Just Intonation, Pythagorean) mapped across all 128 MIDI note numbers.

The Wicki-Hayden keyboard arranges pitches on a hexagonal grid where every interval has the same physical shape regardless of the starting note: moving right raises pitch by a whole tone (+2 semitones), moving up raises pitch by a perfect fifth (+7 semitones).

---

### Wizard Rogue (`wizardrogue/`)

A real-time ASCII roguelike in which a wizard explores procedurally generated dungeons, fights escalating enemy hordes across five floors, and casts spells using fighting-game-style key sequences.

**Focus: Keyboard Input**

Wizard Rogue explicitly demonstrates three distinct approaches to keyboard input in a single application, each suited to a different interaction pattern:

| Technique | Used For | How It Works |
|---|---|---|
| Key-state tracking | WASD movement | A `Set<Integer>` of currently held key codes is updated in `keyPressed` / `keyReleased`; each game tick checks the set for smooth, fluid movement independent of OS key-repeat rate |
| Sequence detection with timing window | Spell casting | An `InputBuffer` records each keypress with a `System.currentTimeMillis()` timestamp; spells are recognized when a matching sequence (e.g. Q E Q for Fulgur) is completed within a 2.5-second window |
| Immediate single-key dispatch | Pickup, stairs, pause | Direct `switch` in `keyPressed` for actions that should fire exactly once per press (Space to pick up items, comma to descend stairs, Escape to pause) |

This design makes the code a practical reference for choosing the right input strategy: polling a key-state set for real-time continuous actions, timestamp-windowed sequence matching for combo inputs, and direct event dispatch for discrete one-shot actions.

Other Java features demonstrated include procedural dungeon generation (BSP room placement with L-shaped corridor connections), Bresenham line-of-sight with three-tier fog of war (hidden, explored-dim, fully visible), and a simple game AI with four behavioral states (wander, alerted, pursue, attack).

The game loop runs at 100 ms ticks using `javax.swing.Timer` on the EDT.

---

## Java Concepts Summary

| Project | Primary Java Feature | Key Classes / APIs |
|---|---|---|
| Bug Blaster | Mouse input | `MouseListener`, `MouseMotionListener`, `MouseEvent` |
| Steganography | Image processing | `BufferedImage`, `ImageIO`, `getRGB` / `setRGB` |
| PianoSynthRecorder | Audio synthesis | `SourceDataLine`, `AudioFormat`, `javax.sound.midi` |
| Wizard Rogue | Keyboard input | `KeyListener`, `KeyEvent`, `Set<Integer>`, timestamped input buffer |
