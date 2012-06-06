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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;

public final class GameActivity extends Activity {
    public static final String EXTRA_PLAYER_NAMES = "playerNames";
    public static final String EXTRA_PLAYER_COLORS = "playerColors";
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
    private Button nextRound;
    private Button previousRound;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = GameDatabase.getInstance(getApplicationContext());
        game = createGame();
        game.setRound(game.roundCount() - 1);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());

        View layout = getLayoutInflater().inflate(R.layout.jogwheel, null);
        setContentView(layout);

        labelTextView = (TextView) layout.findViewById(R.id.label);
        valueTextView = (TextView) layout.findViewById(R.id.value);
        nextRound = (Button) layout.findViewById(R.id.nextRound);
        previousRound = (Button) layout.findViewById(R.id.previousRound);

        scoreHistoryTable = new ScoreHistoryTable(getApplicationContext(),
                (TableLayout) layout.findViewById(R.id.runningScores),
                (TableLayout) layout.findViewById(R.id.currentScores),
                (HorizontalScrollView) layout.findViewById(R.id.runningScoresScroller),
                game);

        jogWheel = (JogWheel) layout.findViewById(R.id.jogWheel);
        jogWheel.setModel(game);
        jogWheel.setListener(new JogWheel.Listener() {
            @Override public void selecting(int player, int value) {
                labelTextView.setText(game.playerName(player));
                labelTextView.setTextColor(game.playerColor(player));
                String prefix = value > 0 ? "+" : "";
                valueTextView.setText(prefix + Integer.toString(value));
            }
            @Override public void selected(int player, int value) {
                int round = game.round();
                game.setPlayerScore(player, round, value);
                scoreHistoryTable.scoreChanged(player, round);
                roundChanged();
            }
            @Override public void cancelled() {
                roundChanged();
            }
        });

        nextRound.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                game.setRound(game.round() + 1);
                roundChanged();
            }
        });

        previousRound.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                game.setRound(game.round() - 1);
                roundChanged();
            }
        });

        roundChanged();
    }

    private void roundChanged() {
        scoreHistoryTable.roundCountChanged();
        labelTextView.setText("Round");
        labelTextView.setTextColor(Color.WHITE);
        previousRound.setEnabled(game.round() > 0);
        nextRound.setEnabled(game.round() < game.roundCount() - 1
                || game.hasNonZeroScore(game.round()));
        valueTextView.setText(Integer.toString(game.round() + 1));
        jogWheel.invalidate();
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

    private Game createGame() {
        if (getIntent().hasExtra(EXTRA_PLAYER_NAMES)) {
            return createNewGame();
        } else {
            return Json.jsonToGame(getIntent().getStringExtra(EXTRA_GAME));
        }
    }

    private Game createNewGame() {
        String[] playerNames = getIntent().getStringArrayExtra(EXTRA_PLAYER_NAMES);
        int[] playerColors = getIntent().getIntArrayExtra(EXTRA_PLAYER_COLORS);
        Game game = new Game();
        game.setDateStarted(System.currentTimeMillis());
        for (int i = 0; i < playerNames.length; i++) {
            game.addPlayer(playerNames[i], playerColors[i]);
        }
        return game;
    }

    private synchronized void saveGame(boolean inBackground,
                                       final Runnable onFinished) {
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
