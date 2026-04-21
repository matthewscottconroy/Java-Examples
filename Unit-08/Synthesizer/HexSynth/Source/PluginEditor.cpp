#include "PluginEditor.h"

// ── Palette ───────────────────────────────────────────────────────────────────
namespace Palette {
    const juce::Colour bg          { 0xff12121f };
    const juce::Colour headerBg    { 0xff1a1a2e };
    const juce::Colour keyNormal   { 0xfff0f0f0 };
    const juce::Colour keyPressed  { 0xff4fc3f7 };
    const juce::Colour keyBorder   { 0xff333355 };
    const juce::Colour scopeBg     { 0xff0a0a14 };
    const juce::Colour scopeGrid   { 0xff1a2a1a };
    const juce::Colour scopeTrace  { 0xff00e676 };
    const juce::Colour knobFill    { 0xff1565c0 };
    const juce::Colour knobThumb   { 0xff4fc3f7 };
    const juce::Colour labelText   { 0xffb0bec5 };
}

//==============================================================================
// HexKeyboardComponent
//==============================================================================
HexKeyboardComponent::HexKeyboardComponent()
{
    setOpaque (true);
}

void HexKeyboardComponent::paint (juce::Graphics& g)
{
    g.fillAll (Palette::headerBg);

    for (const auto& key : keys) {
        const bool isHeld = held.count (key.note) > 0;

        g.setColour (isHeld ? Palette::keyPressed : Palette::keyNormal);
        g.fillPath  (key.path);

        g.setColour (Palette::keyBorder);
        g.strokePath (key.path, juce::PathStrokeType (1.2f));

        // Note name (e.g. "C4")
        g.setColour (isHeld ? juce::Colours::white : juce::Colours::darkgrey);
        g.setFont   (juce::Font (8.5f, juce::Font::bold));
        g.drawText  (juce::MidiMessage::getMidiNoteName (key.note, true, false, 4),
                     key.path.getBounds().toNearestInt(),
                     juce::Justification::centred, false);
    }
}

void HexKeyboardComponent::resized() { buildKeys(); }

void HexKeyboardComponent::mouseDown (const juce::MouseEvent& e)
{
    if (int n = hitTest (e.position); n >= 0) {
        held.insert (n);
        if (onNoteOn) onNoteOn (n);
        repaint();
    }
}

void HexKeyboardComponent::mouseUp (const juce::MouseEvent&)
{
    for (int n : held)
        if (onNoteOff) onNoteOff (n);
    held.clear();
    repaint();
}

void HexKeyboardComponent::mouseDrag (const juce::MouseEvent& e)
{
    int n = hitTest (e.position);
    if (n >= 0 && held.count (n) == 0) {
        // Glide: release old notes, press new one
        for (int old : held)
            if (onNoteOff) onNoteOff (old);
        held.clear();
        held.insert (n);
        if (onNoteOn) onNoteOn (n);
        repaint();
    }
}

int HexKeyboardComponent::hitTest (juce::Point<float> pt) const
{
    for (const auto& key : keys)
        if (key.path.contains (pt))
            return key.note;
    return -1;
}

void HexKeyboardComponent::buildKeys()
{
    keys.clear();

    const int   cols     = 14;
    const int   rows     = 5;
    const float w        = (float) getWidth();
    const float h        = (float) getHeight();
    const float r        = std::min (w / (cols * 1.85f + 0.5f),
                                     h / (rows * 1.55f + 0.2f));
    const float dx       = r * 1.75f;   // horizontal spacing
    const float dy       = r * 1.5f;    // vertical spacing

    // Wicki-Hayden isomorphic layout:
    //   moving right one column  = +2 semitones (whole step)
    //   moving up one row        = +7 semitones (perfect 5th)
    //   odd rows offset right by dx/2
    const int baseNote = 36; // C2

    for (int row = 0; row < rows; ++row) {
        for (int col = 0; col < cols; ++col) {
            const int note = baseNote + row * 7 + col * 2;
            if (note > 127 || note < 0) continue;

            // cx: column + half-step offset for odd rows
            // cy: inverted so low notes are at the bottom
            const float cx = col * dx + (row & 1) * (dx * 0.5f) + r;
            const float cy = (rows - 1 - row) * dy + r;

            juce::Path hex;
            for (int i = 0; i < 6; ++i) {
                const float angle = juce::MathConstants<float>::pi / 6.f
                                    + i * juce::MathConstants<float>::pi / 3.f;
                const float px = cx + r * 0.88f * std::cos (angle);
                const float py = cy + r * 0.88f * std::sin (angle);
                if (i == 0) hex.startNewSubPath (px, py);
                else        hex.lineTo           (px, py);
            }
            hex.closeSubPath();
            keys.push_back ({ note, std::move (hex) });
        }
    }
}

//==============================================================================
// OscilloscopeComponent
//==============================================================================
OscilloscopeComponent::OscilloscopeComponent (HexSynthProcessor& p)
    : proc (p)
{
    startTimerHz (30);
}

void OscilloscopeComponent::paint (juce::Graphics& g)
{
    const int w = getWidth(), h = getHeight();
    g.fillAll (Palette::scopeBg);

    // Grid lines
    g.setColour (Palette::scopeGrid);
    g.drawHorizontalLine (h / 2, 0.f, (float) w);
    g.drawHorizontalLine (h / 4, 0.f, (float) w);
    g.drawHorizontalLine (3 * h / 4, 0.f, (float) w);

    if (display.size() < 2) return;

    const int   nPts  = (int) display.size();
    const float xStep = (float) w / (nPts - 1);

    juce::Path wave;
    for (int i = 0; i < nPts; ++i) {
        const float x = i * xStep;
        const float y = h * 0.5f - display[(size_t) i] * h * 0.45f;
        if (i == 0) wave.startNewSubPath (x, y);
        else        wave.lineTo           (x, y);
    }

    g.setColour (Palette::scopeTrace);
    g.strokePath (wave, juce::PathStrokeType (1.5f,
        juce::PathStrokeType::curved, juce::PathStrokeType::rounded));
}

void OscilloscopeComponent::timerCallback()
{
    const int avail = proc.scopeFifo.getNumReady();
    if (avail <= 0) return;

    int s1, n1, s2, n2;
    proc.scopeFifo.prepareToRead (avail, s1, n1, s2, n2);
    for (int i = 0; i < n1; ++i) display.push_back (proc.scopeData[(size_t)(s1 + i)]);
    for (int i = 0; i < n2; ++i) display.push_back (proc.scopeData[(size_t)(s2 + i)]);
    proc.scopeFifo.finishedRead (n1 + n2);

    const int maxSamples = std::max (256, getWidth());
    while ((int) display.size() > maxSamples)
        display.pop_front();

    repaint();
}

//==============================================================================
// Helpers
//==============================================================================
namespace {

void styleKnob (juce::Slider& s, juce::Label& l, const char* name)
{
    s.setSliderStyle (juce::Slider::RotaryHorizontalVerticalDrag);
    s.setTextBoxStyle (juce::Slider::TextBoxBelow, false, 56, 14);
    s.setColour (juce::Slider::rotarySliderFillColourId, Palette::knobFill);
    s.setColour (juce::Slider::thumbColourId,            Palette::knobThumb);
    s.setColour (juce::Slider::rotarySliderOutlineColourId, Palette::knobFill.darker());
    s.setColour (juce::Slider::textBoxTextColourId,      Palette::labelText);
    s.setColour (juce::Slider::textBoxOutlineColourId,   juce::Colours::transparentBlack);

    l.setText (name, juce::dontSendNotification);
    l.setJustificationType (juce::Justification::centred);
    l.setFont  (juce::Font (9.5f));
    l.setColour (juce::Label::textColourId, Palette::labelText);
}

void placeKnob (juce::Slider& s, juce::Label& l, juce::Rectangle<int> r)
{
    l.setBounds (r.removeFromTop (15));
    s.setBounds (r);
}

} // namespace

//==============================================================================
// HexSynthEditor
//==============================================================================
HexSynthEditor::HexSynthEditor (HexSynthProcessor& p)
    : juce::AudioProcessorEditor (&p),
      proc (p),
      oscilloscope (p)
{
    // ── Header labels ──────────────────────────────────────────────────────
    for (auto* lbl : { &waveformLabel, &tuningLabel }) {
        lbl->setFont  (juce::Font (10.f));
        lbl->setColour (juce::Label::textColourId, Palette::labelText);
        addAndMakeVisible (lbl);
    }
    waveformLabel.setText ("Waveform", juce::dontSendNotification);
    tuningLabel  .setText ("Tuning",   juce::dontSendNotification);

    // ── Waveform combo (APVTS-attached) ────────────────────────────────────
    waveformBox.addItemList ({ "Sine", "Saw", "Square", "Triangle" }, 1);
    waveformAtt = std::make_unique<ComboAttachment> (proc.apvts, "waveform", waveformBox);
    addAndMakeVisible (waveformBox);

    // ── Tuning combo (runtime, not a VST parameter) ─────────────────────────
    tuningBox.addItemList ({ "12-ET", "19-ET", "24-ET", "31-ET", "Just Intonation" }, 1);
    tuningBox.setSelectedId (1, juce::dontSendNotification);
    tuningBox.onChange = [this] {
        proc.setTuningPreset (tuningBox.getSelectedItemIndex());
    };
    addAndMakeVisible (tuningBox);

    // ── Knobs ─────────────────────────────────────────────────────────────
    styleKnob (attackSlider,     attackLabel,     "Attack");
    styleKnob (decaySlider,      decayLabel,      "Decay");
    styleKnob (sustainSlider,    sustainLabel,    "Sustain");
    styleKnob (releaseSlider,    releaseLabel,    "Release");
    styleKnob (cutoffSlider,     cutoffLabel,     "Cutoff");
    styleKnob (resonanceSlider,  resonanceLabel,  "Resonance");
    styleKnob (delayMixSlider,   delayMixLabel,   "Delay Mix");
    styleKnob (delayTimeSlider,  delayTimeLabel,  "Delay Time");
    styleKnob (distortionSlider, distortionLabel, "Distortion");

    // ── APVTS attachments ─────────────────────────────────────────────────
    auto& av = proc.apvts;
    attackAtt     = std::make_unique<SliderAttachment> (av, "attack",     attackSlider);
    decayAtt      = std::make_unique<SliderAttachment> (av, "decay",      decaySlider);
    sustainAtt    = std::make_unique<SliderAttachment> (av, "sustain",    sustainSlider);
    releaseAtt    = std::make_unique<SliderAttachment> (av, "release",    releaseSlider);
    cutoffAtt     = std::make_unique<SliderAttachment> (av, "cutoff",     cutoffSlider);
    resonanceAtt  = std::make_unique<SliderAttachment> (av, "resonance",  resonanceSlider);
    delayMixAtt   = std::make_unique<SliderAttachment> (av, "delay_mix",  delayMixSlider);
    delayTimeAtt  = std::make_unique<SliderAttachment> (av, "delay_time", delayTimeSlider);
    distortionAtt = std::make_unique<SliderAttachment> (av, "distortion", distortionSlider);

    for (auto* s : { &attackSlider, &decaySlider, &sustainSlider, &releaseSlider,
                     &cutoffSlider, &resonanceSlider,
                     &delayMixSlider, &delayTimeSlider, &distortionSlider })
        addAndMakeVisible (s);

    for (auto* l : { &attackLabel, &decayLabel, &sustainLabel, &releaseLabel,
                     &cutoffLabel, &resonanceLabel,
                     &delayMixLabel, &delayTimeLabel, &distortionLabel })
        addAndMakeVisible (l);

    // ── Hex keyboard ──────────────────────────────────────────────────────
    hexKeyboard.onNoteOn  = [this] (int n) { proc.noteOn  (n, 100); };
    hexKeyboard.onNoteOff = [this] (int n) { proc.noteOff (n);      };
    addAndMakeVisible (hexKeyboard);

    // ── Oscilloscope ──────────────────────────────────────────────────────
    addAndMakeVisible (oscilloscope);

    setSize (1060, 700);
}

HexSynthEditor::~HexSynthEditor() = default;

void HexSynthEditor::paint (juce::Graphics& g)
{
    g.fillAll (Palette::bg);
}

void HexSynthEditor::resized()
{
    auto area = getLocalBounds().reduced (4);

    // ── Header (waveform + tuning combos) ─────────────────────────────────
    {
        auto header = area.removeFromTop (32);
        header.removeFromLeft (4);
        waveformLabel.setBounds (header.removeFromLeft (58));
        waveformBox  .setBounds (header.removeFromLeft (100).reduced (2, 3));
        header.removeFromLeft (16);
        tuningLabel  .setBounds (header.removeFromLeft (46));
        tuningBox    .setBounds (header.removeFromLeft (130).reduced (2, 3));
    }

    // ── Right panel: 9 knobs in a 3×3 grid ───────────────────────────────
    {
        auto right = area.removeFromRight (210).reduced (4, 0);
        const int kW = right.getWidth() / 3;
        const int kH = right.getHeight() / 3;

        auto row0 = right.removeFromTop (kH);
        placeKnob (attackSlider,    attackLabel,    row0.removeFromLeft (kW));
        placeKnob (decaySlider,     decayLabel,     row0.removeFromLeft (kW));
        placeKnob (sustainSlider,   sustainLabel,   row0);

        auto row1 = right.removeFromTop (kH);
        placeKnob (releaseSlider,   releaseLabel,   row1.removeFromLeft (kW));
        placeKnob (cutoffSlider,    cutoffLabel,    row1.removeFromLeft (kW));
        placeKnob (resonanceSlider, resonanceLabel, row1);

        placeKnob (delayMixSlider,   delayMixLabel,   right.removeFromLeft (kW));
        placeKnob (delayTimeSlider,  delayTimeLabel,  right.removeFromLeft (kW));
        placeKnob (distortionSlider, distortionLabel, right);
    }

    // ── Oscilloscope strip at the bottom ──────────────────────────────────
    oscilloscope.setBounds (area.removeFromBottom (85));

    // ── Hex keyboard fills the remaining space ────────────────────────────
    hexKeyboard.setBounds (area);
}
