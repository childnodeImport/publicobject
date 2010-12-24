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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import java.util.Arrays;

public class PlayPauseWidgetProvider extends AppWidgetProvider {

    @Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            Intent intent = new Intent(context, OnPlayPause.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playpause_appwidget);
            views.setOnClickPendingIntent(R.id.button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            // TODO: what's this?
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }


        Log.i("PlayPause", "onUpdate: " + Arrays.toString(appWidgetIds));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override public void onDeleted(Context context, int[] appWidgetIds) {
        Log.i("PlayPause", "onDeleted: " + Arrays.toString(appWidgetIds));
        super.onDeleted(context, appWidgetIds);
    }

    @Override public void onEnabled(Context context) {
        Log.i("PlayPause", "onEnabled");
        super.onEnabled(context);
    }

    @Override public void onDisabled(Context context) {
        Log.i("PlayPause", "onDisabled");
        super.onDisabled(context);
    }
}
