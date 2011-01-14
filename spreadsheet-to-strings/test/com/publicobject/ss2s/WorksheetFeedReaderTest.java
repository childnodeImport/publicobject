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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.xmlpull.v1.XmlPullParserException;

public final class WorksheetFeedReaderTest extends TestCase {

  public void test() throws IOException, XmlPullParserException {
    String xml = "<?xml version='1.0' encoding='UTF-8' ?>" +
        "<feed xmlns='http://www.w3.org/2005/Atom' xmlns:openSearch='http://a9.com/-/spec/opensearchrss/1.0/' xmlns:gs='http://schemas.google.com/spreadsheets/2006'>\n" +
        "    <id>https://spreadsheets.google.com/feeds/worksheets/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/private/full</id>" +
        "    <updated>2011-01-14T04:55:04.463Z</updated>" +
        "    <category scheme='http://schemas.google.com/spreadsheets/2006' term='http://schemas.google.com/spreadsheets/2006#worksheet'/>" +
        "    <title type='text'>Shush!</title>" +
        "    <link rel='alternate' type='text/html' href='https://spreadsheets.google.com/ccc?key=0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc'/>" +
        "    <link rel='http://schemas.google.com/g/2005#feed' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/worksheets/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/private/full'/>" +
        "    <link rel='http://schemas.google.com/g/2005#post' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/worksheets/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/private/full'/>" +
        "    <link rel='self' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/worksheets/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/private/full'/>" +
        "    <author>" +
        "        <name>limpbizkit</name>" +
        "        <email>limpbizkit@gmail.com</email>" +
        "    </author>" +
        "    <openSearch:totalResults>1</openSearch:totalResults>" +
        "    <openSearch:startIndex>1</openSearch:startIndex>" +
        "    <entry>" +
        "        <id>https://spreadsheets.google.com/feeds/worksheets/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/private/full/od6</id>" +
        "        <updated>2011-01-14T04:54:50.857Z</updated>" +
        "        <category scheme='http://schemas.google.com/spreadsheets/2006' term='http://schemas.google.com/spreadsheets/2006#worksheet'/>" +
        "        <title type='text'>Sheet1</title>" +
        "        <content type='text'>Sheet1</content>" +
        "        <link rel='http://schemas.google.com/spreadsheets/2006#listfeed' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full'/>" +
        "        <link rel='http://schemas.google.com/spreadsheets/2006#cellsfeed' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/cells/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full'/>" +
        "        <link rel='http://schemas.google.com/visualization/2008#visualizationApi' type='application/atom+xml' href='https://spreadsheets.google.com/tq?key=0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc&amp;sheet=od6'/>" +
        "        <link rel='self' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/worksheets/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/private/full/od6'/>" +
        "        <link rel='edit' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/worksheets/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/private/full/od6/cr160b00mg'/>" +
        "        <gs:rowCount>100</gs:rowCount>" +
        "        <gs:colCount>20</gs:colCount>" +
        "    </entry>" +
        "</feed>";

    String listFeedUrl = "https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full";
    List<String> listFeedUrls = new WorksheetFeedReader().readListFeedUrls(
        new ByteArrayInputStream(xml.getBytes("UTF-8")));
    assertEquals(Arrays.asList(listFeedUrl), listFeedUrls);
  }
}
