/**
 * Copyright (C) 2012 Jesse Wilson
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
package com.publicobject.roundsweb;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Accept games as JSON and serve them as HTML.
 */
public final class GameServlet extends HttpServlet {
  private static final Pattern PATTERN = Pattern.compile("/games/(\\d+)");

  private final Gson gson = new GsonBuilder()
      .disableHtmlEscaping()
      .create();

  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    URL requestUrl = new URL(request.getRequestURL().toString());
    Matcher pathMatcher = PATTERN.matcher(requestUrl.getPath());
    if (!pathMatcher.matches()) {
      throw new UnsupportedOperationException("Unexpected URL: " + requestUrl);
    }
    long gameId = Long.parseLong(pathMatcher.group(1));

    Game game;
    try {
      game = getGame(gameId);
    } catch (EntityNotFoundException e) {
      throw new IllegalArgumentException("Unexpected game: " + gameId);
    }

    request.setAttribute("game", game);
    request.getRequestDispatcher("/WEB-INF/game.jspx").forward(request, response);
  }

  private Game getGame(long gameId) throws EntityNotFoundException {
    Key key = KeyFactory.createKey("Game", gameId);
    Entity entity = DatastoreServiceFactory.getDatastoreService().get(key);
    String json = (String) entity.getProperty("json");
    return gson.fromJson(json, Game.class);
  }
}