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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeActivity extends Activity implements OnClickListener {
    private Game mostRecentGame;
    private GameDatabase database;
    private Button resumeLastGame;
    private Button loadGame;
    private Button newGame;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = GameDatabase.getInstance(getApplicationContext());
        setContentView(R.layout.home);
        setUpWidgets();
    }

    @Override public void onResume() {
        super.onResume();
        mostRecentGame = database.mostRecentGame();
        resumeLastGame.setEnabled(mostRecentGame != null);
        loadGame.setEnabled(mostRecentGame != null);
    }

    @Override protected void onPause() {
        super.onPause();
        mostRecentGame = null;
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        database = null;
    }

    private void setUpWidgets() {
        newGame = (Button) findViewById(R.id.newGame);
        resumeLastGame = (Button) findViewById(R.id.resumeLastGame);
        loadGame = (Button) findViewById(R.id.loadGame);

        for (Button button : new Button[]{newGame, resumeLastGame, loadGame}) {
            button.setOnClickListener(this);
        }
    }

    @Override public void onClick(View v) {
        if (v == newGame) {
            Intent newGameIntent = new Intent(this, SetUpActivity.class);
            startActivity(newGameIntent);
        } else if (v == resumeLastGame) {
            Intent intent = new Intent(HomeActivity.this, GameActivity.class);
            intent.putExtra(GameActivity.EXTRA_GAME, Json.gameToJson(mostRecentGame));
            startActivity(intent);
        } else if (v == loadGame) {
            // TODO
        }
    }
}