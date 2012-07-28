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
import com.google.appengine.api.datastore.Key;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Accept games as JSON and serve them as HTML.
 */
public final class PostServlet extends HttpServlet {
  private final Gson gson = new GsonBuilder()
      .disableHtmlEscaping()
      .create();

  @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    URL requestUrl = new URL(request.getRequestURL().toString());

    Reader reader = new InputStreamReader(request.getInputStream(), "UTF-8");
    JsonReader jsonReader = new JsonReader(reader);
    Game game = gson.fromJson(jsonReader, Game.class);
    long gameId = storeGame(game);

    response.setContentType("text/plain");
    Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
    URL gameUrl = new URL(requestUrl, String.format("/%d", gameId));
    writer.write(gameUrl.toString());
    writer.close();
  }

  private long storeGame(Game game) {
    Entity entity = new Entity("Game");
    entity.setProperty("json", gson.toJson(game, Game.class));
    Key key = DatastoreServiceFactory.getDatastoreService().put(entity);
    return key.getId();
  }
}