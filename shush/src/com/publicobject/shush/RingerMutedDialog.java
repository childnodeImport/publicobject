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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;

import static android.media.AudioManager.EXTRA_RINGER_MODE;
import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.RINGER_MODE_VIBRATE;
import static android.media.AudioManager.VIBRATE_SETTING_OFF;
import static android.media.AudioManager.VIBRATE_SETTING_ON;
import static android.media.AudioManager.VIBRATE_TYPE_RINGER;
import static android.view.Gravity.BOTTOM;
import static com.publicobject.shush.ClockSlider.CLOCK_SLIDER;
import static com.publicobject.shush.ClockSlider.VIBRATE_PICKER;
import static com.publicobject.shush.ClockSlider.VOLUME_SLIDER;

/**
 * A dialog to schedule the ringer back on after a specified duration.
 */
public final class RingerMutedDialog extends Activity {
    /** two hours */
    public static final int DEFAULT_MINUTES = 120;
    /** 80% of max volume */
    public static final float DEFAULT_VOLUME = 0.8f;
    /** show full-screen toast messages for two seconds */
    private static final long TOAST_LENGTH_MILLIS = 2 * 1000;
    /** cancel shush after 60 seconds of inactivity */
    private static final long TIMEOUT_MILLIS = 60 * 1000;
    /** observe broadcast ringer mode changes */
    private final IntentFilter RINGER_MODE_CHANGED
            = new IntentFilter("android.media.RINGER_MODE_CHANGED");

    /** the shush alert dialog. */
    private Dialog dialog;
    /** either the regular dialog or the dialog in a full screen window for the lock screen */
    private ShushWindow shushWindow;
    /** the main UI control */
    private ClockSlider clockSlider;

    /** Read/write access to this activity's event queue */
    private final Handler handler = new Handler();

    /** True for notifications; false for toasts. */
    private boolean notifications;

    /** If the user turns the ringer back on, dismiss the dialog and exit. */
    private final BroadcastReceiver dismissFromVolumeUp = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (clockSlider == null) {
                return; // race between volume up and onStop
            }
            int newRingerMode = intent.getIntExtra(EXTRA_RINGER_MODE, -1);
            if (RINGER_MODE_NORMAL == newRingerMode) {
                cancel(false);
            } else if (newRingerMode == RINGER_MODE_VIBRATE) {
                clockSlider.setVibrateNow(true);
            } else if (newRingerMode == RINGER_MODE_SILENT) {
                clockSlider.setVibrateNow(false);
            }
        }
    };

    /** If the user doesn't take action, quietly dismiss Shush. */
    private final Runnable dismissFromTimeout = new Runnable() {
        public void run() {
            if (shushWindow != null) {
                cancel(false);
            }
        }
    };

    /**
     * Returns an intent that triggers this dialog as an activity.
     */
    public static Intent getIntent(Context context) {
        Intent result = new Intent();
        result.setClass(context, RingerMutedDialog.class);
        result.setAction(RingerMutedDialog.class.getName());
        result.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return result;
    }

    @Override protected void onStart() {
        super.onStart();

        TurnRingerOn.cancelScheduled(this);
        RingerMutedNotification.dismiss(this);

        KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        shushWindow = keyguard.inKeyguardRestrictedInputMode()
                ? new ShushFullscreen()
                : new ShushDialogOnly();
        createShushDialog();
        clockSlider.setRingerMutedDialog(this);
        clockSlider.setStart(new Date());

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        clockSlider.setMinutes(preferences.getInt("minutes", DEFAULT_MINUTES));
        clockSlider.setVolume(preferences.getFloat("volume", DEFAULT_VOLUME));
        clockSlider.setColor(preferences.getInt("color", Welcome.COLORS[0]));
        clockSlider.setVibrateNow(getDeviceVibrateNow());
        clockSlider.setVibrateLater(getDeviceVibrateLater());
        notifications = preferences.getBoolean("notifications", true);

        registerReceiver(dismissFromVolumeUp, RINGER_MODE_CHANGED);
        registerTimeoutCallback();
    }

    @Override protected void onStop() {
        unregisterReceiver(dismissFromVolumeUp);
        unregisterTimeoutCallback();
        dialog.dismiss();
        shushWindow = null;
        clockSlider = null;
        super.onStop();
    }

    private void registerTimeoutCallback() {
        handler.postDelayed(dismissFromTimeout, TIMEOUT_MILLIS);
    }

    private void unregisterTimeoutCallback() {
        handler.removeCallbacks(dismissFromTimeout);
    }

    public void modeChanged(int displayMode, boolean vibrateNow, boolean vibrateLater) {
        if (displayMode == CLOCK_SLIDER) {
            dialog.setTitle(R.string.turnRingerOnIn);
        } else if (displayMode == VOLUME_SLIDER) {
            dialog.setTitle(R.string.restoreVolumeLevel);
        } else if (displayMode == VIBRATE_PICKER) {
            if (vibrateNow && vibrateLater) {
                dialog.setTitle(R.string.vibrateOnOn);
            } else if (!vibrateNow && vibrateLater) {
                dialog.setTitle(R.string.vibrateOffOn);
            } else if (vibrateNow && !vibrateLater) {
                dialog.setTitle(R.string.vibrateOnOff);
            } else {
                dialog.setTitle(R.string.vibrateOffOff);
            }
        } else {
            throw new IllegalArgumentException();
        }

        setDeviceVibrateNow(vibrateNow);
        setDeviceVibrateLater(vibrateLater);
    }

    private boolean getDeviceVibrateNow() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        return audioManager.getRingerMode() == RINGER_MODE_VIBRATE;
    }

    private void setDeviceVibrateNow(boolean vibrateNow) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        boolean vibrateSetting = audioManager.getRingerMode() == RINGER_MODE_VIBRATE;
        if (vibrateSetting != vibrateNow) {
            audioManager.setRingerMode(vibrateNow ? RINGER_MODE_VIBRATE : RINGER_MODE_SILENT);
        }
    }

    private void setDeviceVibrateLater(boolean vibrate) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // For 'ringing' apps that use the pre-ICS system-wide vibrate setting.
        audioManager.setVibrateSetting(VIBRATE_TYPE_RINGER,
                vibrate ? VIBRATE_SETTING_ON : VIBRATE_SETTING_OFF);
        // For the phone app in ICS and beyond
        Settings.System.putInt(getContentResolver(), "vibrate_when_ringing", vibrate ? 1 : 0);
    }

    private boolean getDeviceVibrateLater() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            return audioManager.getVibrateSetting(VIBRATE_TYPE_RINGER) == VIBRATE_SETTING_ON;
        } else {
            // TODO: does this need to be "vibrate_on_ring" for ICS ?...
            return Settings.System.getInt(getContentResolver(), "vibrate_when_ringing", 1) == 1;
        }
    }

    private void commit() {
        if (clockSlider == null) {
            return; // race between volume up and shush button
        }

        unregisterTimeoutCallback();
        PendingIntent ringerOn = TurnRingerOn.createPendingIntent(this, clockSlider.getVolume());

        long onTime = clockSlider.getEnd().getTime();
        TurnRingerOn.schedule(this, ringerOn, onTime);

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("minutes", clockSlider.getMinutes());
        editor.putFloat("volume", clockSlider.getVolume());
        editor.commit();

        String message = RingerMutedNotification.getMessage(this, onTime);
        if (notifications) {
            RingerMutedNotification.show(this, message, ringerOn);
            shushWindow.finish(null);
        } else {
            shushWindow.finish(message);
        }
    }

    private void cancel(boolean showMessage) {
        if (clockSlider == null) {
            return; // race between volume up and cancel button
        }

        unregisterTimeoutCallback();
        String message = showMessage
                ? getString(R.string.ringerShushedIndefinitely)
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
        clockSlider.setVibrateNow(getDeviceVibrateNow());
        clockSlider.setVibrateLater(getDeviceVibrateLater());
    }

    private void createShushDialog() {
        clockSlider = new ClockSlider(getApplicationContext(), null);

        // Wrap the clock slider in some padding for Holo
        View dialogView;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.addView(clockSlider, params);
            params.setMargins(4, 20, 4, 20);
            dialogView = linearLayout;
        } else {
            dialogView = clockSlider;
        }

        dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ShushTheme))
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
                .setView(dialogView)
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

    interface ShushWindow {
        void finish(String message);
    }

    /**
     * Show Shush! in a dialog by default.
     */
    class ShushDialogOnly implements ShushWindow {
        public void finish(String message) {
            if (message != null) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
            RingerMutedDialog.this.finish();
        }
    }

    /**
     * Show Shush! as a full-screen app when triggered by the lock screen.
     */
    class ShushFullscreen implements ShushWindow {
        private final Window fullScreenWindow;

        ShushFullscreen() {
            fullScreenWindow = getWindow();
            fullScreenWindow.setContentView(R.layout.toast);
            fullScreenWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            fullScreenWindow.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        public void finish(String message) {
            if (message == null) {
                RingerMutedDialog.this.finish();
                return;
            }

            dialog.dismiss();
            TextView toast = (TextView) fullScreenWindow.findViewById(R.id.toast);
            toast.setText(message);

            handler.postDelayed(new Runnable() {
                public void run() {
                    RingerMutedDialog.this.finish();
                }
            }, TOAST_LENGTH_MILLIS);
        }
    }
}
