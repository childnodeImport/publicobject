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

package com.publicobject.ss2s.web;

import com.publicobject.ss2s.ListFeedReader;
import com.publicobject.ss2s.Localization;
import com.publicobject.ss2s.LocalizationToStrings;
import com.publicobject.ss2s.WorksheetFeedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Takes a spreadsheet key and prints the corresponding XML files.
 */
public final class KeyServlet extends HttpServlet {

  private static final String ACCESS_TOKEN_SESSION_KEY = "token";

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String token = checkAccessGranted(request);
      if (token == null) {
        System.out.println("Redirecting!");
        requestAccess(request, response);
        return;
      }

      String key = request.getPathInfo().substring(1);
      System.out.println("Using key=" + key);
      List<Localization> localizations = keyToLocalizations(key, token);

      response.setContentType("text/plain");
      Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
      writer.write("" + localizations.size() + " localizations\n");
      for (Localization localization : localizations) {
        writer.write("--------------------------------------------------------------------------------\n");
        writer.write("values-" + localization.getLanguageAndRegion() + ".xml\n\n");
        new LocalizationToStrings().localizationToStrings(localization, writer);
        writer.write("\n\n");
      }
      System.out.println("Success!");
    } catch (XmlPullParserException e) {
      throw new RuntimeException(e);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private List<Localization> keyToLocalizations(String key, String token) throws IOException, XmlPullParserException {
    String urlString = "https://spreadsheets.google.com/feeds/worksheets/" + key + "/private/full";
    System.out.println("worksheet url: " + urlString);
    URL url = new URL(urlString);
    HttpURLConnection worksheetConnection = (HttpURLConnection) url.openConnection();
    authorizeConnection(token, worksheetConnection);

    List<String> feedUrls = new WorksheetFeedReader()
        .readListFeedUrls(worksheetConnection.getInputStream());
    worksheetConnection.disconnect();

    if (feedUrls.size() <= 0) {
      throw new RuntimeException("Spreadsheet contains no worksheets!");
    }

    String feedUrl = feedUrls.get(0);
    System.out.println("feed url: " + feedUrl);
    HttpURLConnection listFeedConnection = (HttpURLConnection) new URL(feedUrl).openConnection();
    authorizeConnection(token, listFeedConnection);
    List<Localization> localizations = new ListFeedReader()
        .readLocalizations(listFeedConnection.getInputStream());
    listFeedConnection.disconnect();
    return localizations;
  }

  private void requestAccess(HttpServletRequest request, HttpServletResponse response)
      throws IOException, URISyntaxException {
    String requestUri = request.getRequestURL().toString();
    String query = "scope=https://spreadsheets.google.com/feeds/"
        + "&session=1"
        + "&secure=0"
        + "&next=" + requestUri;
    URI redirect = new URI("https", "www.google.com", "/accounts/AuthSubRequest", query, null);
    response.sendRedirect(redirect.toString());
  }

  public String checkAccessGranted(HttpServletRequest request)
      throws IOException, URISyntaxException {
    String sessionToken = (String) request.getSession().getAttribute(ACCESS_TOKEN_SESSION_KEY);
    if (sessionToken == null) {
      String oneUseToken = request.getParameter("token");
      if (oneUseToken == null) {
        return null;
      }
      System.out.println("Received one use token: " + oneUseToken);
      sessionToken = upgradeToken(oneUseToken);
      System.out.println("Received session token: " + sessionToken);
      request.getSession().setAttribute(ACCESS_TOKEN_SESSION_KEY, sessionToken);
    }
    return sessionToken;
  }

  /**
   * Upgrades a one time use token to a session token by making a request to
   * Google's servers.
   */
  private String upgradeToken(String oneUseToken) throws URISyntaxException, IOException {
    URL url = new URI("https", "www.google.com", "/accounts/AuthSubSessionToken", null).toURL();
    URLConnection connection = url.openConnection();
    authorizeConnection(oneUseToken, connection);
    connection.connect();

    Properties properties = new Properties();
    InputStream in = connection.getInputStream();
    properties.load(in);
    in.close();

    return properties.getProperty("Token");
  }

  private void authorizeConnection(String token, URLConnection connection) {
    connection.addRequestProperty("Authorization", "AuthSub token=\"" + token + "\"");
  }
}