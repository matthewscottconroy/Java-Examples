# Digital Sound Synthesis: From Physics to Samples

## What Is Sound?

Sound is pressure waves in air — oscillating compressions and rarefactions
traveling outward from a vibrating source. Your eardrum is a thin membrane
that moves in response to these pressure variations. Your auditory system
interprets the motion as sound.

A loudspeaker works in reverse: an electrical signal drives a cone back and
forth, creating the same pressure variations in air. To produce a given sound,
a computer needs to produce the right electrical signal — a sequence of numbers
representing the air pressure at successive points in time.

---

## Sampling and the Nyquist-Shannon Theorem

Digital audio represents sound as a sequence of discrete **samples** — one
measurement of air pressure, stored as a number, taken at regular intervals.

The **sample rate** determines how many samples are taken per second. CD audio
uses 44,100 samples per second (44.1 kHz). This choice is not arbitrary —
it is determined by the **Nyquist-Shannon sampling theorem**:

> To perfectly represent a signal containing frequencies up to F Hz, you must
> sample at a rate of at least 2F samples per second.

Human hearing extends to roughly 20 kHz. So a sample rate of 40 kHz would
theoretically suffice — 44.1 kHz was chosen as a comfortable margin above this.

The implication: any frequency above half the sample rate (the **Nyquist
frequency**) cannot be represented. If such a frequency is present in the
analog signal being recorded, it will be *aliased* — misinterpreted as a lower
frequency, producing audible distortion. Professional audio equipment includes
**anti-aliasing filters** before the analog-to-digital converter precisely to
prevent this.

---

## Waveforms and Timbre

A tuning fork produces a nearly pure **sine wave** — a single frequency, a
pure tone. Most real instruments produce **complex periodic waveforms**: a
fundamental frequency plus **overtones** (integer multiples of the fundamental),
each with its own amplitude.

The relative amplitudes of the overtones determine the **timbre** — the
characteristic "color" of a sound. Middle C on a piano and middle C on a
clarinet have the same fundamental frequency (≈262 Hz) but completely different
overtone structures, which is why they sound different.

**Fourier's theorem** states that any periodic waveform can be decomposed into
a sum of sine waves:
```
f(t) = A₁sin(2πf₀t) + A₂sin(2·2πf₀t) + A₃sin(3·2πf₀t) + …
```

This decomposition is the **Fourier series**. The inverse transform (synthesis)
reconstructs a sound by summing the right sine waves with the right amplitudes.
A digital synthesizer does exactly this — it generates and sums periodic
waveforms to create the desired timbre.

The four waveforms in the synthesizer:

**Sine**: only the fundamental, no overtones. Pure, flute-like. The simplest
possible waveform.

**Saw(tooth)**: all harmonics, each at 1/n amplitude:
```
f(t) = sin(f₀t) + ½sin(2f₀t) + ⅓sin(3f₀t) + ¼sin(4f₀t) + …
```
This sum converges to a triangular wave-like shape with sharp edges. Bright,
string-like or brass-like timbre.

**Square**: only odd harmonics, each at 1/n amplitude:
```
f(t) = sin(f₀t) + ⅓sin(3f₀t) + ⅕sin(5f₀t) + …
```
Hollow, nasal sound. Clarinets have a similar overtone structure (odd harmonics
dominate because they are cylindrical bore instruments closed at one end).

**Triangle**: only odd harmonics, at 1/n² amplitude. Much softer falloff —
sounds mellow and flute-like, but with slightly more warmth than pure sine.

---

## The ADSR Envelope

A piano note has a distinctive shape over time: it attacks instantly to full
volume, then gradually decays, sustains at a lower level while the key is held,
and releases when the key is released. A violin bows in slowly, sustains at
full volume, then fades. A plucked string attacks, decays immediately, then is
silent.

This shape is the **amplitude envelope**. The ADSR (Attack-Decay-Sustain-Release)
model approximates it with four parameters:

```
Amplitude
   │     /\
 1 │    /  \
   │   /    \___________
 S │  /                 \
   │ /                   \
 0 └──────────────────────── Time
   |Attack|Decay|Sustain  |Release|
```

- **Attack**: time to ramp from silence to peak amplitude (after key press)
- **Decay**: time to fall from peak to sustain level
- **Sustain**: amplitude held while key is held (a level, not a time)
- **Release**: time to fade from sustain level to silence (after key release)

Every synthesized sound has an ADSR envelope. The envelope is what makes a
synthesized piano sound like a piano rather than an organ — it is the temporal
shape of the sound, not just its frequency content.

---

## Tuning Systems

What frequencies should musical notes have? The answer has varied across
cultures and centuries, and the mathematics of tuning is surprisingly deep.

**Equal temperament (12-ET)**: the octave is divided into 12 equal-ratio steps.
Each semitone multiplies the frequency by 2^(1/12) ≈ 1.0595. An octave is
exactly a 2:1 frequency ratio; all other intervals are irrational numbers. This
is the standard Western tuning — it makes all keys equally in-tune (or equally
slightly out-of-tune), which is why a piano can play in any key.

**Just intonation**: intervals are pure integer ratios (5-limit just intonation):
- Perfect fifth: 3:2 (702 cents, vs. 700 in 12-ET)
- Major third: 5:4 (386 cents, vs. 400 in 12-ET)
- Minor third: 6:5 (316 cents, vs. 300 in 12-ET)

Just intonation makes individual chords sound extraordinarily pure — the
overtones align and the sound "locks in." The price is that transposing to a
different key requires different ratios; a fixed instrument cannot be
justly-tuned in all keys simultaneously. This was the central problem of
Western music theory for 300 years.

**Pythagorean tuning**: all intervals are built from stacked 3:2 perfect
fifths. Fifths are pure; major thirds are too wide (81:64 instead of 5:4).

**19-ET, 31-ET**: alternative equal temperaments that approximate just
intonation more closely than 12-ET for certain intervals. They include
microtones — intervals smaller than a semitone.

---

## The Isomorphic Keyboard

Standard piano keyboards are not isomorphic: the fingering pattern for a
C major chord differs from the pattern for a D major chord. This makes music
theory harder to visualize and certain techniques harder to transfer between
keys.

The **Wicki-Hayden** layout solves this. Every note is a hexagon. The interval
rules are:
- One step right: +2 semitones (whole tone)
- One step up: +7 semitones (perfect fifth)

The result: every major chord, every minor chord, every scale has the *same
shape* regardless of starting note. Transposition is physical translation — move
your hand to the right for a higher key, up-right for a fifth higher. The same
pattern of finger positions works in all 12 keys.

This makes the Wicki-Hayden layout a physical demonstration of **group theory
in music**: the symmetry group of transpositions acts uniformly on the note grid.

---

## Why This Matters in Computing

Digital audio is the clearest everyday application of several core CS and
mathematics concepts:

**Sampling theory** (Nyquist-Shannon) is foundational to all digital signal
processing — not just audio, but video, medical imaging, telecommunications,
and sensor data.

**Fourier analysis** underlies audio compression (MP3, AAC), image compression
(JPEG), data transmission, and large parts of scientific computing. The FFT
(Fast Fourier Transform) is one of the most important algorithms ever invented.

**Real-time systems**: a synthesizer must produce exactly 44,100 samples per
second without interruption. This is a real-time constraint — missing a deadline
causes an audible glitch. The Java audio system, the operating system scheduler,
and the hardware all interact to meet this constraint.

**Floating-point arithmetic**: the ADSR envelope, the filter cutoff, the
waveform samples — all require precise floating-point computation. Errors
accumulate over millions of samples; numerical stability matters.

---

## What to Notice in the Synthesizer

- Switch between **Sine**, **Saw**, **Square**, and **Triangle** at the same
  pitch. The frequency (pitch) is the same; the timbre (quality) is different.
  This is Fourier analysis made audible.
- Set **Attack** to 500 ms and **Release** to 1000 ms. Press a key briefly.
  The note takes half a second to reach full volume and one second to fade. This
  is the ADSR envelope controlling the amplitude over time.
- Compare **12-ET** and **Just Intonation** while playing a major chord (e.g.,
  C-E-G). Just intonation sounds "locked in" and resonant; 12-ET sounds
  slightly brighter and more tense. Then transpose the chord upward — in just
  intonation, each key requires different ratios; the pure sound disappears.
- On the **Wicki-Hayden keyboard**, play a C major chord, memorize the shape,
  then play a D major chord with the same shape, shifted two hexagons right.
  It works because the layout is isomorphic.
- Record a melody and **Export WAV**. Open the WAV file in an audio editor.
  Zoom in to see individual samples — the waveform is just a sequence of numbers
  that your speaker converts to air pressure.
