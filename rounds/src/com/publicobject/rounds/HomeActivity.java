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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

public final class HomeActivity extends Activity {
    private GameDatabase database;
    private ListView gameList;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = GameDatabase.getInstance(getApplicationContext());

        setContentView(R.layout.home);
        gameList = (ListView) findViewById(R.id.gameList);
        gameList.setItemsCanFocus(true);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Rounds");
    }

    @Override public void onResume() {
        super.onResume();
        gameList.setAdapter(new GameListAdapter(database.allGames()));
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        database = null;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.newGame:
            Intent newGameIntent = new Intent(this, SetUpActivity.class);
            newGameIntent.putExtra(IntentExtras.IS_NEW_GAME, true);
            startActivity(newGameIntent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void launchGame(Game game) {
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        intent.putExtra(IntentExtras.GAME_ID, game.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    private class GameListAdapter extends BaseAdapter {
        private final List<Game> games;

        private final View.OnClickListener resumeListener = new View.OnClickListener() {
            @Override public void onClick(View view) {
                int position = gameList.getPositionForView(view);
                Game game = (Game) gameList.getAdapter().getItem(position);
                launchGame(game);
            }
        };

        private final View.OnClickListener rematchListener = new View.OnClickListener() {
            @Override public void onClick(View button) {
                int position = gameList.getPositionForView((View) button.getParent());
                Game game = (Game) gameList.getAdapter().getItem(position);
                Game replay = game.replay();
                database.save(replay);
                launchGame(replay);
            }
        };

        private GameListAdapter(List<Game> games) {
            this.games = games;
        }

        @Override public int getCount() {
            return games.size() + 1;
        }

        @Override public Object getItem(int i) {
            if (i < 1) {
                return null;
            }
            return games.get(i - 1);
        }

        @Override public long getItemId(int i) {
            if (i < 1) {
                return i;
            }
            return games.get(i - 1).getDateStarted();
        }

        @Override public View getView(int position, View recycle, ViewGroup parent) {
            if (position == 0) {
                return getOverview(recycle, parent);
            }
            return getGame(position, recycle, parent);
        }

        @Override public int getViewTypeCount() {
            return 2; // header, games
        }

        @Override public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        private View getOverview(View recycle, ViewGroup parent) {
            return (recycle == null)
                    ? getLayoutInflater().inflate(R.layout.overview_item, parent, false)
                    : recycle;
        }

        private View getGame(int position, View recycle, ViewGroup parent) {
            LinearLayout layout = (LinearLayout) ((recycle == null)
                    ? getLayoutInflater().inflate(R.layout.game_item, parent, false)
                    : recycle);
            TextView players = (TextView) layout.findViewById(R.id.players);
            TextView summary = (TextView) layout.findViewById(R.id.summary);
            Button rematch = (Button) layout.findViewById(R.id.rematch);
            Game game = (Game) getItem(position);
            int maxTotal = game.maxTotal();

            SpannableStringBuilder ssb = new SpannableStringBuilder();
            for (int p = 0, size = game.playerCount(); p < size; p++) {
                if (p > 0) {
                    ssb.append("  ");
                }
                String name = game.playerName(p);
                String total = Integer.toString(game.playerTotal(p));
                ssb.append(name);
                ssb.append("\u00a0");

                ssb.append(total);
                ssb.setSpan(new ForegroundColorSpan(game.playerColor(p)),
                        ssb.length() - total.length(), ssb.length(), 0);
                if (maxTotal == game.playerTotal(p)) {
                    ssb.setSpan(new StyleSpan(Typeface.BOLD),
                            ssb.length() - total.length(), ssb.length(), 0);
                }
            }
            players.setText(ssb);

            StringBuilder rounds = new StringBuilder();
            if (game.roundCount() == 1) {
                rounds.append("1 round. ");
            } else {
                rounds.append(Integer.toString(game.roundCount()));
                rounds.append(" rounds. ");
            }
            rounds.append("Started ");
            rounds.append(DateUtils.getRelativeTimeSpanString(getApplicationContext(),
                    game.getDateStarted(), true));
            summary.setText(rounds);

            rematch.setOnClickListener(rematchListener);
            layout.setOnClickListener(resumeListener);
            layout.setFocusable(true);
            layout.setBackgroundResource(android.R.drawable.list_selector_background);
            return layout;
        }
    }
}