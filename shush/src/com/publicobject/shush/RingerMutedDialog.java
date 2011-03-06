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
import android.app.KeyguardManager;
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
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateUtils;
import static android.view.Gravity.BOTTOM;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Date;

/**
 * A dialog to schedule the ringer back on after a specified duration.
 */
public class RingerMutedDialog extends Activity {

    /** two hours */
    public static final int DEFAULT_MINUTES = 120;
    /** 80% of max volume */
    public static final float DEFAULT_VOLUME = 0.8f;
    /** either a dialog (regular) or full screen window (for lock screen) */
    private ShushWindow shushWindow;
    /** the main UI control */
    private ClockSlider clockSlider;

    /** If the user turns the ringer back on, dismiss the dialog and exit. */
    private final BroadcastReceiver dismissFromVolumeUp = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int newRingerMode = intent.getIntExtra(EXTRA_RINGER_MODE, -1);
            if (RINGER_MODE_NORMAL == newRingerMode) {
                cancel(false);
            }
        }
    };

    @Override protected void onStart() {
        super.onStart();

        KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        shushWindow = keyguard.inKeyguardRestrictedInputMode()
                ? new ShushFullscreen()
                : new ShushDialog();
        clockSlider = shushWindow.getClockSlider();
        clockSlider.setRingerMutedDialog(this);
        clockSlider.setStart(new Date());

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        clockSlider.setMinutes(preferences.getInt("minutes", DEFAULT_MINUTES));
        clockSlider.setVolume(preferences.getFloat("volume", DEFAULT_VOLUME));

        registerReceiver(
                dismissFromVolumeUp, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
    }

    @Override protected void onStop() {
        unregisterReceiver(dismissFromVolumeUp);
        shushWindow.close();
        shushWindow = null;
        clockSlider = null;
        super.onStop();
    }

    public void volumeSliding(boolean sliding) {
        shushWindow.setTitle(sliding ? R.string.restoreVolumeLevel : R.string.turnRingerOnIn);
    }

    private void commit() {
        long onTime = clockSlider.getEnd().getTime();
        long onRealtime = onTime - System.currentTimeMillis() + SystemClock.elapsedRealtime();
        Context context = getApplicationContext();
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .set(ELAPSED_REALTIME, onRealtime, createIntent());
        String message = String.format(getResources().getString(R.string.ringerShushedUntil),
                DateUtils.formatSameDayTime(onTime, onTime, DateFormat.SHORT, DateFormat.SHORT));

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("minutes", clockSlider.getMinutes());
        editor.putFloat("volume", clockSlider.getVolume());
        editor.commit();

        shushWindow.finish(message);
    }

    private void cancel(boolean showMessage) {
        Context context = getApplicationContext();
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(createIntent());

        String message = showMessage
                ? context.getString(R.string.ringerShushedIndefinitely)
                : null;
        shushWindow.finish(message);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long start = clockSlider.getStart().getTime();
        int minutes = clockSlider.getMinutes();
        float volume = clockSlider.getVolume();
        outState.putLong("start", start);
        outState.putInt("minutes", minutes);
        outState.putFloat("volume", volume);
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int minutes = savedInstanceState.getInt("minutes", DEFAULT_MINUTES);
        long start = savedInstanceState.getLong("start", System.currentTimeMillis());
        float volume = savedInstanceState.getFloat("volume", DEFAULT_VOLUME);
        clockSlider.setStart(new Date(start));
        clockSlider.setMinutes(minutes);
        clockSlider.setVolume(volume);
    }

    private PendingIntent createIntent() {
        Context context = getApplicationContext();
        Intent turnRingerOn = new Intent(context, TurnRingerOn.class);
        turnRingerOn.putExtra("volume", clockSlider.getVolume());
        return PendingIntent.getBroadcast(context, 0, turnRingerOn, FLAG_CANCEL_CURRENT);
    }

    interface ShushWindow {
        ClockSlider getClockSlider();
        void setTitle(int titleId);
        void finish(String message);
        void close();
    }

    class ShushDialog implements ShushWindow {
        private final ClockSlider clockSlider;
        private final Dialog dialog;

        ShushDialog() {
            clockSlider = new ClockSlider(RingerMutedDialog.this, null);
            dialog = new AlertDialog.Builder(RingerMutedDialog.this)
                    .setPositiveButton(R.string.shush, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            commit();
                        }
                    })
                    .setNegativeButton(R.string.keepItOff, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            cancel(true);
                        }
                    })
                    .setIcon(null)
                    .setView(clockSlider)
                    .setTitle(R.string.turnRingerOnIn)
                    .setCancelable(true)
                    .create();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setGravity(BOTTOM);
            dialog.show();
        }

        public ClockSlider getClockSlider() {
            return clockSlider;
        }

        public void setTitle(int titleId) {
            dialog.setTitle(titleId);
        }

        public void finish(String message) {
            if (message != null) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
            RingerMutedDialog.this.finish();
        }

        public void close() {
            dialog.dismiss();
        }
    }

    class ShushFullscreen implements ShushWindow {
        private final Window fullScreenWindow;

        ShushFullscreen() {
            fullScreenWindow = getWindow();
            fullScreenWindow.setContentView(R.layout.fullscreen);
            fullScreenWindow.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN);

            Button shush = (Button) fullScreenWindow.findViewById(R.id.shush);
            shush.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    commit();
                }
            });

            Button keepItOff = (Button) fullScreenWindow.findViewById(R.id.keepItOff);
            keepItOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    cancel(true);
                }
            });
        }

        public ClockSlider getClockSlider() {
            return (ClockSlider) fullScreenWindow.findViewById(R.id.clockSlider);
        }

        public void setTitle(int titleId) {
            TextView title = (TextView) fullScreenWindow.findViewById(R.id.title);
            title.setText(titleId);
        }

        public void finish(String message) {
            if (message == null) {
                RingerMutedDialog.this.finish();
                return;
            }

            fullScreenWindow.setContentView(R.layout.toast);
            TextView toast = (TextView) fullScreenWindow.findViewById(R.id.toast);
            toast.setText(message);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    RingerMutedDialog.this.finish();
                }
            }, 3000);
        }

        public void close() {}
    }
}
