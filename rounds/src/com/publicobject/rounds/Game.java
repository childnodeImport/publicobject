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

package com.publicobject.rounds;

import java.util.ArrayList;
import java.util.List;

/**
 * All model for a game. Includes players, rounds scores and the currently
 * selected round.
 */
public final class Game implements Cloneable {
    public static Game SAMPLE = new Game();
    static {
        SAMPLE.addPlayer("Jesse", 0xff0000);
        SAMPLE.addPlayer("Jodie", 0x00ff00);
        SAMPLE.addPlayer("Mike", 0x0000ff);
        SAMPLE.addPlayer("Jono", 0xff00ff);
    }

    private String id;
    private long dateStarted;
    private int round = 0;
    private List<Player> players = new ArrayList<Player>();

    public Game() {
        setRound(0);
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public long getDateStarted() {
        return dateStarted;
    }
    public void setDateStarted(long dateStarted) {
        this.dateStarted = dateStarted;
    }
    public void addPlayer(String name, int color) {
        players.add(new Player(name, color));
        makeRoundsConsistent();
    }

    private void makeRoundsConsistent() {
        int longestHistory = 0;
        for (Player player : players) {
            longestHistory = Math.max(player.history.size(), longestHistory);
        }
        for (Player player : players) {
            for (int i = player.history.size(); i < longestHistory; i++) {
                player.history.add(0);
            }
        }
        setRound(longestHistory > 0 ? longestHistory - 1 : 0);
    }

    public void setRound(int round) {
        this.round = round;

        for (Player player : players) {
            while (player.history.size() <= round) {
                player.history.add(0);
            }
        }
    }

    public boolean hasNonZeroScore(int round) {
        for (Player player : players) {
            if (player.history.get(round) != 0) {
                return true;
            }
        }
        return false;
    }

    public void setPlayerScore(int player, int round, int value) {
        Player playerScore = players.get(player);
        int last = playerScore.history.set(round, value);
        playerScore.score = playerScore.score - last + value;
    }

    public int round() {
        return round;
    }

    public int playerCount() {
        return players.size();
    }

    public int playerColor(int player) {
        return players.get(player).color;
    }

    public String playerName(int player) {
        return players.get(player).name;
    }

    public int roundCount() {
        return players.get(0).history.size();
    }

    public int playerScore(int player, int round) {
        return players.get(player).history.get(round);
    }

    public int playerTotal(int player) {
        return players.get(player).score;
    }

    public int roundScore(int player) {
        return players.get(player).history.get(round);
    }

    @Override public Game clone() {
        try {
            Game cloned = (Game) super.clone();
            cloned.players = new ArrayList<Player>(players);
            for (int i = 0; i < cloned.players.size(); i++) {
                cloned.players.set(i, cloned.players.get(i).clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private static class Player implements Cloneable {
        private final String name;
        private final int color;
        private int score;
        private final List<Integer> history = new ArrayList<Integer>();

        private Player(String name, int color) {
            if (name == null) {
                throw new IllegalArgumentException();
            }
            this.name = name;
            this.color = color;
        }

        @Override public Player clone() {
            Player result = new Player(name, color);
            result.score = score;
            result.history.addAll(history);
            return result;
        }
    }
}
