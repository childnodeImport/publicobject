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

package com.publicobject.shush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A slider around a circle, to select a time between now and 12 hours from now.
 */
final class ClockSlider extends View {
    /* Display modes */
    public static final int CLOCK_SLIDER = 1;
    public static final int VOLUME_SLIDER = 2;
    public static final int VIBRATE_PICKER = 3;

    private static final int INSETS = 6;
    private static final int MINUTES_PER_HALF_DAY = 720;

    private RingerMutedDialog ringerMutedDialog;

    private int width;
    private int height;
    private int centerX;
    private int centerY;
    private int diameter;
    private RectF outerCircle;
    private RectF innerCircle;
    private RectF buttonCircle;
    private final Path path = new Path();

    private RectF smallVolume;
    private RectF smallVolumeTouchRegion;
    private RectF largeVolume;
    private int displayMode = CLOCK_SLIDER;
    /** Volume to restore to; between 0.0 and 1.0 */
    private float volume = 0.8f;

    /** Vibrate mode */
    private boolean vibrateNow = true;
    private boolean vibrateLater = true;
    private RectF smallVibrate;
    private RectF smallVibrateTouchRegion;

    private Paint lightGrey = new Paint();
    private Paint pink = new Paint();
    private Paint white = new Paint();
    private Paint duration = new Paint();
    private Paint durationUnits = new Paint();
    private Paint unshushTime = new Paint();
    private Paint percentPaint = new Paint();
    private Paint buttonCirclePaint = new Paint();

    private Calendar start = new GregorianCalendar();
    private int startAngle = 0;
    private Calendar end = new GregorianCalendar();

    /** minutes to shush. */
    private int minutes = 0;
    private boolean upPushed;
    private boolean downPushed;

    public ClockSlider(Context context, AttributeSet attrs) {
        super(context, attrs);

        lightGrey.setColor(Color.rgb(115, 115, 115));
        lightGrey.setAntiAlias(true);
        pink.setColor(Color.rgb(255, 0, 165));
        pink.setAntiAlias(true);
        white.setColor(Color.WHITE);
        white.setAntiAlias(true);
        duration.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        duration.setSubpixelText(true);
        duration.setAntiAlias(true);
        duration.setColor(Color.WHITE);
        duration.setTextAlign(Paint.Align.CENTER);
        durationUnits = new Paint(duration);
        durationUnits.setTypeface(Typeface.SANS_SERIF);
        unshushTime = new Paint(duration);
        unshushTime.setColor(lightGrey.getColor());
        percentPaint = new Paint(duration);
        percentPaint.setTextAlign(Paint.Align.LEFT);
        percentPaint.setColor(lightGrey.getColor());
        buttonCirclePaint.setColor(Color.argb(102, 115, 115, 115));
        buttonCirclePaint.setAntiAlias(true);
    }

    public void setColor(int color) {
        pink.setColor(color);
    }

    public void setRingerMutedDialog(RingerMutedDialog ringerMutedDialog) {
        this.ringerMutedDialog = ringerMutedDialog;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() != width || getHeight() != height) {
            width = getWidth();
            height = getHeight();
            centerX = width / 2;
            centerY = height / 2;

            diameter = Math.min(width, height) - (2 * INSETS);
            int thickness = diameter / 15;

            int left = (width - diameter) / 2;
            int top = (height - diameter) / 2;
            int bottom = top + diameter;
            int right = left + diameter;
            outerCircle = new RectF(left, top, right, bottom);

            int innerDiameter = diameter - thickness * 2;
            innerCircle = new RectF(left + thickness, top + thickness,
                    left + thickness + innerDiameter, top + thickness + innerDiameter);

            int offset = thickness * 2;
            int buttonDiameter = diameter - offset * 2;
            buttonCircle = new RectF(left + offset, top + offset,
                    left + offset + buttonDiameter, top + offset + buttonDiameter);

            // the large volume triangle
            int volumeLeft = Math.max(INSETS * 2, centerX - diameter);
            int volumeRight = Math.min(width - INSETS * 2, centerX + diameter);
            int volumeHeight = (volumeRight - volumeLeft) / 2;
            largeVolume = new RectF(volumeLeft, bottom - volumeHeight, volumeRight, bottom);

            // the small volume triangle fits into the large triangle's bottom left corner
            float smallVolumeWidth = diameter * 0.25f;
            float smallVolumeHeight = smallVolumeWidth / largeVolume.width() * largeVolume.height();
            smallVolume = new RectF(volumeLeft,
                    bottom - smallVolumeHeight,
                    volumeLeft + smallVolumeWidth,
                    bottom);
            smallVibrate = new RectF(volumeRight - smallVolumeWidth,
                    bottom - smallVolumeHeight,
                    volumeRight,
                    bottom);

            // the small volume touch region is slightly bigger than the triangle
            smallVolumeTouchRegion = new RectF(
                    smallVolume.left   - smallVolume.width() * 0.25f,
                    smallVolume.top    - smallVolume.height() * 0.50f,
                    smallVolume.right  + smallVolume.width() * 0.10f,
                    smallVolume.bottom + smallVolume.height() * 0.25f);
            smallVibrateTouchRegion = new RectF(
                    smallVibrate.left   - smallVibrate.width() * 0.10f,
                    smallVibrate.top    - smallVibrate.height() * 0.50f,
                    smallVibrate.right  + smallVibrate.width() * 0.25f,
                    smallVibrate.bottom + smallVibrate.height() * 0.25f);

            duration.setTextSize(diameter * 0.32f);
            durationUnits.setTextSize(diameter * 0.10f);
            unshushTime.setTextSize(diameter * 0.13f);
            percentPaint.setTextSize(diameter * 0.08f);
        }

        if (displayMode == CLOCK_SLIDER) {
            drawClock(canvas);
            drawClockTextAndButtons(canvas);
            drawVolumeSlider(canvas, smallVolume);
            drawDevice(canvas, smallVibrate.centerX(), smallVibrate.centerY(),
                    smallVibrate.height(), true, vibrateNow, false, true);
            drawDevice(canvas, smallVibrate.centerX(), smallVibrate.centerY(),
                    smallVibrate.height(), false, vibrateLater, false, true);
        } else if (displayMode == VOLUME_SLIDER) {
            drawVolumeSlider(canvas, largeVolume);
        } else if (displayMode == VIBRATE_PICKER) {
            drawVibratePicker(canvas);
        } else {
            throw new AssertionError();
        }
    }

    public Date getStart() {
        return start.getTime();
    }

    public void setStart(Date now) {
        start.setTime(now);
        int minuteOfHalfDay = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE);
        if (minuteOfHalfDay > MINUTES_PER_HALF_DAY) {
            minuteOfHalfDay -= MINUTES_PER_HALF_DAY;
        }
        int angle = minuteOfHalfDay / 2; // 720 minutes per half-day -> 360 degrees per circle
        angle += 270; // clocks start at 12:00, but our angles start at 3:00
        startAngle = angle % 360;
        postInvalidate();
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        if (minutes == this.minutes) {
            return; // avoid unnecessary repaints
        }
        this.minutes = minutes;
        end.setTimeInMillis(start.getTimeInMillis() + (this.minutes * 60 * 1000L));
        postInvalidate();
    }

    public float getVolume() {
        return volume;
    }

    public void setVibrateNow(boolean vibrateNow) {
        setVolumeAndVibrate(displayMode, volume, vibrateNow, vibrateLater);
    }

    public void setVibrateLater(boolean vibrateLater) {
        setVolumeAndVibrate(displayMode, volume, vibrateNow, vibrateLater);
    }

    public void setVolume(float volume) {
        setVolumeAndVibrate(displayMode, volume, vibrateNow, vibrateLater);
    }

    private void setVolumeAndVibrate(int displayMode, float volume,
            boolean vibrateNow, boolean vibrateLater) {
        if (displayMode == this.displayMode
                && volume == this.volume
                && vibrateNow == this.vibrateNow
                && vibrateLater == this.vibrateLater) {
            return; // avoid unnecessary repaints
        }
        if (displayMode != this.displayMode
                || vibrateNow != this.vibrateNow
                || vibrateLater != this.vibrateLater) {
            this.ringerMutedDialog.modeChanged(displayMode, vibrateNow, vibrateLater);
        }
        this.displayMode = displayMode;
        this.volume = volume;
        this.vibrateNow = vibrateNow;
        this.vibrateLater = vibrateLater;
        postInvalidate();
    }

    public Date getEnd() {
        return end.getTime();
    }

    /**
     * Draw a circle and an arc of the selected duration from start thru end.
     */
    private void drawClock(Canvas canvas) {
        int sweepDegrees = (minutes / 2) - 1;

        // the colored "filled" part of the circle
        drawArc(canvas, startAngle, sweepDegrees, pink);

        // the white selected part of the circle
        drawArc(canvas, startAngle + sweepDegrees, 2, white);

        // the grey empty part of the circle
        drawArc(canvas, startAngle + sweepDegrees + 2, 360 - sweepDegrees - 2, lightGrey);
    }

    private void drawArc(Canvas canvas, int startAngle, int sweepDegrees, Paint paint) {
        if (sweepDegrees <= 0) {
            return;
        }

        path.reset();
        path.arcTo(outerCircle, startAngle, sweepDegrees);
        path.arcTo(innerCircle, startAngle + sweepDegrees, -sweepDegrees);
        path.close();
        canvas.drawPath(path, paint);
    }

    /**
     * This draws a triangle showing the current volume level. The triangle may
     * be small for an icon button or large for an active slider. At min volume
     * the triangle is mostly grey; at max volume it is all pink.
     */
    private void drawVolumeSlider(Canvas canvas, RectF bound) {
        int percent = (int) (volume * 100);
        float textX = largeVolume.left;
        float textY = largeVolume.bottom - smallVolume.height();
        canvas.drawText(percent + "%", textX, textY, percentPaint);

        drawTriangleSlice(canvas, bound, 0.0f, volume, pink);
        drawTriangleSlice(canvas, bound, volume, 1.0f, lightGrey);

        float linePercent = 3.0f / bound.width(); // percent of bound that's 3 dips wide
        float top = Math.min(volume + (linePercent / 2), 1.0f);
        float bottom = top - linePercent;
        drawTriangleSlice(canvas, bound, bottom, top, white);
    }

    /**
     * Draws a vertical slice of a right triangle whose slope goes from left to
     * right in bound. Left and right are lower and upper fractions of the
     * drawn area of bound.
     */
    private void drawTriangleSlice(Canvas canvas, RectF bound, float from, float to, Paint paint) {

        /*
         * Fill the middle slice of the triangle bounded by 'bound':
         *
         *        /|
         *       / |
         *      /| |
         *     / | |
         *    /| | |
         *   /_|_|_|
         */

        float left = bound.left + (from * bound.width());
        float right = bound.left + (to * bound.width());
        float levelTwo = bound.bottom - (from * bound.height());
        float levelThree = bound.bottom - (to * bound.height());

        path.reset();
        path.moveTo(left, bound.bottom);
        path.lineTo(left, levelTwo);
        path.lineTo(right, levelThree);
        path.lineTo(right, bound.bottom);
        path.close();
        canvas.drawPath(path, paint);
    }

    /**
     * Write labels in the middle of the circle like so:
     *
     *    2 1/2
     *    hours
     *  10:15 PM
     */
    private void drawClockTextAndButtons(Canvas canvas) {
        // up/down button backgrounds
        if (upPushed) {
            canvas.drawArc(buttonCircle, 270, 180, true, buttonCirclePaint);
        }
        if (downPushed) {
            canvas.drawArc(buttonCircle, 90, 180, true, buttonCirclePaint);
        }

        String durationText;
        int durationUnitsId;
        long timeInMillis = end.getTimeInMillis();
        String onAtText = DateFormat.getTimeFormat(getContext()).format(timeInMillis);
        if (minutes < 60) {
            durationText = Integer.toString(minutes);
            durationUnitsId = R.string.minutes;
        } else if (minutes == 60) {
            durationText = "1";
            durationUnitsId = R.string.hour;
        } else if (minutes % 60 == 0) {
            durationText = Integer.toString(minutes / 60);
            durationUnitsId = R.string.hours;
        } else if (minutes % 60 == 15) {
            durationText = minutes / 60 + "\u00BC"; // 1/4
            durationUnitsId = R.string.hours;
        } else if (minutes % 60 == 30) {
            durationText = minutes / 60 + "\u00BD"; // 1/2
            durationUnitsId = R.string.hours;
        } else if (minutes % 60 == 45) {
            durationText = minutes / 60 + "\u00BE"; // 3/4
            durationUnitsId = R.string.hours;
        } else {
            throw new AssertionError();
        }
        String durationUnitsText = getResources().getString(durationUnitsId);
        canvas.drawText(durationText,      centerX, centerY - (diameter * 0.08f), duration);
        canvas.drawText(durationUnitsText, centerX, centerY + (diameter * 0.06f), durationUnits);
        canvas.drawText(onAtText,          centerX, centerY + (diameter * 0.25f), unshushTime);

        // up/down buttons
        Paint downPaint = downPushed ? white : lightGrey;
        canvas.drawRect(centerX - diameter * 0.32f, centerY - diameter * 0.01f,
                        centerX - diameter * 0.22f, centerY + diameter * 0.01f, downPaint);
        Paint upPaint = upPushed ? white : lightGrey;
        canvas.drawRect(centerX + diameter * 0.22f, centerY - diameter * 0.01f,
                        centerX + diameter * 0.32f, centerY + diameter * 0.01f, upPaint);
        canvas.drawRect(centerX + diameter * 0.26f, centerY - diameter * 0.05f,
                        centerX + diameter * 0.28f, centerY + diameter * 0.05f, upPaint);
    }

    /**
     * Accept a touches near the circle's edge, translate it to an angle, and
     * update the sweep angle.
     */
    @Override public boolean onTouchEvent(MotionEvent event) {
        if (outerCircle == null) {
            return true; // ignore all events until the canvas is drawn
        }

        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        // Handle volume slider and vibrate picker.
        int newDisplayMode = displayMode;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (smallVolumeTouchRegion.contains(touchX, touchY)) {
                newDisplayMode = VOLUME_SLIDER;
            } else if (smallVibrateTouchRegion.contains(touchX, touchY)) {
                newDisplayMode = VIBRATE_PICKER;
            }
        }
        if (newDisplayMode == VOLUME_SLIDER) {
            float newVolume = toVolume((touchX - largeVolume.left) / largeVolume.width());
            if (event.getAction() == MotionEvent.ACTION_UP) {
                newDisplayMode = CLOCK_SLIDER;
            }
            setVolumeAndVibrate(newDisplayMode, newVolume, vibrateNow, vibrateLater);
            return true;
        }
        if (newDisplayMode == VIBRATE_PICKER) {
            boolean vibrateNow = touchX < width / 2;
            boolean vibrateLater = touchY < height / 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                newDisplayMode = CLOCK_SLIDER;
            }
            setVolumeAndVibrate(newDisplayMode, volume, vibrateNow, vibrateLater);
            return true;
        }

        upPushed = false;
        downPushed = false;
        int distanceFromCenterX = centerX - touchX;
        int distanceFromCenterY = centerY - touchY;
        int distanceFromCenterSquared = distanceFromCenterX * distanceFromCenterX
                + distanceFromCenterY * distanceFromCenterY;
        float maxSlider = (diameter * 1.3f) / 2;
        float maxUpDown = (diameter * 0.8f) / 2;

        // handle increment/decrement
        if (distanceFromCenterSquared < (maxUpDown * maxUpDown)) {
            boolean up = touchX > centerX;

            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (up) {
                    upPushed = true;
                } else {
                    downPushed = true;
                }
                postInvalidate();
                return true;
            }

            int angle = up ? (15 + minutes) : (705 + minutes);
            if (angle > 720) {
                angle -= 720;
            }
            setMinutes(angle);
            return true;

        // if it's on the slider, handle that
        } else if (distanceFromCenterSquared < (maxSlider * maxSlider)) {
            int angle = pointToAngle(touchX, touchY);
            /*
             * Convert the angle into a sweep angle. The sweep angle is a positive
             * angle between the start angle and the touched angle.
             */
            angle = 360 + angle - startAngle;
            int angleX2 = angle * 2;
            angleX2 = roundToNearest15(angleX2);
            if (angleX2 > 720) {
                angleX2 = angleX2 - 720; // avoid mod because we prefer 720 over 0
            }
            setMinutes(angleX2);
            return true;

        } else {
            return false;
        }
    }

    /**
     * Returns a volume fraction that's in permitted bounds. We don't let the
     * volume go too low (what would be the point!) or above 100%.
     */
    private float toVolume(float rawFraction) {
        if (rawFraction < 0.1) {
            return 0.1f;
        }
        if (rawFraction > 1.0) {
            return 1.0f;
        }
        return rawFraction;
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int maxHeight = (int) Math.min(height, width * 0.7);
        setMeasuredDimension(width, maxHeight);
    }

    /**
     * Returns the number of degrees (0-359) for the given point, such that
     * 3pm is 0 and 9pm is 180.
     */
    private int pointToAngle(int x, int y) {

        /* Get the angle from a triangle by dividing opposite by adjacent
         * and taking the atan. This code is careful not to divide by 0.
         *
         *
         *      adj | opp
         *          |
         * opp +180 | +270 adj
         * _________|_________
         *          |
         * adj  +90 | +0   opp
         *          |
         *      opp | adj
         *
         */

        if (x >= centerX && y < centerY) {
            double opp = x - centerX;
            double adj = centerY - y;
            return 270 + (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x > centerX && y >= centerY) {
            double opp = y - centerY;
            double adj = x - centerX;
            return (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x <= centerX && y > centerY) {
            double opp = centerX - x;
            double adj = y - centerY;
            return 90 + (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x < centerX && y <= centerY) {
            double opp = centerY - y;
            double adj = centerX - x;
            return 180 + (int) Math.toDegrees(Math.atan(opp / adj));
        }

        throw new IllegalArgumentException();
    }

    /**
     * Rounds the angle to the nearest 7.5 degrees, which equals 15 minutes on
     * a clock. Not strictly necessary, but it discourages fat-fingered users
     * from being frustrated when trying to select a fine-grained period.
     */
    private int roundToNearest15(int angleX2) {
        return ((angleX2 + 8) / 15) * 15;
    }

    private void drawVibratePicker(Canvas canvas) {
        boolean onOn = vibrateNow & vibrateLater;
        boolean offOn = !vibrateNow & vibrateLater;
        boolean onOff = vibrateNow & !vibrateLater;
        boolean offOff = !vibrateNow & !vibrateLater;

        int selectedLeft = vibrateNow ? 0 : width / 2;
        int selectedTop = vibrateLater ? 0 : height / 2;
        canvas.drawRect(selectedLeft, selectedTop,
                selectedLeft + width / 2, selectedTop + height / 2, buttonCirclePaint);

        float deviceHeight = height * 0.2f;
        // top left ON ON
        drawDevice(canvas, width * 0.25f, height * 0.25f, deviceHeight, true, true, onOn, true);
        drawDevice(canvas, width * 0.25f, height * 0.25f, deviceHeight, false, true, onOn, true);
        // top right OFF ON
        drawDevice(canvas, width * 0.75f, height * 0.25f, deviceHeight, true, false, offOn, true);
        drawDevice(canvas, width * 0.75f, height * 0.25f, deviceHeight, false, true, offOn, true);
        // bottom left ON OFF
        drawDevice(canvas, width * 0.25f, height * 0.75f, deviceHeight, true, true, onOff, true);
        drawDevice(canvas, width * 0.25f, height * 0.75f, deviceHeight, false, false, onOff, true);
        // bottom right, OFF OFF
        drawDevice(canvas, width * 0.75f, height * 0.75f, deviceHeight, true, false, offOff, true);
        drawDevice(canvas, width * 0.75f, height * 0.75f, deviceHeight, false, false, offOff, true);
    }

    private void drawDevice(Canvas canvas, float centerX, float centerY, float height,
            boolean now, boolean vibrate, boolean selected, boolean sideBySide) {
        canvas.save();

        int width = (int) (height * 0.7f);
        int bezel = (int) (height * 0.08f);
        int left = (int) (now ? (centerX - width - bezel) : (centerX + bezel));
        int right = left + width;
        int top = (int) (centerY - height / 2);
        int bottom = (int) (centerY + height / 2);

        // If we're vibrating, rotate the device.
        if (vibrate) {
            // If we're right beside another device, move away from center as to not overlap.
            if (sideBySide) {
                canvas.translate(now ? -height * 0.18f : height * 0.18f, 0f);
            }
            canvas.rotate(27f, left + width / 2, centerY);
        }

        Paint paint = selected ? white : lightGrey;
        canvas.drawRect(left, top, left + bezel, bottom, paint); // left edge
        canvas.drawRect(right - bezel, top, right, bottom, paint); // right edge
        canvas.drawRect(left, top, right, top + bezel, paint); // top edge
        canvas.drawRect(left, bottom - bezel, right, bottom, paint); // bottom edge

        if (vibrate) {
            canvas.drawRect(left - 4 * bezel, top + 3 * bezel,
                    left - 3 * bezel, top + 7 * bezel, paint);
            canvas.drawRect(left - 2 * bezel, top + 1 * bezel,
                    left - 1 * bezel, top + 9 * bezel, paint);
            canvas.drawRect(right + 3 * bezel, bottom - 7 * bezel,
                    right + 4 * bezel, bottom - 3 * bezel, paint);
            canvas.drawRect(right + 1 * bezel, bottom - 9 * bezel,
                    right + 2 * bezel, bottom - 1 * bezel, paint);
        }

        canvas.restore();
    }
}
