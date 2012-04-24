/**
 * Copyright (C) 2009 Jesse Wilson
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


package com.publicobject.shush;

import android.app.AlarmManager;
import android.app.PendingIntent;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import android.content.BroadcastReceiver;
import android.content.Context;
import static android.content.Context.AUDIO_SERVICE;
import android.content.Intent;
import android.media.AudioManager;
import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.STREAM_RING;
import android.widget.Toast;

/**
 * Turns the ringer on when received.
 */
public final class TurnRingerOn extends BroadcastReceiver {
    public static PendingIntent createPendingIntent(Context context, float volume) {
        Intent intent = new Intent(context, TurnRingerOn.class);
        intent.putExtra("volume", volume);
        return PendingIntent.getBroadcast(context, 0, intent, FLAG_CANCEL_CURRENT);
    }

    public void onReceive(Context context, Intent intent) {
        AudioManager audioManager = getAudioManager(context);
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT
                && audioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
            return;
        }

        audioManager.setRingerMode(RINGER_MODE_NORMAL);
        int volume = (int) (audioManager.getStreamMaxVolume(STREAM_RING)
                * intent.getExtras().getFloat("volume", RingerMutedDialog.DEFAULT_VOLUME));
        audioManager.setStreamVolume(STREAM_RING, volume, 0);
        Toast.makeText(context, R.string.ringerRestored, Toast.LENGTH_LONG).show();
    }

    public static void schedule(Context context, PendingIntent ringerOnIntent, long onTime) {
        getAlarmService(context).set(AlarmManager.RTC_WAKEUP, onTime, ringerOnIntent);
    }

    public static void cancelScheduled(Context context) {
        getAlarmService(context).cancel(createPendingIntent(context, 0.0f));
    }

    private AudioManager getAudioManager(Context context) {
        return (AudioManager) context.getSystemService(AUDIO_SERVICE);
    }

    private static AlarmManager getAlarmService(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}
