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
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SetUpActivity extends Activity {
    private static final int ROWS = 2;
    private static final int COLS = 4;

    private LinearLayout namesRow0;
    private LinearLayout namesRow1;
    private AutoCompleteTextView name;
    private Button next;
    private Button play;

    private List<Player> players;
    private Player editingPlayer;
    private ColorPicker colorPicker;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.set_up, null);
        setContentView(layout);

        name = (AutoCompleteTextView) layout.findViewById(R.id.name);
        Set<String> playerNames = GameDatabase.getInstance(getApplicationContext())
                .suggestedPlayerNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                playerNames.toArray(new String[playerNames.size()]));
        name.setThreshold(1);
        name.setAdapter(adapter);

        View colorPlaceholder = layout.findViewById(R.id.colorPlaceholder);
        next = (Button) layout.findViewById(R.id.next);
        namesRow0 = (LinearLayout) layout.findViewById(R.id.namesRow0);
        namesRow1 = (LinearLayout) layout.findViewById(R.id.namesRow1);
        play = (Button) layout.findViewById(R.id.play);

        name.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int a, int b, int c) {
            }
            @Override public void onTextChanged(CharSequence text, int a, int b, int c) {
                String currentName = name.getText().toString().trim();
                editingPlayer.setName(currentName);
                updateButtons();
            }
            @Override public void afterTextChanged(Editable textView) {
            }
        });

        name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView textView, int action, KeyEvent event) {
                next();
                return true;
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                next();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                play();
            }
        });

        ColorPicker.Listener listener = new ColorPicker.Listener() {
            @Override public void selecting() {
                name.setVisibility(View.INVISIBLE);
                next.setVisibility(View.INVISIBLE);
                play.setVisibility(View.INVISIBLE);
                namesRow0.setVisibility(View.INVISIBLE);
                namesRow1.setVisibility(View.INVISIBLE);
            }
            @Override public void selected(int color) {
                editingPlayer.setColor(color);
                name.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);
                play.setVisibility(View.VISIBLE);
                namesRow0.setVisibility(View.VISIBLE);
                namesRow1.setVisibility(View.VISIBLE);
            }
        };

        colorPicker = new ColorPicker(getApplicationContext(), null, colorPlaceholder, listener);
        layout.addView(colorPicker);

        makePlayers();
        setEditingPlayer(players.get(0));
    }

    private void next() {
        if (!next.isEnabled()) {
            return; // ignore calls from the soft keyboard when the button is disabled
        }
        int current = players.indexOf(editingPlayer);
        setEditingPlayer(players.get((current + 1) % players.size()));
    }

    private void play() {
        int nonEmptyPlayerCount = nonEmptyPlayerCount();
        String[] playerNames = new String[nonEmptyPlayerCount];
        int[] playerColors = new int[nonEmptyPlayerCount];

        int p = 0;
        for (Player player : players) {
            if (!player.isEmpty()) {
                playerNames[p] = player.getName();
                playerColors[p] = player.getColor();
                p++;
            }
        }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames);
        intent.putExtra(GameActivity.EXTRA_PLAYER_COLORS, playerColors);
        startActivity(intent);
    }

    private void setEditingPlayer(Player player) {
        editingPlayer = player;
        String nameString = player.getName();
        name.setText(nameString);
        name.setSelection(nameString.length());
        colorPicker.setColor(player.getColor());
        updateButtons();
    }

    private void updateButtons() {
        play.setEnabled(nonEmptyPlayerCount() != 0);
        next.setEnabled(!editingPlayer.isEmpty());
    }

    private int nonEmptyPlayerCount() {
        int nonEmptyPlayerCount = 0;
        for (Player player : players) {
            if (!player.isEmpty()) {
                nonEmptyPlayerCount++;
            }
        }
        return nonEmptyPlayerCount;
    }

    private void makePlayers() {
        View.OnTouchListener nameTouchListener = new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                for (Player player : players) {
                    if (player.textView == view) {
                        setEditingPlayer(player);
                    }
                }
                return true;
            }
        };

        players = new ArrayList<Player>();
        for (int r = 0; r < ROWS; r++) {
            LinearLayout layout = (r == 0) ? namesRow0 : namesRow1;
            for (int c = 0; c < COLS; c++) {
                TextView textView = new TextView(getApplicationContext());
                textView.setTextAppearance(getApplicationContext(),
                        android.R.style.TextAppearance_Holo_Large);
                textView.setPadding(10, 10, 10, 10);
                textView.setVisibility(View.GONE);
                layout.addView(textView, new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                textView.setOnTouchListener(nameTouchListener);
                players.add(new Player(textView, c == 0, Colors.DEFAULT_COLORS[r * COLS + c]));
            }
        }
    }

    static class Player {
        private final TextView textView;
        private int color;
        private boolean firstInRow;

        Player(TextView textView, boolean firstInRow, int color) {
            this.textView = textView;
            this.firstInRow = firstInRow;
            setColor(color);
            updateVisibility();
        }

        private boolean isEmpty() {
            return textView.getText().length() == 0;
        }

        public void setName(String name) {
            this.textView.setText(name);
            updateVisibility();
        }

        private void updateVisibility() {
            textView.setVisibility((firstInRow || !isEmpty()) ? View.VISIBLE : View.GONE);
        }

        public String getName() {
            return textView.getText().toString();
        }

        public void setColor(int color) {
            this.color = color;
            textView.setTextColor(color);
        }

        public int getColor() {
            return color;
        }
    }
}
