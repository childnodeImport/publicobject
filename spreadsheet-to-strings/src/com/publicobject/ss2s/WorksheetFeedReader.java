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
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Reads the XML of a Google Spreadsheets worksheet-based feed into the URLs
 * of the list-based feeds of the sheet's worksheets.
 *
 * <p>Example URL:
 * https://spreadsheets.google.com/feeds/worksheets/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/private/full
 */
public final class WorksheetFeedReader {

  private static final String LIST_FEED_REL
      = "http://schemas.google.com/spreadsheets/2006#listfeed";

  public List<String> readListFeedUrls(InputStream xmlIn)
      throws XmlPullParserException, IOException {
    List<String> result = new ArrayList<String>();
    XmlPullParser in = XmlPullParserFactory.newInstance().newPullParser();
    in.setInput(xmlIn, null);

    XmlParsing.assertEquals(XmlPullParser.START_TAG, in.nextTag());
    XmlParsing.assertEquals("feed", in.getName());
    readFeed(in, result);
    xmlIn.close();

    return result;
  }

  private void readFeed(XmlPullParser in, List<String> results)
      throws XmlPullParserException, IOException {
    while (in.nextTag() == XmlPullParser.START_TAG) {
      if ("entry".equals(in.getName())) {
        readEntry(in, results);
      } else {
        XmlParsing.skip(in);
      }
    }
  }

  private void readEntry(XmlPullParser in, List<String> results)
      throws IOException, XmlPullParserException {
    while (in.nextTag() == XmlPullParser.START_TAG) {
      if ("link".equals(in.getName())
          && LIST_FEED_REL.equals(in.getAttributeValue(null, "rel"))) {
        results.add(in.getAttributeValue(null, "href"));
      }
      XmlParsing.skip(in);
    }
  }
}
