#pragma once
#include "PluginProcessor.h"
#include <deque>
#include <set>

//==============================================================================
// HexKeyboardComponent
// Draws a Wicki-Hayden hexagonal keyboard and fires note-on/off callbacks.
//==============================================================================
class HexKeyboardComponent : public juce::Component
{
public:
    std::function<void(int)> onNoteOn;
    std::function<void(int)> onNoteOff;

    HexKeyboardComponent();

    void paint   (juce::Graphics&)            override;
    void resized ()                            override;
    void mouseDown  (const juce::MouseEvent&) override;
    void mouseUp    (const juce::MouseEvent&) override;
    void mouseDrag  (const juce::MouseEvent&) override;

private:
    struct HexKey { int note; juce::Path path; };
    std::vector<HexKey> keys;
    std::set<int>       held;   // currently pressed notes

    void buildKeys();
    int  hitTest (juce::Point<float> pt) const; // returns note or -1

    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR (HexKeyboardComponent)
};

//==============================================================================
// OscilloscopeComponent
// Scrolling waveform display driven by a 30 Hz GUI timer.
//==============================================================================
class OscilloscopeComponent : public juce::Component,
                               private juce::Timer
{
public:
    explicit OscilloscopeComponent (HexSynthProcessor& p);

    void paint (juce::Graphics&) override;

private:
    HexSynthProcessor& proc;
    std::deque<float>  display;

    void timerCallback() override;

    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR (OscilloscopeComponent)
};

//==============================================================================
// HexSynthEditor
//==============================================================================
class HexSynthEditor : public juce::AudioProcessorEditor
{
public:
    explicit HexSynthEditor (HexSynthProcessor&);
    ~HexSynthEditor() override;

    void paint   (juce::Graphics&) override;
    void resized ()                 override;

private:
    HexSynthProcessor& proc;

    HexKeyboardComponent  hexKeyboard;
    OscilloscopeComponent oscilloscope;

    // Header combos
    juce::Label    waveformLabel, tuningLabel;
    juce::ComboBox waveformBox,   tuningBox;

    // Control knobs
    juce::Slider attackSlider,     decaySlider,    sustainSlider,  releaseSlider;
    juce::Slider cutoffSlider,     resonanceSlider;
    juce::Slider delayMixSlider,   delayTimeSlider, distortionSlider;

    juce::Label  attackLabel,     decayLabel,    sustainLabel,  releaseLabel;
    juce::Label  cutoffLabel,     resonanceLabel;
    juce::Label  delayMixLabel,   delayTimeLabel, distortionLabel;

    using SliderAttachment = juce::AudioProcessorValueTreeState::SliderAttachment;
    using ComboAttachment  = juce::AudioProcessorValueTreeState::ComboBoxAttachment;

    std::unique_ptr<SliderAttachment>
        attackAtt, decayAtt, sustainAtt, releaseAtt,
        cutoffAtt, resonanceAtt,
        delayMixAtt, delayTimeAtt, distortionAtt;

    std::unique_ptr<ComboAttachment> waveformAtt;

    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR (HexSynthEditor)
};
