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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public final class Game {
  public String name;
  public long lastSaved;
  public long dateStarted;
  public int round;
  public Player[] players;
  public String winCondition;

  public String getName() {
    return name;
  }

  public long getLastSaved() {
    return lastSaved;
  }

  public String getDateStarted() {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.US)
        .format(new Date(dateStarted));
  }

  public int getRound() {
    return round;
  }

  public Player[] getPlayers() {
    return players;
  }

  public String getWinCondition() {
    return winCondition;
  }

  public static class Player {
    public String name;
    public int color;
    public int total;
    public int[] history;

    public String getName() {
      return name;
    }

    public String getColor() {
      return String.format("#%06x", color & 0xffffff);
    }

    public int getTotal() {
      return total;
    }

    public int[] getHistory() {
      return history;
    }
  }
}
