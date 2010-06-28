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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static android.media.AudioManager.EXTRA_RINGER_MODE;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.RINGER_MODE_VIBRATE;

/**
 * Upon ringer mode changing (a broadcast intent), show the ringer muted dialog (an activity intent).
 */
public class OnRingerMuted extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        int newRingerMode = intent.getIntExtra(EXTRA_RINGER_MODE, -1);
        if (RINGER_MODE_SILENT == newRingerMode || RINGER_MODE_VIBRATE == newRingerMode) {
            Intent showRingerMutedDialog = new Intent();
            showRingerMutedDialog.setClass(context, RingerMutedDialog.class);
            showRingerMutedDialog.setAction(RingerMutedDialog.class.getName());
            showRingerMutedDialog.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(showRingerMutedDialog);
        }
    }
}
