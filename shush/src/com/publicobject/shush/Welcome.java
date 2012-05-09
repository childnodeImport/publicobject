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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A dialog that explains how Shush works and lets users pick limited options.
 *
 * TODO: read and write preferences in the background?
 */
public final class Welcome extends Activity
        implements DialogInterface.OnClickListener, OnCancelListener {
    public static final int[] COLORS = {
            Color.rgb(0xff, 0x00, 0xff), //  0 pink
            Color.rgb(0x88, 0x33, 0xbb), //  1 purple
            Color.rgb(0x66, 0x99, 0xff), //  2 blue
            Color.rgb(0x99, 0xcc, 0x33), //  3 green
            Color.rgb(0xff, 0xcc, 0x00), //  4 yellow
            Color.rgb(0xff, 0x66, 0x00), //  5 orange
            Color.rgb(0xcc, 0x00, 0x00), //  6 red
            Color.rgb(0x33, 0xb5, 0xe5), //  7 ICS blue
            Color.rgb(0xaa, 0x66, 0xcc), //  8 ICS purple
            Color.rgb(0x99, 0xcc, 0x00), //  9 ICS green
            Color.rgb(0xff, 0xbb, 0x33), // 10 ICS orange
            Color.rgb(0xff, 0x44, 0x44), // 11 ICS red
    };
    public static final int[] COLOR_NAMES = {
            R.string.pink,
            R.string.purple,
            R.string.blue,
            R.string.green,
            R.string.yellow,
            R.string.orange,
            R.string.red,
            R.string.blue,
            R.string.purple,
            R.string.green,
            R.string.orange,
            R.string.red,
    };

    private boolean notifications = true;
    private int colorIndex = 0;

    @Override protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences(
                RingerMutedDialog.class.getSimpleName(), Context.MODE_PRIVATE);
        int savedColor = preferences.getInt("color", COLORS[0]);
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == savedColor) {
                colorIndex = i;
                break;
            }
        }
        notifications = preferences.getBoolean("notifications", true);

        View view = LayoutInflater.from(this).inflate(R.layout.welcome, null);
        Picker colorPicker = new ColorPicker(view);
        colorPicker.selectionChanged();

        Picker notificationsPicker = new NotificationsPicker(view);
        notificationsPicker.selectionChanged();

        Picker sharePicker = new SharePicker(view);
        sharePicker.selectionChanged();

        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setView(view)
                .setOnCancelListener(this)
                .setPositiveButton(R.string.okay, this)
                .create()
                .show();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }

    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }

    private abstract class Picker implements View.OnTouchListener {
        private final LinearLayout layout;
        protected final ImageView image;
        protected final TextView label;

        public Picker(View view, int layoutId, int labelId, int imageId) {
            layout = (LinearLayout) view.findViewById(layoutId);
            image = (ImageView) view.findViewById(imageId);
            label = (TextView) view.findViewById(labelId);
            layout.setOnTouchListener(this);
        }

        public final boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                buttonStateChanged(true);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                buttonStateChanged(false);
                toggle();
                selectionChanged();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                buttonStateChanged(false);
            }
            return true;
        }

        protected void buttonStateChanged(boolean down) {
            int background = down ? Color.rgb(0x40, 0x40, 0x40) : Color.rgb(0x18, 0x18, 0x18);
            layout.setBackgroundColor(background);
        }

        abstract void selectionChanged();

        abstract void toggle();
    }

    private class ColorPicker extends Picker {
        public ColorPicker(View view) {
            super(view, R.id.colorsLayout, R.id.colorsLabel, R.id.colorsImage);
        }

        @Override void toggle() {
            colorIndex++;

            if (Build.VERSION.SDK_INT < 14) {
                // Use colors 0-7 for Honeycomb and earlier
                if (colorIndex >= 7) {
                    colorIndex = 0;
                }
            } else {
                // Use colors 7-11 for ICS
                if (colorIndex < 7 || colorIndex >= 12) {
                    colorIndex = 7;
                }
            }

            SharedPreferences preferences = getSharedPreferences(
                    RingerMutedDialog.class.getSimpleName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("color", COLORS[colorIndex]);
            editor.commit();
        }

        @Override protected void buttonStateChanged(boolean down) {
            image.setImageResource(down ? R.drawable.colors_down : R.drawable.colors_up);
            super.buttonStateChanged(down);
        }

        @Override void selectionChanged() {
            image.setBackgroundColor(COLORS[colorIndex]);
            label.setText(COLOR_NAMES[colorIndex]);
        }
    }

    private class NotificationsPicker extends Picker {
        public NotificationsPicker(View view) {
            super(view, R.id.notificationsLayout, R.id.notificationsLabel, R.id.notificationsImage);
        }

        @Override void toggle() {
            notifications = !notifications;

            SharedPreferences preferences = getSharedPreferences(
                    RingerMutedDialog.class.getSimpleName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("notifications", notifications);
            editor.commit();
        }

        @Override void selectionChanged() {
            if (notifications) {
                image.setImageResource(R.drawable.notifications_on);
                label.setText(R.string.notificationsOn);
            } else {
                image.setImageResource(R.drawable.notifications_off);
                label.setText(R.string.notificationsOff);
            }
        }
    }

    private class SharePicker extends Picker {
        public SharePicker(View view) {
            super(view, R.id.shareLayout, R.id.shareLabel, R.id.shareImage);
        }

        @Override void toggle() {
            Intent intent=new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            Context context = getApplicationContext();
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shareSubject));
            intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.shareMessage));
            startActivity(Intent.createChooser(intent,
                    context.getString(R.string.shareChooserTitle)));
        }

        @Override void selectionChanged() {
        }
    }
}
