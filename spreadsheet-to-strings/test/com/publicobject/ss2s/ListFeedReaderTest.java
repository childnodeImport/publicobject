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
import java.util.List;
import junit.framework.TestCase;
import org.xmlpull.v1.XmlPullParserException;

public final class ListFeedReaderTest extends TestCase {

  public void test() throws IOException, XmlPullParserException {
    String xml = "<?xml version='1.0' encoding='UTF-8' ?>\n" +
        "<feed xmlns='http://www.w3.org/2005/Atom' xmlns:openSearch='http://a9.com/-/spec/opensearchrss/1.0/' xmlns:gsx='http://schemas.google.com/spreadsheets/2006/extended'>" +
        "    <id>https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full</id>" +
        "    <updated>2011-01-14T04:55:04.463Z</updated>" +
        "    <category scheme='http://schemas.google.com/spreadsheets/2006' term='http://schemas.google.com/spreadsheets/2006#list'/>" +
        "    <title type='text'>Sheet1</title>" +
        "    <link rel='alternate' type='text/html' href='https://spreadsheets.google.com/ccc?key=0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc'/>" +
        "    <link rel='http://schemas.google.com/g/2005#feed' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full'/>" +
        "    <link rel='http://schemas.google.com/g/2005#post' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full'/>" +
        "    <link rel='self' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full'/>" +
        "    <author>" +
        "        <name>limpbizkit</name>" +
        "        <email>limpbizkit@gmail.com</email>" +
        "    </author>" +
        "    <openSearch:totalResults>3</openSearch:totalResults>" +
        "    <openSearch:startIndex>1</openSearch:startIndex>" +
        "    <entry>" +
        "        <id>https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full/cokwr</id>" +
        "        <updated>2011-01-14T04:55:04.463Z</updated>" +
        "        <category scheme='http://schemas.google.com/spreadsheets/2006' term='http://schemas.google.com/spreadsheets/2006#list'/>" +
        "        <title type='text'>title</title>" +
        "        <content type='text'>default: Shush! Ringer Restorer, fr: Shush! Ringur Restorare</content>" +
        "        <link rel='self' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full/cokwr'/>" +
        "        <link rel='edit' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full/cokwr/50m6gb0pjacg9'/>" +
        "        <gsx:_cn6ca>title</gsx:_cn6ca>" +
        "        <gsx:default>Shush! Ringer Restorer</gsx:default>" +
        "        <gsx:fr>Shush! Ringur Restorare</gsx:fr>" +
        "    </entry>" +
        "    <entry>" +
        "        <id>https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full/cre1l</id>" +
        "        <updated>2011-01-14T04:55:04.463Z</updated>" +
        "        <category scheme='http://schemas.google.com/spreadsheets/2006' term='http://schemas.google.com/spreadsheets/2006#list'/>" +
        "        <title type='text'>okay</title>" +
        "        <content type='text'>default: Sweet!, fr: C'est Bon!</content>" +
        "        <link rel='self' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full/cre1l'/>" +
        "        <link rel='edit' type='application/atom+xml' href='https://spreadsheets.google.com/feeds/list/0AqH4uBT50p0tdFJZNHNuTWxuc2NtTjRrZWlQelZWRXc/od6/private/full/cre1l/1ccbm4gdnl4od9'/>" +
        "        <gsx:_cn6ca>okay</gsx:_cn6ca>" +
        "        <gsx:default>Sweet!</gsx:default>" +
        "        <gsx:fr>C'est Bon!</gsx:fr>" +
        "    </entry>" +
        "</feed>";

    ListFeedReader listFeedReader = new ListFeedReader();
    List<Localization> localizations = listFeedReader.readLocalizations(
        new ByteArrayInputStream(xml.getBytes("UTF-8")));
    assertEquals(2, localizations.size());

    Localization en = localizations.get(0);
    Localization fr = localizations.get(1);

    assertEquals("default", en.getLanguageAndRegion());
    assertEquals("fr", fr.getLanguageAndRegion());
    assertEquals(2, en.size());
    assertEquals(2, fr.size());
    assertEquals("title", en.getName(0));
    assertEquals("title", fr.getName(0));
    assertEquals("Shush! Ringer Restorer", en.getValue(0));
    assertEquals("Shush! Ringur Restorare", fr.getValue(0));
    assertEquals("okay", en.getName(1));
    assertEquals("okay", fr.getName(1));
    assertEquals("Sweet!", en.getValue(1));
    assertEquals("C'est Bon!", fr.getValue(1));
  }
}
