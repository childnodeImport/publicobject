/**
 * Copyright (C) 2011 Jesse Wilson
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.text.format.DateFormat;

/**
 * Shows a message like "Ringer muted til 2pm' in the status bar. Clicking on
 * this notification immediately restores the ringer.
 */
final class RingerMutedNotification {
    private static final int NOTIFICATION_ID = 1;

    public static void show(Context context, long onTime, PendingIntent ringerOnIntent) {
        String message = String.format(context.getString(R.string.ringerShushedUntil),
                DateFormat.getTimeFormat(context).format(onTime));

        Notification notification = new Notification();
        notification.icon = R.drawable.notification;
        notification.tickerText = message;
        notification.flags |= Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT
                | Notification.FLAG_NO_CLEAR;
        notification.setLatestEventInfo(context, message, context.getString(R.string.turnItOnNow),
                ringerOnIntent);
        getNotificationManager(context).notify(NOTIFICATION_ID, notification);
    }

    public static void dismiss(Context context) {
        getNotificationManager(context).cancel(NOTIFICATION_ID);
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
