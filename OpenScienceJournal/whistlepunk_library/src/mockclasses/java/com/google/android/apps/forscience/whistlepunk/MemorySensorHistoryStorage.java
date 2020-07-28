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

package com.google.android.apps.forscience.whistlepunk;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

public class MemorySensorHistoryStorage implements SensorHistoryStorage {
  List<String> stored = new ArrayList<>();

  @Override
  public List<String> getMostRecentSensorIds() {
    return Lists.newArrayList(stored);
  }

  @Override
  public void setMostRecentSensorIds(List<String> ids) {
    stored.clear();
    stored.addAll(ids);
  }
}
