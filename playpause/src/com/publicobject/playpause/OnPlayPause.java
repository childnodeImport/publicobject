/**
 * Copyright (C) 2010 Jesse Wilson
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

package com.publicobject.playpause;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Responds to the user touching the widget by broadcasting an event to the
 * current media app.
 */
public final class OnPlayPause extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Log.i("PlayPause", "Music was active: " + audioManager.isMusicActive());

        Log.i("PlayPause", "onReceive : " + intent);
        long now = SystemClock.uptimeMillis();

        Intent down = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        down.putExtra(Intent.EXTRA_KEY_EVENT,
                new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0));
        context.sendOrderedBroadcast(down, null);

        Intent up = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        up.putExtra(Intent.EXTRA_KEY_EVENT,
                new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0));
        context.sendOrderedBroadcast(up, null);

        Log.i("PlayPause", "Music now active: " + audioManager.isMusicActive());
    }
}
