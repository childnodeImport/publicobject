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

import java.io.IOException;
import java.net.URL;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the form submission.
 */
public final class ShowXmlServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String spreadsheetUrl1 = request.getParameter("spreadsheetUrl");
    URL spreadsheetUrl = new URL(spreadsheetUrl1);
    String key = getParameterFromQuery(spreadsheetUrl, "key");
    response.sendRedirect("/Key/" + key);
  }

  private String getParameterFromQuery(URL url, String parameterName) {
    String query = url.getQuery();
    int queryStart = query.indexOf(parameterName + "=");
    if (queryStart == -1) {
      return null;
    }
    queryStart += parameterName.length() + 1;
    int queryEnd = query.indexOf("&", queryStart);
    if (queryEnd == -1) {
      queryEnd = query.length();
    }
    return query.substring(queryStart, queryEnd);
  }
}