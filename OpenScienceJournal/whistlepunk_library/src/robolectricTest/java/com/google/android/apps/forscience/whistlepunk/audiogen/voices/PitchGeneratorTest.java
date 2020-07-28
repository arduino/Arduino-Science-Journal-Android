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

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for PitchGenerator */
@RunWith(RobolectricTestRunner.class)
public class PitchGeneratorTest {
  @Test
  public void testPitchGenerator_testPitchExtrema() {
    int scale[] = {0 /* C */, 2 /* D */, 4 /* E */, 7 /* G */, 9 /* A */}; // pentatonic scale

    int pitchMin = 71; // C5
    int pitchMax = 92; // A6
    int[] pitches = PitchGenerator.generatePitches(scale, pitchMin, pitchMax);

    int[] expected = {71, 73, 75, 78, 80, 83, 85, 87, 90, 92};
    assertArrayEquals(expected, pitches);
  }

  @Test
  public void testPitchGenerator_testPitchMaxNotInScale() {
    int scale[] = {0 /* C */, 2 /* D */, 4 /* E */, 7 /* G */, 9 /* A */}; // pentatonic scale

    int pitchMin = 71; // C5
    int pitchMax = 93; // A#6, note is not in scale.
    int[] pitches = PitchGenerator.generatePitches(scale, pitchMin, pitchMax);

    int[] expected = {71, 73, 75, 78, 80, 83, 85, 87, 90, 92};
    assertArrayEquals(expected, pitches);
  }
}
