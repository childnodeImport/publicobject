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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * A dialog that explains how Play/Pause works.
 */
public final class Welcome extends Activity implements OnClickListener, OnCancelListener {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(R.drawable.icon)
                .setTitle("Play/Pause Widget")
                .setMessage("Add the widget to your home screen. Touching it toggles "
                        + "between play and pause in your current media player.")
                .setOnCancelListener(this)
                .setPositiveButton("Sweet!", this)
                .create()
                .show();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }

    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }
}
