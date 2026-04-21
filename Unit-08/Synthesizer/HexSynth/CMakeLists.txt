cmake_minimum_required(VERSION 3.22)
project(HexSynth VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── Locate JUCE ───────────────────────────────────────────────────────────────
# Option A: place a JUCE clone next to CMakeLists.txt  (git clone … HexSynth/JUCE)
# Option B: CMake will fetch it automatically the first time  (~500 MB download)
if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/JUCE/CMakeLists.txt")
    message(STATUS "Using local JUCE copy")
    add_subdirectory(JUCE)
else()
    message(STATUS "JUCE not found locally — fetching via FetchContent (first run is slow)")
    include(FetchContent)
    FetchContent_Declare(JUCE
        GIT_REPOSITORY https://github.com/juce-framework/JUCE.git
        GIT_TAG        7.0.9
        GIT_SHALLOW    TRUE
        GIT_PROGRESS   TRUE
    )
    FetchContent_MakeAvailable(JUCE)
endif()

# ── Plugin target ─────────────────────────────────────────────────────────────
juce_add_plugin(HexSynth
    VERSION                   1.0.0
    COMPANY_NAME              "CSC210"
    COMPANY_WEBSITE           "https://example.com"
    PLUGIN_MANUFACTURER_CODE  CSC2
    PLUGIN_CODE               HxSy
    FORMATS                   VST3 Standalone
    PRODUCT_NAME              "HexSynth"
    IS_SYNTH                  TRUE
    NEEDS_MIDI_INPUT          TRUE
    NEEDS_MIDI_OUTPUT         FALSE
    IS_MIDI_EFFECT            FALSE
    EDITOR_WANTS_KEYBOARD_FOCUS TRUE
    COPY_PLUGIN_AFTER_BUILD   FALSE
)

# Generates JuceHeader.h in the build tree and adds its directory to the
# target's include path.  Without this, #include <JuceHeader.h> cannot be found.
juce_generate_juce_header(HexSynth)

target_sources(HexSynth PRIVATE
    Source/PluginProcessor.cpp
    Source/PluginEditor.cpp
)

target_compile_definitions(HexSynth PUBLIC
    JUCE_WEB_BROWSER=0
    JUCE_USE_CURL=0
    JUCE_VST3_CAN_REPLACE_VST2=0
    JUCE_DISPLAY_SPLASH_SCREEN=0
)

target_link_libraries(HexSynth
    PRIVATE
        juce::juce_audio_utils
        juce::juce_dsp
    PUBLIC
        juce::juce_recommended_config_flags
        juce::juce_recommended_lto_flags
        juce::juce_recommended_warning_flags
)
