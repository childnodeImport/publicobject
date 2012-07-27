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

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import java.util.ArrayList;
import java.util.List;

public final class ShareActivity extends SherlockActivity {
    private Game game;
    private GameDatabase database;
    private String username = "limpbizkit";
    private TextView messagePreview;
    private Button share;

    @Override protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        database = GameDatabase.getInstance(getApplicationContext());

        Intent intent = getIntent();
        String gameId = savedState != null
                ? savedState.getString(IntentExtras.GAME_ID)
                : intent.getStringExtra(IntentExtras.GAME_ID);
        game = database.get(gameId);

        View layout = getLayoutInflater().inflate(R.layout.share, null);

        final EditText gameName = (EditText) layout.findViewById(R.id.gameName);
        final RadioButton highScoreWins = (RadioButton) layout.findViewById(R.id.highScoreWins);
        final RadioButton lowScoreWins = (RadioButton) layout.findViewById(R.id.lowScoreWins);
        messagePreview = (TextView) layout.findViewById(R.id.messagePreview);
        share = (Button) layout.findViewById(R.id.share);

        if (game.getName() == null) {
            game.setName("");
        }
        gameName.setText(game.getName());
        highScoreWins.setChecked(game.getWinCondition() == WinCondition.HIGH_SCORE);
        lowScoreWins.setChecked(game.getWinCondition() == WinCondition.LOW_SCORE);
        updateMessagePreview();

        gameName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int a, int b, int c) {
            }
            @Override public void onTextChanged(CharSequence text, int a, int b, int c) {
                game.setName(gameName.getText().toString().trim());
                updateMessagePreview();
            }
            @Override public void afterTextChanged(Editable textView) {
            }
        });

        OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                WinCondition winCondition = highScoreWins.isChecked()
                        ? WinCondition.HIGH_SCORE
                        : WinCondition.LOW_SCORE;
                game.setWinCondition(winCondition);
                updateMessagePreview();
            }
        };
        highScoreWins.setOnCheckedChangeListener(checkedChangeListener);
        lowScoreWins.setOnCheckedChangeListener(checkedChangeListener);

        setContentView(layout);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void updateMessagePreview() {
        messagePreview.setText(getSummaryMessage());

        String gameName = game.getName();
        if (gameName == null || gameName.isEmpty()) {
            messagePreview.setVisibility(View.INVISIBLE);
            share.setEnabled(false);
        } else {
            messagePreview.setVisibility(View.VISIBLE);
            share.setEnabled(true);
        }
    }

    private String getSummaryMessage() {
        List<String> winners = new ArrayList<String>();
        List<String> losers = new ArrayList<String>();

        if (game.getWinCondition() == WinCondition.NONE) {
            for (int p = 0; p < game.playerCount(); p++) {
                winners.add(game.playerName(p));
            }
        } else {
            int winningTotal = game.getWinCondition() == WinCondition.HIGH_SCORE
                    ? game.maxTotal()
                    : game.minTotal();
            for (int p = 0; p < game.playerCount(); p++) {
                if (winningTotal == game.playerTotal(p)) {
                    winners.add(game.playerName(p));
                } else {
                    losers.add(game.playerName(p));
                }
            }
        }

        StringBuilder message = new StringBuilder();
        appendNames(message, winners);

        if (game.getWinCondition() == WinCondition.NONE) {
            message.append(" played ");
        } else if (!losers.isEmpty()) {
            message.append(" defeated ");
            appendNames(message, losers);
            message.append(" at ");
        } else {
            message.append(" tied at ");
        }

        String gameName = game.getName();
        if (gameName == null || gameName.isEmpty()) {
            gameName = "a game";
        }
        message.append(gameName).append(".\n");
        message.append("roundsapp.com/");
        message.append(username);
        message.append("/42");
        return message.toString();
    }

    private void appendNames(StringBuilder message, List<String> players) {
        for (int i = 0; i < players.size(); i++) {
            String player = players.get(i);
            if (i == 0) {
                message.append(player);
            } else if (i < players.size() - 1) {
                message.append(", ").append(player);
            } else {
                message.append(" and ").append(player);
            }
        }
    }

    @Override protected void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putString(IntentExtras.GAME_ID, game.getId());
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra(IntentExtras.GAME_ID, game.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
