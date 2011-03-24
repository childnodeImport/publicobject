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
import android.content.SharedPreferences;
import android.graphics.Color;
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
            Color.rgb(0xff, 0x00, 0xff), // pink
            Color.rgb(0xff, 0x00, 0x00), // red
            Color.rgb(0xff, 0xff, 0x00), // yellow
            Color.rgb(0x00, 0xff, 0x00), // green
            Color.rgb(0x00, 0xff, 0xff), // cyan
            Color.rgb(0x00, 0x00, 0xff), // blue
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

        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(R.drawable.shush)
                .setView(view)
                .setTitle(R.string.title)
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
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
                    || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                layout.setBackgroundColor(Color.argb(102, 115, 115, 115));
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                layout.setBackgroundColor(Color.TRANSPARENT);
                toggle();
                selectionChanged();
            }
            return true;
        }

        abstract void selectionChanged();

        abstract void toggle();
    }

    private class ColorPicker extends Picker {
        public ColorPicker(View view) {
            super(view, R.id.colorsLayout, R.id.colorsLabel, R.id.colorsImage);
        }

        @Override void toggle() {
            colorIndex = (colorIndex + 1) % 6;

            SharedPreferences preferences = getSharedPreferences(
                    RingerMutedDialog.class.getSimpleName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("color", COLORS[colorIndex]);
            editor.commit();
        }

        @Override void selectionChanged() {
            if (colorIndex == 0) {
                image.setImageResource(R.drawable.colorpink);
                label.setText(R.string.pink);
            } else if (colorIndex == 1) {
                image.setImageResource(R.drawable.colorred);
                label.setText(R.string.red);
            } else if (colorIndex == 2) {
                image.setImageResource(R.drawable.coloryellow);
                label.setText(R.string.yellow);
            } else if (colorIndex == 3) {
                image.setImageResource(R.drawable.colorgreen);
                label.setText(R.string.green);
            } else if (colorIndex == 4) {
                image.setImageResource(R.drawable.colorcyan);
                label.setText(R.string.cyan);
            } else if (colorIndex == 5) {
                image.setImageResource(R.drawable.colorblue);
                label.setText(R.string.blue);
            }
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
}
