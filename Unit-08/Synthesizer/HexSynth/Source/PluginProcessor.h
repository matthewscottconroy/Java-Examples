#pragma once
#include <JuceHeader.h>

#include <array>
#include <set>
#include <vector>

//==============================================================================
// Voice — one polyphonic note with ADSR envelope
//==============================================================================
struct Voice
{
    double freq     = 440.0;
    double phase    = 0.0;
    double amp      = 0.0;
    long   sample   = 0;      // samples since note-on
    long   relStart = 0;      // sample index when note-off occurred
    double ampAtRel = 0.0;    // amplitude at release moment
    int    midiNote = -1;
    bool   active   = false;
    bool   released = false;

    void   noteOn  (int note, double hz)   noexcept;
    void   noteOff ()                      noexcept;

    /**
     * Advance oscillator + envelope by one sample.
     * waveform: 0=Sine  1=Saw  2=Square  3=Triangle
     * atkS/decS/relS are durations in *samples*.
     */
    double process (int waveform, double sr,
                    double atkS, double decS,
                    double sus,  double relS) noexcept;
};

//==============================================================================
// Mono delay line (circular buffer)
//==============================================================================
struct DelayLine
{
    std::vector<float> buf;
    int writePos = 0;

    void  prepare (int maxSamples);
    float tick    (float input, int delaySamples, float feedback) noexcept;
};

//==============================================================================
// HexSynthProcessor
//==============================================================================
class HexSynthProcessor : public juce::AudioProcessor
{
public:
    HexSynthProcessor();
    ~HexSynthProcessor() override = default;

    //==========================================================================
    void prepareToPlay (double sampleRate, int samplesPerBlock) override;
    void releaseResources() override;
    void processBlock  (juce::AudioBuffer<float>&, juce::MidiBuffer&) override;

    //==========================================================================
    juce::AudioProcessorEditor* createEditor() override;
    bool hasEditor()  const override { return true;  }

    const juce::String getName() const override { return "HexSynth"; }
    bool acceptsMidi()   const override { return true;  }
    bool producesMidi()  const override { return false; }
    bool isMidiEffect()  const override { return false; }
    double getTailLengthSeconds() const override { return 1.5; }

    int  getNumPrograms()    override { return 1; }
    int  getCurrentProgram() override { return 0; }
    void setCurrentProgram (int) override {}
    const juce::String getProgramName  (int) override { return {}; }
    void changeProgramName (int, const juce::String&) override {}

    void getStateInformation (juce::MemoryBlock& dest) override;
    void setStateInformation (const void* data, int size) override;

    //==========================================================================
    juce::AudioProcessorValueTreeState apvts;

    // Per-note frequency table (overwritten by setTuningPreset)
    std::array<double, 128> tuning {};
    /** 0=12-ET  1=19-ET  2=24-ET  3=31-ET  4=5-limit Just Intonation */
    void setTuningPreset (int preset);

    // Lock-free oscilloscope FIFO: audio thread writes, GUI timer reads
    juce::AbstractFifo  scopeFifo;
    std::vector<float>  scopeData;

    // Called from the editor's hex keyboard (runs on the message thread)
    void noteOn  (int midiNote, int velocity);
    void noteOff (int midiNote);

private:
    static constexpr int MAX_VOICES = 16;

    double sr = 44100.0;
    std::array<Voice, MAX_VOICES> voices {};
    DelayLine                     delayLine;

    juce::dsp::StateVariableTPTFilter<float> filter;

    static juce::AudioProcessorValueTreeState::ParameterLayout createParams();

    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR (HexSynthProcessor)
};
