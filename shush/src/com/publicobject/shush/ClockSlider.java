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
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A slider around a circle, to select a time between now and 12 hours from now.
 */
final class ClockSlider extends View {

    private static final int MAX_SIZE = 200;
    private static final int INSETS = 6;
    private static final int MINUTES_PER_HALF_DAY = 720;

    private int width;
    private int height;
    private int centerX;
    private int centerY;
    private int diameter;
    private RectF outerCircle;
    private Path clip;

    private Paint lightGrey = new Paint();
    private Paint pink = new Paint();
    private Paint white = new Paint();
    private Paint duration = new Paint();
    private Paint durationUnits = new Paint();
    private Paint unshushTime = new Paint();

    private Calendar start = new GregorianCalendar();
    private int startAngle = 0;
    private Calendar end = new GregorianCalendar();
    private int sweepAngle = 0;

    public ClockSlider(Context context) {
        super(context);

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
            outerCircle = new RectF(left, top, left + diameter, top + diameter);

            int innerDiameter = diameter - thickness * 2;
            RectF innerCircle = new RectF(left + thickness, top + thickness,
                    left + thickness + innerDiameter, top + thickness + innerDiameter);

            clip = new Path();
            clip.addRect(outerCircle, Path.Direction.CW);
            clip.addOval(innerCircle, Path.Direction.CCW);
        }

        drawClock(canvas);
        drawText(canvas);
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

    public int getSweepAngle() {
        return sweepAngle;
    }

    public void setSweepAngle(int angle) {
        if (angle == sweepAngle) {
            return; // avoid unnecessary repaints
        }
        sweepAngle = angle;
        end.setTimeInMillis(start.getTimeInMillis() + (sweepAngle * 2 * 60 * 1000L));
        postInvalidate();
    }

    public Date getEnd() {
        return end.getTime();
    }

    /**
     * Draw a circle and an arc of the selected duration from start thru end.
     */
    private void drawClock(Canvas canvas) {
        canvas.save();
        canvas.clipPath(clip);
        canvas.drawOval(outerCircle, lightGrey);
        canvas.drawArc(outerCircle, startAngle, sweepAngle, true, pink);
        canvas.drawArc(outerCircle, startAngle + sweepAngle - 1, 2, true, white);
        canvas.restore();
    }

    /**
     * Write labels in the middle of the circle like so:
     *
     *    2 1/2
     *    hours
     *  10:15 PM
     */
    private void drawText(Canvas canvas) {
        int halfHours = sweepAngle / 15;

        String durationText;
        String durationUnitsText;
        long timeInMillis = end.getTimeInMillis();
        String onAtText = DateUtils.formatSameDayTime(timeInMillis, timeInMillis,
                DateFormat.SHORT, DateFormat.SHORT).toString();

        String half = "\u00BD"; // pretty unicode 1/2
        if (halfHours == 1) {
            durationText = half;
            durationUnitsText = "hour";
        } else if (halfHours == 2) {
            durationText = "1";
            durationUnitsText = "hour";
        } else if (halfHours % 2 == 1) {
            durationText = (halfHours / 2) + half;
            durationUnitsText = "hours";
        } else {
            durationText = String.valueOf(halfHours / 2);
            durationUnitsText = "hours";
        }

        int diameterSize = diameter / 3;
        int durationUnitsSize = diameter / 10;
        int unshushTimeSize = diameter / 8;
        int gap = diameter / 30;

        duration.setTextSize(diameterSize);
        durationUnits.setTextSize(durationUnitsSize);
        unshushTime.setTextSize(unshushTimeSize);

        int y = centerY - gap;
        canvas.drawText(durationText, centerX, y, duration);
        y += durationUnitsSize;
        y += gap;
        canvas.drawText(durationUnitsText, centerX, y, durationUnits);
        y += unshushTimeSize;
        y += gap;
        canvas.drawText(onAtText, centerX, y, unshushTime);
    }

    /**
     * Accept a touches near the circle's edge, translate it to an angle, and
     * update the sweep angle.
     */
    @Override public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        // ignore the touch if it's too far from the circle
        int distanceFromCenterX = centerX - touchX;
        int distanceFromCenterY = centerY - touchY;
        int distanceFromCenterSquared = distanceFromCenterX * distanceFromCenterX
                + distanceFromCenterY * distanceFromCenterY;
        float maxDistance = (diameter * 1.3f) / 2;
        float minDistance = (diameter * 0.6f) / 2;
        if (distanceFromCenterSquared < (minDistance * minDistance)
                || distanceFromCenterSquared > (maxDistance * maxDistance)) {
            return false;
        }

        int angle = pointToAngle(touchX, touchY);

        /*
         * Convert the angle into a sweep angle. The sweep angle is a positive
         * angle between the start angle and the touched angle.
         */
        angle = 360 + angle - startAngle;
        angle = roundToNearest15Degrees(angle);
        if (angle > 360) {
            angle = angle - 360; // avoid mod because we prefer 360 over 0
        }
        setSweepAngle(angle);

        return true;
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, Math.min(height, MAX_SIZE));
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
     * Rounds the angle to the nearest 15 degrees, which equals 30 minutes on
     * a clock. Not strictly necessary, but it discourages fat-fingered users
     * from being frustrated when trying to select a fine-grained period.
     */
    private int roundToNearest15Degrees(int angle) {
        return ((angle + 8) / 15) * 15;
    }
}
