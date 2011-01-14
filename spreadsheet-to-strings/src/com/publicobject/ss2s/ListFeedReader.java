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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Reads the XML of a Google Spreadsheets list-based feed into localization
 * data. The column headers should be language names like "en" and
 * "fr". The row headers should be string names like "applicationTitle" and
 * "welcomeMessage".
 *
 * <p>Example URL:
 * https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full
 *
 * <p>Example spreadsheet:
 * https://spreadsheets.google.com/ccc?key=tRY4snMlnscmN4keiPzVVEw
 */
public final class ListFeedReader {

  private static final String CELL_VALUE_PREFIX = "gsx:";

  public List<Localization> readLocalizations(InputStream xmlIn)
      throws XmlPullParserException, IOException {
    Map<String, Localization> result = new LinkedHashMap<String, Localization>();
    XmlPullParser in = XmlPullParserFactory.newInstance().newPullParser();
    in.setInput(xmlIn, null);

    XmlParsing.assertEquals(XmlPullParser.START_TAG, in.nextTag());
    XmlParsing.assertEquals("feed", in.getName());
    readFeed(in, result);
    xmlIn.close();

    return new ArrayList<Localization>(result.values());
  }

  /**
   * Reads a spreadsheet. Content is of this form:
   *
   * <feed>
   *   <id>...</id>
   *   <updated>...</updated>
   *   <entry>...</entry>
   *   <entry>...</entry>
   *   <entry>...</entry>
   * </feed>
   */
  private void readFeed(XmlPullParser in, Map<String, Localization> results)
      throws XmlPullParserException, IOException {
    while (in.nextTag() == XmlPullParser.START_TAG) {
      if ("entry".equals(in.getName())) {
        readEntry(in, results);
      } else {
        XmlParsing.skip(in);
      }
    }
  }

  /**
   * Reads a spreadsheet row, starting from row 2. Content is of this form:
   *
   * <entry>
   *   <id>...</id>
   *   <updated>...</updated>
   *   <gsx:_cn6ca>title</gsx:_cn6ca>
   *   <gsx:default>Shush! Ringer Restorer</gsx:default>
   *   <gsx:fr>Shush! Ringur Restorare</gsx:fr>
   * </entry>
   */
  private void readEntry(XmlPullParser in, Map<String, Localization> results)
      throws IOException, XmlPullParserException {
    String rowName = null;
    while (in.nextTag() == XmlPullParser.START_TAG) {
      String tagName = in.getName();
      if (tagName.startsWith(CELL_VALUE_PREFIX)) {
        String content = readCell(in);
        String languageAndRegion = tagName.substring(CELL_VALUE_PREFIX.length());
        if (rowName == null) {
          rowName = content;
        } else {
          getLocalization(results, languageAndRegion).add(rowName, content);
        }
      } else {
        XmlParsing.skip(in);
      }
    }
  }

  private Localization getLocalization(Map<String, Localization> map, String languageAndRegion) {
    Localization localization = map.get(languageAndRegion);
    if (localization == null) {
      localization = new Localization(languageAndRegion);
      map.put(languageAndRegion, localization);
    }
    return localization;
  }

  private String readCell(XmlPullParser in) throws IOException, XmlPullParserException {
    String result = "";
    int tag = in.next();
    if (tag == XmlPullParser.TEXT) {
      result = in.getText();
      tag = in.nextTag();
    }
    XmlParsing.assertEquals(XmlPullParser.END_TAG, tag);
    return result;
  }
}
