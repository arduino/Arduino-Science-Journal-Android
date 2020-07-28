/*
 *  Copyright 2016 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.android.apps.forscience.whistlepunk.audiogen.voices;

import com.google.android.apps.forscience.whistlepunk.audiogen.JsynUnitVoiceAdapter;
import com.jsyn.Synthesizer;
import com.softsynth.shared.time.TimeStamp;

/**
 * Adapt the SimpleJsynUnitVoice to the SimpleJsynAudioGenerator using pitch variation.
 *
 * <p>Adapt the SimpleJsynUnitVoice to the SimpleJsynAudioGenerator. This implementation maps the
 * data from the range (min-max) linearly to the pitch range FREQ_MIN-FREQ_MAX.
 */
public class DefaultVoice extends JsynUnitVoiceAdapter {

  public DefaultVoice(Synthesizer synth) {
    voice = new SimpleJsynUnitVoice();
    synth.add(voice);
  }

  public void noteOn(double value, double min, double max, TimeStamp timeStamp) {
    // Range checking, in case min or max is higher or lower than value (respectively).
    if (value < min) value = min;
    if (value > max) value = max;

    double freq = (value - min) / (max - min) * (FREQ_MAX - FREQ_MIN) + FREQ_MIN;
    voice.noteOn(freq, AMP_VALUE, timeStamp);
  }
}
