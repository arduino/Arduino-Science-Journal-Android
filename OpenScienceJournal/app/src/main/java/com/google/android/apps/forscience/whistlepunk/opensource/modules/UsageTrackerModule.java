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

package com.google.android.apps.forscience.whistlepunk.opensource.modules;

import android.util.SparseArray;
import com.google.android.apps.forscience.whistlepunk.analytics.UsageTracker;
import dagger.Module;
import dagger.Provides;

/** Stub UsageTracker which does nothing. */
@Module
public class UsageTrackerModule {
  @Provides
  public UsageTracker provideUsageTracker() {
    return new UsageTracker() {
      @Override
      public void setOptOut(boolean optOut) {}

      @Override
      public void trackScreenView(String screenName) {}

      @Override
      public void trackEvent(String category, String action, String label, long value) {}

      @Override
      public void trackDimensionEvent(
          String category, String action, SparseArray<String> dimensions) {}
    };
  }
}
