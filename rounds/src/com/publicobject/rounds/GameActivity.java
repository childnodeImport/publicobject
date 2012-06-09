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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;

public final class GameActivity extends Activity {
    public static final String EXTRA_GAME = "game";

    private static final long PERIODIC_SAVE_PERIOD = TimeUnit.SECONDS.toMillis(30);

    private Game game;
    private PowerManager.WakeLock wakeLock;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean paused = true;
    private GameDatabase database;

    private JogWheel jogWheel;
    private ScoreHistoryTable scoreHistoryTable;
    private TextView labelTextView;
    private TextView valueTextView;

    private ActionBarBackground actionBarBackground;
    private ActionBar actionBar;
    private ImageButton nextRound;
    private TextView roundTextView;
    private ImageButton previousRound;

    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        database = GameDatabase.getInstance(getApplicationContext());

        Intent intent = getIntent();
        String gameJson = savedState != null
                ? savedState.getString(EXTRA_GAME)
                : intent.getStringExtra(EXTRA_GAME);
        game = Json.jsonToGame(gameJson);
        game.setRound(game.roundCount() - 1);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        View layout = getLayoutInflater().inflate(R.layout.jogwheel, null);
        setContentView(layout);

        labelTextView = (TextView) layout.findViewById(R.id.label);
        valueTextView = (TextView) layout.findViewById(R.id.value);

        scoreHistoryTable = new ScoreHistoryTable(getApplicationContext(),
                (TableLayout) layout.findViewById(R.id.runningScores),
                (TableLayout) layout.findViewById(R.id.currentScores),
                (HorizontalScrollView) layout.findViewById(R.id.runningScoresScroller),
                game);

        jogWheel = (JogWheel) layout.findViewById(R.id.jogWheel);
        jogWheel.setModel(game);
        jogWheel.setListener(new JogWheel.Listener() {
            @Override public void selecting(int player, int value) {
                int selectingFrom = game.playerScore(player, game.round());

                labelTextView.setText(game.playerName(player));
                labelTextView.setTextColor(game.playerColor(player));
                labelTextView.setVisibility(View.VISIBLE);

                SpannableStringBuilder ssb = new SpannableStringBuilder();
                if (selectingFrom != 0 && selectingFrom != value) {
                    ssb.append(Integer.toString(selectingFrom));
                    if (value > selectingFrom) {
                        ssb.append(" + ").append(Integer.toString(value - selectingFrom));
                    } else {
                        ssb.append(" - ").append(Integer.toString(selectingFrom - value));
                    }
                    ssb.append(" = ");
                }
                String valueString = (value > 0 ? "+" : "") + Integer.toString(value);
                ssb.append(valueString);
                ssb.setSpan(new AbsoluteSizeSpan(32, true),
                        ssb.length() - valueString.length(), ssb.length(), 0);
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        ssb.length() - valueString.length(), ssb.length(), 0);

                valueTextView.setText(ssb);
                valueTextView.setVisibility(View.VISIBLE);
                actionBar.hide();
            }
            @Override public void selected(int player, int value) {
                int round = game.round();
                game.setPlayerScore(player, round, value);
                scoreHistoryTable.scoreChanged(player, round);
                updateActionBarBackground();
                roundChanged();
            }
            @Override public void cancelled() {
                roundChanged();
            }
        });

        View roundPicker = getLayoutInflater().inflate(R.layout.round_picker, null);
        nextRound = (ImageButton) roundPicker.findViewById(R.id.nextRound);
        roundTextView = (TextView) roundPicker.findViewById(R.id.roundNumber);
        previousRound = (ImageButton) roundPicker.findViewById(R.id.previousRound);
        nextRound.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                game.setRound(game.round() + 1);
                roundChanged();
            }
        });
        previousRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.setRound(game.round() - 1);
                roundChanged();
            }
        });

        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(roundPicker);
        actionBarBackground = new ActionBarBackground(getResources());
        actionBar.setBackgroundDrawable(actionBarBackground);
        updateActionBarBackground();

        roundChanged();
    }

    @Override protected void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putString(EXTRA_GAME, Json.gameToJson(game));
    }

    private void roundChanged() {
        scoreHistoryTable.roundCountChanged();
        roundTextView.setText("Round " + Integer.toString(game.round() + 1));
        labelTextView.setVisibility(View.INVISIBLE);
        previousRound.setEnabled(game.round() > 0);
        nextRound.setEnabled(game.round() < game.roundCount() - 1
                || game.hasNonZeroScore(game.round()));
        valueTextView.setVisibility(View.INVISIBLE);
        actionBar.show();
        jogWheel.invalidate();
    }

    private void updateActionBarBackground() {
        int color = Color.WHITE;
        int maxTotal = Integer.MIN_VALUE;
        for (int p = 0; p < game.playerCount(); p++) {
            int playerTotal = game.playerTotal(p);
            if (playerTotal > maxTotal) {
                color = game.playerColor(p);
                maxTotal = playerTotal;
            } else if (playerTotal == maxTotal) {
                color = Color.WHITE;
            }
        }
        actionBarBackground.setColor(color);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        database = null;
    }

    @Override protected void onPause() {
        super.onPause();

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        paused = true;

        if (shouldAutosave()) {
            saveGame(false, null);
        }
    }

    @Override protected void onResume() {
        super.onResume();

        boolean useWakeLock = true; // TODO: make this a preference?
        if (useWakeLock && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        startPeriodicSave();
        roundChanged();
        paused = false;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void startPeriodicSave() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (shouldAutosave()) {
                    saveGame(true, new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) {
                                startPeriodicSave();
                            }
                        }
                    });
                } else {
                    if (!paused) {
                        startPeriodicSave();
                    }
                }
            }
        }, PERIODIC_SAVE_PERIOD);

    }

    private boolean shouldAutosave() {
        // TODO(jessewilson): only save when dirty
        return true;
    }

    private synchronized void saveGame(boolean inBackground, final Runnable onFinished) {
        if (inBackground) {
            // do in the background to avoid jankiness
            new AsyncTask<Void, Void, Void>() {
                @Override protected Void doInBackground(Void... params) {
                    GameDatabase database = GameActivity.this.database;
                    if (database != null) {
                        database.save(game);
                    }
                    return null;
                }
                @Override protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    if (onFinished != null) {
                        onFinished.run();
                    }
                }
            }.execute((Void) null);
        } else {
            // do in foreground to ensure the game gets saved before the activity finishes
            database.save(game);
            if (onFinished != null) {
                onFinished.run();
            }
        }
    }
}
