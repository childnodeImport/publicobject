/**
 * Copyright (C) 2012 Jesse Wilson
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

package com.publicobject.rounds;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

/**
 * A black rectangle with a thin border at the bottom.
 */
public final class ActionBarBackground extends Drawable {
    private final Paint fill = new Paint();
    private final Paint line = new Paint();
    private final int width;

    public ActionBarBackground(Resources resources) {
        fill.setColor(Color.BLACK);
        width = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                1.5f, resources.getDisplayMetrics())); // 2 pixels before scaling
    }

    @Override public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom - width, fill);
        canvas.drawRect(bounds.left, bounds.bottom - width, bounds.right, bounds.bottom, line);
    }

    public void setColor(int color) {
        line.setColor(color);
        invalidateSelf();
    }

    @Override public void setAlpha(int alpha) {
    }

    @Override public void setColorFilter(ColorFilter cf) {
    }

    @Override public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
