#include "PluginProcessor.h"
#include "PluginEditor.h"

//==============================================================================
// Voice
//==============================================================================
void Voice::noteOn (int note, double hz) noexcept
{
    midiNote = note;
    freq     = hz;
    phase    = 0.0;
    sample   = 0;
    amp      = 0.0;
    released = false;
    active   = true;
}

void Voice::noteOff() noexcept
{
    if (active && !released) {
        released = true;
        relStart = sample;
        ampAtRel = amp;
    }
}

double Voice::process (int waveform, double sr,
                       double atkS, double decS,
                       double sus,  double relS) noexcept
{
    if (!active) return 0.0;

    // ── Oscillator ────────────────────────────────────────────────────────────
    double out;
    switch (waveform) {
        case 0: // Sine
            out = std::sin (juce::MathConstants<double>::twoPi * phase);
            break;
        case 1: // Sawtooth
            out = 2.0 * (phase - std::floor (phase + 0.5));
            break;
        case 2: // Square
            out = phase < 0.5 ? 1.0 : -1.0;
            break;
        default: // Triangle
            out = phase < 0.5 ? (4.0 * phase - 1.0)
                               : (3.0 - 4.0 * phase);
            break;
    }
    phase += freq / sr;
    if (phase >= 1.0) phase -= 1.0;

    // ── Envelope ──────────────────────────────────────────────────────────────
    if (!released) {
        ++sample;
        if (sample <= (long) atkS)
            amp = (atkS > 0) ? sample / atkS : 1.0;
        else if (sample <= (long)(atkS + decS))
            amp = 1.0 - ((sample - atkS) / decS) * (1.0 - sus);
        else
            amp = sus;
    } else {
        long rel = sample - relStart;
        if (rel >= (long) relS) { active = false; return 0.0; }
        amp = (relS > 0) ? ampAtRel * (1.0 - rel / relS) : 0.0;
        ++sample;
    }

    return out * amp;
}

//==============================================================================
// DelayLine
//==============================================================================
void DelayLine::prepare (int maxSamples)
{
    buf.assign (maxSamples, 0.f);
    writePos = 0;
}

float DelayLine::tick (float input, int delaySamples, float feedback) noexcept
{
    int sz = (int) buf.size();
    if (sz == 0) return input;
    delaySamples = juce::jlimit (1, sz - 1, delaySamples);

    int readPos   = (writePos - delaySamples + sz) % sz;
    float delayed = buf[readPos];
    buf[writePos] = input + delayed * feedback;
    writePos      = (writePos + 1) % sz;
    return delayed;
}

//==============================================================================
// Parameter layout
//==============================================================================
juce::AudioProcessorValueTreeState::ParameterLayout HexSynthProcessor::createParams()
{
    juce::AudioProcessorValueTreeState::ParameterLayout layout;

    layout.add (std::make_unique<juce::AudioParameterChoice> (
        "waveform", "Waveform",
        juce::StringArray { "Sine", "Saw", "Square", "Triangle" }, 0));

    // ADSR — milliseconds (skewed so the low end has more resolution)
    auto msRange = juce::NormalisableRange<float> (1.f, 5000.f, 1.f, 0.4f);
    layout.add (std::make_unique<juce::AudioParameterFloat> ("attack",  "Attack",  msRange, 100.f));
    layout.add (std::make_unique<juce::AudioParameterFloat> ("decay",   "Decay",   msRange, 200.f));
    layout.add (std::make_unique<juce::AudioParameterFloat> ("sustain", "Sustain", 0.f, 1.f, 0.8f));
    layout.add (std::make_unique<juce::AudioParameterFloat> ("release", "Release", msRange, 500.f));

    // Filter
    auto cutRange = juce::NormalisableRange<float> (20.f, 20000.f, 1.f, 0.25f);
    layout.add (std::make_unique<juce::AudioParameterFloat> ("cutoff",    "Cutoff",    cutRange, 18000.f));
    layout.add (std::make_unique<juce::AudioParameterFloat> ("resonance", "Resonance", 0.5f, 10.f, 0.7f));

    // Effects
    layout.add (std::make_unique<juce::AudioParameterFloat> ("delay_mix",  "Delay Mix",  0.f, 0.95f, 0.2f));
    layout.add (std::make_unique<juce::AudioParameterFloat> ("delay_time", "Delay Time", 0.01f, 1.0f, 0.3f));
    layout.add (std::make_unique<juce::AudioParameterFloat> ("distortion", "Distortion", 0.f, 1.f,   0.f));

    return layout;
}

//==============================================================================
// Constructor / destructor
//==============================================================================
HexSynthProcessor::HexSynthProcessor()
    : juce::AudioProcessor (
          BusesProperties().withOutput ("Output", juce::AudioChannelSet::stereo(), true)),
      apvts (*this, nullptr, "Parameters", createParams()),
      scopeFifo (8192)
{
    scopeData.assign (8192, 0.f);
    setTuningPreset (0); // 12-ET
}

//==============================================================================
// Prepare / release
//==============================================================================
void HexSynthProcessor::prepareToPlay (double sampleRate, int samplesPerBlock)
{
    sr = sampleRate;

    juce::dsp::ProcessSpec spec;
    spec.sampleRate       = sampleRate;
    spec.maximumBlockSize = (juce::uint32) samplesPerBlock;
    spec.numChannels      = (juce::uint32) getTotalNumOutputChannels();

    filter.prepare (spec);
    filter.setType  (juce::dsp::StateVariableTPTFilterType::lowpass);
    filter.reset();

    delayLine.prepare ((int)(sampleRate * 1.1)); // 1.1 s max delay
}

void HexSynthProcessor::releaseResources() {}

//==============================================================================
// Audio processing
//==============================================================================
void HexSynthProcessor::processBlock (juce::AudioBuffer<float>& buffer,
                                      juce::MidiBuffer& midiBuffer)
{
    juce::ScopedNoDenormals noDenormals;
    buffer.clear();

    // ── MIDI events ───────────────────────────────────────────────────────────
    for (const auto meta : midiBuffer) {
        const auto msg = meta.getMessage();
        if      (msg.isNoteOn()      && msg.getVelocity() > 0)
            noteOn  (msg.getNoteNumber(), msg.getVelocity());
        else if (msg.isNoteOff()     || msg.getVelocity() == 0)
            noteOff (msg.getNoteNumber());
        else if (msg.isAllNotesOff() || msg.isAllSoundOff())
            for (auto& v : voices) v.active = false;
    }

    // ── Read parameters (atomic loads are cheap, do once per block) ───────────
    const int   waveform  = (int) apvts.getRawParameterValue ("waveform") ->load();
    const float atkMs     =       apvts.getRawParameterValue ("attack")   ->load();
    const float decMs     =       apvts.getRawParameterValue ("decay")    ->load();
    const float sus       =       apvts.getRawParameterValue ("sustain")  ->load();
    const float relMs     =       apvts.getRawParameterValue ("release")  ->load();
    const float cutoff    =       apvts.getRawParameterValue ("cutoff")   ->load();
    const float resonance =       apvts.getRawParameterValue ("resonance")->load();
    const float delayMix  =       apvts.getRawParameterValue ("delay_mix") ->load();
    const float delayTime =       apvts.getRawParameterValue ("delay_time")->load();
    const float distAmt   =       apvts.getRawParameterValue ("distortion")->load();

    const double atkS = atkMs * sr / 1000.0;
    const double decS = decMs * sr / 1000.0;
    const double relS = relMs * sr / 1000.0;
    const int delaySamples = juce::jmax (1, (int)(delayTime * sr));

    filter.setCutoffFrequency (cutoff);
    filter.setResonance       (resonance);

    // ── Generate audio sample by sample ───────────────────────────────────────
    const int nSamples = buffer.getNumSamples();
    float* outL = buffer.getWritePointer (0);
    float* outR = buffer.getNumChannels() > 1 ? buffer.getWritePointer (1) : nullptr;

    // Scale down to avoid clipping with up to MAX_VOICES active
    constexpr double VOICE_SCALE = 0.12;

    for (int i = 0; i < nSamples; ++i) {
        double mix = 0.0;
        for (auto& v : voices)
            mix += v.process (waveform, sr, atkS, decS, (double) sus, relS);
        mix *= VOICE_SCALE;

        // Soft distortion (tanh waveshaping)
        float x = (float) std::tanh (mix * (1.0 + distAmt * 10.0));

        // Delay with feedback
        const float delayed = delayLine.tick (x, delaySamples, delayMix * 0.75f);
        x = x * (1.f - delayMix) + delayed * delayMix;

        x = juce::jlimit (-1.f, 1.f, x);
        outL[i] = x;
        if (outR) outR[i] = x;
    }

    // ── Low-pass filter over the whole block ─────────────────────────────────
    juce::dsp::AudioBlock<float>              block (buffer);
    juce::dsp::ProcessContextReplacing<float> ctx   (block);
    filter.process (ctx);

    // ── Feed oscilloscope FIFO ────────────────────────────────────────────────
    {
        int s1, n1, s2, n2;
        scopeFifo.prepareToWrite (nSamples, s1, n1, s2, n2);
        for (int i = 0; i < n1; ++i) scopeData[(size_t)(s1 + i)] = outL[i];
        for (int i = 0; i < n2; ++i) scopeData[(size_t)(s2 + i)] = outL[n1 + i];
        scopeFifo.finishedWrite (n1 + n2);
    }
}

//==============================================================================
// Note on / off (message-thread safe: no locking needed here because
// processBlock runs on the audio thread and notes are added atomically)
//==============================================================================
void HexSynthProcessor::noteOn (int midiNote, int velocity)
{
    if (velocity == 0) { noteOff (midiNote); return; }

    const double hz = tuning[(size_t) juce::jlimit (0, 127, midiNote)];

    // Retrigger existing voice for the same note
    for (auto& v : voices) {
        if (v.active && v.midiNote == midiNote) {
            v.noteOn (midiNote, hz);
            return;
        }
    }

    // Allocate a free voice
    for (auto& v : voices) {
        if (!v.active) { v.noteOn (midiNote, hz); return; }
    }

    // Voice steal: grab the longest-held voice
    Voice* oldest = nullptr;
    for (auto& v : voices)
        if (!oldest || v.sample > oldest->sample)
            oldest = &v;
    if (oldest) oldest->noteOn (midiNote, hz);
}

void HexSynthProcessor::noteOff (int midiNote)
{
    for (auto& v : voices)
        if (v.active && v.midiNote == midiNote)
            v.noteOff();
}

//==============================================================================
// Tuning presets
//==============================================================================
void HexSynthProcessor::setTuningPreset (int preset)
{
    switch (preset) {
        case 1: // 19-ET
            for (int n = 0; n < 128; ++n)
                tuning[(size_t) n] = 440.0 * std::pow (2.0, (n - 69) / 19.0);
            break;
        case 2: // 24-ET (quarter-tone)
            for (int n = 0; n < 128; ++n)
                tuning[(size_t) n] = 440.0 * std::pow (2.0, (n - 69) / 24.0);
            break;
        case 3: // 31-ET
            for (int n = 0; n < 128; ++n)
                tuning[(size_t) n] = 440.0 * std::pow (2.0, (n - 69) / 31.0);
            break;
        case 4: { // 5-limit Just Intonation
            // Ratios relative to unison, chromatic scale from C
            static constexpr double ji[12] = {
                1.0,        16.0/15.0,  9.0/8.0,   6.0/5.0,
                5.0/4.0,    4.0/3.0,   45.0/32.0,  3.0/2.0,
                8.0/5.0,    5.0/3.0,   9.0/5.0,   15.0/8.0
            };
            for (int n = 0; n < 128; ++n) {
                const int oct = (n - 69) / 12;
                const int pc  = ((n - 69) % 12 + 12) % 12;
                tuning[(size_t) n] = 440.0 * std::pow (2.0, oct) * ji[pc];
            }
            break;
        }
        default: // 0 = 12-ET (standard equal temperament)
            for (int n = 0; n < 128; ++n)
                tuning[(size_t) n] = 440.0 * std::pow (2.0, (n - 69) / 12.0);
            break;
    }
}

//==============================================================================
// State persistence
//==============================================================================
void HexSynthProcessor::getStateInformation (juce::MemoryBlock& dest)
{
    auto state = apvts.copyState();
    if (auto xml = state.createXml())
        copyXmlToBinary (*xml, dest);
}

void HexSynthProcessor::setStateInformation (const void* data, int size)
{
    if (auto xml = getXmlFromBinary (data, size))
        if (xml->hasTagName (apvts.state.getType()))
            apvts.replaceState (juce::ValueTree::fromXml (*xml));
}

//==============================================================================
// Editor factory — required whenever hasEditor() returns true
//==============================================================================
juce::AudioProcessorEditor* HexSynthProcessor::createEditor()
{
    return new HexSynthEditor (*this);
}

//==============================================================================
// Plugin entry point (required by JUCE)
//==============================================================================
juce::AudioProcessor* JUCE_CALLTYPE createPluginFilter()
{
    return new HexSynthProcessor();
}
