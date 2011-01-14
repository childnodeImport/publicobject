/**
 * Copyright (C) 2011 Jesse Wilson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicobject.ss2s;

import java.util.ArrayList;
import java.util.List;

public final class Localization {

  /**
   * Like 'fr' or "fr-rCA".
   * http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources
   */
  private final String languageAndRegion;
  private final List<String> names = new ArrayList<String>();
  private final List<String> values = new ArrayList<String>();

  public Localization(String languageAndRegion) {
    this.languageAndRegion = languageAndRegion;
  }

  public void add(String name, String value) {
    this.names.add(name);
    this.values.add(value);
  }

  public String getLanguageAndRegion() {
    return languageAndRegion;
  }

  public int size() {
    return names.size();
  }

  public String getName(int index) {
    return names.get(index);
  }

  public String getValue(int index) {
    return values.get(index);
  }
}
