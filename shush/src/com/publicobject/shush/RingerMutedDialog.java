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

import android.app.Activity;
import android.app.AlarmManager;
import static android.app.AlarmManager.ELAPSED_REALTIME;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import static android.media.AudioManager.EXTRA_RINGER_MODE;
import static android.media.AudioManager.RINGER_MODE_NORMAL;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import static android.view.Gravity.BOTTOM;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Date;

/**
 * A dialog to schedule the ringer back on after a specified duration.
 */
public class RingerMutedDialog extends Activity {

    /** two hours */
    private static final int DEFAULT_MINUTES = 120;

    private Dialog dialog;
    private ClockSlider clockSlider;

    /** If the user turns the ringer back on, dismiss the dialog and exit. */
    private final BroadcastReceiver dismissFromVolumeUp = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int newRingerMode = intent.getIntExtra(EXTRA_RINGER_MODE, -1);
            if (RINGER_MODE_NORMAL == newRingerMode) {
                dialog.dismiss();
                finish();
            }
        }
    };

    @Override protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        clockSlider = new ClockSlider(this);
        clockSlider.setStart(new Date());

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        clockSlider.setMinutes(preferences.getInt("minutes", DEFAULT_MINUTES));

        dialog = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.shush, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogCommitted();
                    }
                })
                .setNegativeButton(R.string.keepItOff, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogCancelled();
                    }
                })
                .setIcon(null)
                .setView(clockSlider)
                .setTitle(R.string.turnRingerOnIn)
                .setCancelable(true)
                .create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                dialogCancelled();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setGravity(BOTTOM);
    }

    private void dialogCommitted() {
        long onTime = clockSlider.getEnd().getTime();
        long onRealtime = onTime - System.currentTimeMillis() + SystemClock.elapsedRealtime();
        Context context = getApplicationContext();
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .set(ELAPSED_REALTIME, onRealtime, createIntent());
        String message = String.format(getResources().getString(R.string.ringerShushedUntil),
            DateUtils.formatSameDayTime(onTime, onTime, DateFormat.SHORT, DateFormat.SHORT));
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("minutes", clockSlider.getMinutes());
        editor.commit();

        finish();
    }

    private void dialogCancelled() {
        Context context = getApplicationContext();
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(createIntent());
        Toast.makeText(context, R.string.ringerShushedIndefinitely, Toast.LENGTH_LONG).show();

        finish();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long start = clockSlider.getStart().getTime();
        int minutes = clockSlider.getMinutes();
        outState.putLong("start", start);
        outState.putInt("minutes", minutes);
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int minutes = savedInstanceState.getInt("minutes", DEFAULT_MINUTES);
        long start = savedInstanceState.getLong("start", System.currentTimeMillis());
        clockSlider.setStart(new Date(start));
        clockSlider.setMinutes(minutes);
    }

    @Override protected void onStart() {
        super.onStart();
        dialog.show();
        registerReceiver(
                dismissFromVolumeUp, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
    }

    @Override protected void onStop() {
        unregisterReceiver(dismissFromVolumeUp);
        dialog.dismiss();
        super.onStop();
    }

    private PendingIntent createIntent() {
        Context context = getApplicationContext();
        Intent turnRingerOn = new Intent(context, TurnRingerOn.class);
        return PendingIntent.getBroadcast(context, 0, turnRingerOn, FLAG_CANCEL_CURRENT);
    }
}
