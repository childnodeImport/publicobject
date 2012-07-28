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

import android.os.Handler;
import java.util.concurrent.TimeUnit;

/**
 * Manages saving a game to durable storage. Methods on this class may only be
 * called on the main thread.
 */
final class GameSaver implements Runnable {
    private final Handler handler = new Handler();
    private final GameDatabase database;
    private final Game game;
    private volatile boolean savePending = false;

    GameSaver(GameDatabase database, Game game) {
        this.database = database;
        this.game = game;
    }

    /**
     * Saves the game at some point in the future. Multiple calls to this
     * method may be coalesced into a single filesystem write.
     */
    public void saveLater() {
        if (savePending) {
            return;
        }

        handler.postDelayed(this, TimeUnit.SECONDS.toMillis(30));
        savePending = true;
    }

    public void onPause() {
        handler.removeCallbacks(this);
        database.save(game);
    }

    @Override public void run() {
        savePending = false;
        database.saveLater(game);
    }
}
