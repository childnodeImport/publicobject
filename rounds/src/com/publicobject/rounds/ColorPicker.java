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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * A color selection swatch with pop-up.
 */
public final class ColorPicker extends View {
    private static final int COLS = 4;
    private static final int ROWS = 4;

    private View placeholder;
    private Listener listener;

    private int width;
    private int height;

    private float side;
    private float stride;
    private float radius;
    private RectF button;
    private RectF[] swatches;
    private Paint[] swatchPaints;
    private Paint extraPaint;

    private boolean picking = false;
    private Paint selectedPaint;

    public ColorPicker(Context context, AttributeSet attrs, View placeholder, Listener listener) {
        super(context, attrs);
        this.placeholder = placeholder;
        this.listener = listener;

        swatchPaints = new Paint[ROWS * COLS];
        for (int i = 0; i < swatchPaints.length; i++) {
            swatchPaints[i] = new Paint();
            swatchPaints[i].setAntiAlias(true);
            swatchPaints[i].setColor(Colors.COLORS[i]);
        }

        extraPaint = new Paint();
        extraPaint.setAntiAlias(true);
        extraPaint.setColor(Color.WHITE);
        selectedPaint = extraPaint;
    }


    public void setColor(int color) {
        extraPaint.setColor(color);
        selectedPaint = extraPaint;
        postInvalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (width != getWidth() || height != getHeight()) {
            dimensionsChanged();
        }

        canvas.drawRoundRect(button, radius, radius, selectedPaint);
        if (picking) {
            for (int s = 0; s < swatches.length; s++) {
                canvas.drawRoundRect(swatches[s], radius, radius, swatchPaints[s]);
            }
        }
    }

    private void dimensionsChanged() {
        this.width = getWidth();
        this.height = getHeight();

        int[] xy = new int[2];
        placeholder.getLocationInWindow(xy);
        int placeholderX = xy[0];
        int placeholderY = xy[1];
        this.getLocationInWindow(xy);
        int meX = xy[0];
        int meY = xy[1];
        int left = placeholderX - meX;
        int top = placeholderY - meY;
        side = placeholder.getHeight(); // swatches are square
        stride = side + 10;
        radius = 0f;

        button = new RectF(left, top, left + side, top + side);
        makeSwatches();
    }

    private void makeSwatches() {
        swatches = new RectF[ROWS * COLS];
        float firstColumn = button.left - COLS * stride;
        float firstRow = button.top;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int index = r * COLS + c;
                float left = firstColumn + c * stride;
                float top = firstRow + r * stride;
                swatches[index] = new RectF(left, top, left + side, top + side);
            }
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (!picking) {
            if (event.getAction() != MotionEvent.ACTION_DOWN || !button.contains(x, y)) {
                return false;
            } else {
                listener.selecting();
                picking = true;
                postInvalidate();
                return true;
            }
        }

        Paint previous = selectedPaint;
        for (int s = 0; s < swatches.length; s++) {
            if (swatches[s].contains(x, y)) {
                selectedPaint = swatchPaints[s];
                break;
            }
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            listener.selected(selectedPaint.getColor());
            picking = false;
            postInvalidate();
            return true;
        }

        if (previous != selectedPaint) {
            postInvalidate();
        }

        return true;
    }

    public interface Listener {
        void selecting();
        void selected(int color);
    }
}
