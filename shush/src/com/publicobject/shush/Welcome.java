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
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A dialog that explains how Shush works.
 */
public final class Welcome extends Activity
        implements View.OnClickListener, DialogInterface.OnClickListener, OnCancelListener {
    private boolean notifications = true;
    private int color = 0;

    private ImageView colorsImage;
    private TextView colorsLabel;
    private ImageView notificationsImage;
    private TextView notificationsLabel;

    @Override protected void onResume() {
        super.onResume();

        View view = LayoutInflater.from(this).inflate(R.layout.welcome, null);
        colorsImage = (ImageView) view.findViewById(R.id.colorsImage);
        colorsLabel = (TextView) view.findViewById(R.id.colorsLabel);
        notificationsImage = (ImageView) view.findViewById(R.id.notificationsImage);
        notificationsLabel = (TextView) view.findViewById(R.id.notificationsLabel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(R.drawable.shush)
                .setView(view)
                .setTitle(R.string.title)
                .setOnCancelListener(this)
                .setPositiveButton(R.string.okay, this)
                .create();
        dialog.show();

        colorsImage.setOnClickListener(this);
        colorsLabel.setOnClickListener(this);
        notificationsImage.setOnClickListener(this);
        notificationsLabel.setOnClickListener(this);
    }

    @Override protected void onPause() {
        colorsImage = null;
        colorsLabel = null;
        notificationsImage = null;
        notificationsLabel = null;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }

    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.notificationsImage || id == R.id.notificationsLabel) {
            notifications = !notifications;

            if (notifications) {
                notificationsImage.setImageResource(R.drawable.notifications_on);
                notificationsLabel.setText(R.string.notificationsOn);
            } else {
                notificationsImage.setImageResource(R.drawable.notifications_off);
                notificationsLabel.setText(R.string.notificationsOff);
            }
        } else if (id == R.id.colorsImage|| id == R.id.colorsLabel) {
            color = (color + 1) % 6;

            if (color == 0) {
                colorsImage.setImageResource(R.drawable.colorpink);
                colorsLabel.setText(R.string.pink);
            } else if (color == 1) {
                colorsImage.setImageResource(R.drawable.colorred);
                colorsLabel.setText(R.string.red);
            } else if (color == 2) {
                colorsImage.setImageResource(R.drawable.coloryellow);
                colorsLabel.setText(R.string.yellow);
            } else if (color == 3) {
                colorsImage.setImageResource(R.drawable.colorgreen);
                colorsLabel.setText(R.string.green);
            } else if (color == 4) {
                colorsImage.setImageResource(R.drawable.colorcyan);
                colorsLabel.setText(R.string.cyan);
            } else if (color == 5) {
                colorsImage.setImageResource(R.drawable.colorblue);
                colorsLabel.setText(R.string.blue);
            }
        }
    }
}
