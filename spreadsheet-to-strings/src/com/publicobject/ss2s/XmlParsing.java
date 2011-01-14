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

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class XmlParsing {
  private XmlParsing() {}

  /**
   * Skips the current tag recursively.
   */
  public static void skip(XmlPullParser in) throws IOException, XmlPullParserException {
    int depth = 1;
    while (depth > 0) {
      int type = in.next();
      if (type == XmlPullParser.START_TAG) {
        depth++;
      } else if (type == XmlPullParser.END_TAG) {
        depth--;
      }
    }
  }

  public static void assertEquals(int expected, int actual) throws XmlPullParserException {
    if (expected != actual) {
      throw new XmlPullParserException("Expected " + expected + " but was " + actual);
    }
  }

  public static void assertEquals(Object expected, Object actual) throws XmlPullParserException {
    if (!expected.equals(actual)) {
      throw new XmlPullParserException("Expected " + expected + " but was " + actual);
    }
  }
}
