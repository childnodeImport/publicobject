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
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A slider around a circle to select an integer value.
 */
public final class JogWheel extends View {
    private static final long MILLIS_PER_MINUTE = TimeUnit.MINUTES.toMillis(1);
    private static final long FRAMERATE = 1000 / 60; // 60 fps
    private static final long UPDATE_RPM_PERIOD = 1000 / 60; // 60 fps
    private static final long ELIMINATE_DURATION = 1000;
    private static final long NAME_DURATION = 1000;
    private static final int ELIMINATE_ROTATION = 720;
    private static final int INSETS = 6;

    /** Degrees between ticks at speedMultiplier==1.0 */
    private static final int TICK_DISTANCE = 15;

    /** Compute the speed out the most recent 10 frames; about 170ms */
    private static final int LOG_SIZE = 10;

    /** Degrees of space in the circle for the player's name. */
    private static final int NAME_GAP = 20;

    /** Size of the player name selection area, in degrees. */
    private static final int TOUCH_SLOP = 15;

    private final Path path = new Path();
    private final RpmComputer rpmComputer = new RpmComputer();
    private final PlayerSelector playerSelector = new PlayerSelector();

    private Game model;
    private Listener listener;

    // geometry
    private int width;
    private int height;
    private int diameter;
    private int centerX;
    private int centerY;
    private RectF circle0;
    private RectF circle1;
    private RectF circle2;
    private RectF circle3;
    private RectF circle4;

    /** Amount of rotation for player 0. */
    // TODO: vary this by the number of players
    private float baseAngle = 180 + 45;
    private float playerSlice;

    private int touchPlayer = -1;
    private float lastTouchDegrees = Float.NaN;
    private float value;
    private Paint[] playerPaints;

    /** revs per minute (possibly negative) */
    private float rpm;

    // palette
    private final Paint circle;
    private Paint roundScore;
    private Paint[][] evenTicks;
    private Paint[][] oddTicks;

    public JogWheel(Context context, AttributeSet attrs) {
        super(context, attrs);

        circle = new Paint();
        circle.setColor(Color.rgb(0x3b, 0x42, 0x42));
        circle.setAntiAlias(true);

        setModel(Game.SAMPLE);
        setListener(Listener.NULL);
    }

    public void setModel(Game model) {
        this.model = model;
        playerPaints = new Paint[model.playerCount()];
        playerSlice = 360f / model.playerCount();

        evenTicks = new Paint[model.playerCount()][];
        oddTicks = new Paint[model.playerCount()][];

        Typeface sansSerif = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        Typeface sansSerifBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

        roundScore = new Paint();
        roundScore.setTypeface(sansSerifBold);
        roundScore.setColor(Color.WHITE);
        roundScore.setSubpixelText(true);
        roundScore.setAntiAlias(true);
        roundScore.setTextAlign(Paint.Align.CENTER);
        roundScore.setTextSize(diameter * 0.07f);

        for (int p = 0; p < model.playerCount(); p++) {
            int color = model.playerColor(p);
            int r1 = (int) Math.min(255, Color.red(color) * 1.3);
            int g1 = (int) Math.min(255, Color.green(color) * 1.3);
            int b1 = (int) Math.min(255, Color.blue(color) * 1.3);
            int r2 = (int) Math.max(0, Color.red(color) * 0.7);
            int g2 = (int) Math.max(0, Color.green(color) * 0.7);
            int b2 = (int) Math.max(0, Color.blue(color) * 0.7);

            playerPaints[p] = new Paint(roundScore);
            playerPaints[p].setTypeface(sansSerif);
            playerPaints[p].setColor(color);

            evenTicks[p] = new Paint[10];
            oddTicks[p] = new Paint[10];

            for (int i = 0; i < evenTicks[p].length; i++) {
                evenTicks[p][i] = new Paint();
                oddTicks[p][i] = new Paint();
                int alpha = Math.max(0, 255 - i * 30);
                evenTicks[p][i].setColor(Color.argb(alpha, r1, g1, b1));
                oddTicks[p][i].setColor(Color.argb(alpha, r2, g2, b2));
                evenTicks[p][i].setAntiAlias(true);
                oddTicks[p][i].setAntiAlias(true);
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() != width || getHeight() != height) {
            dimensionsChanged();
        }

        long timestamp = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        if (playerSelector.draw(canvas, timestamp)) {
            return;
        }

        if (touchPlayer == -1) {
            drawInactive(canvas);
            return;
        }

        drawArc(canvas, 0, 180, playerPaints[touchPlayer]);
        drawArc(canvas, 180, 180, playerPaints[touchPlayer]);

        /*
         * Render major and minor tick marks for every 'TICK_DISTANCE' degrees.
         * This adjusts the ticks around the current touch position so the scale
         * can change without shifting the currently selected value.
         *
         * Which ticks are major and which are minor depends on 'value' (which
         * is a running total). Where they go depends on 'lastTouchDegrees'.
         */
        int tickBelow = ((int) value / TICK_DISTANCE) * TICK_DISTANCE;
        float speedMultiplier = getSpeedMultiplier();
        float tickBelowDegrees = lastTouchDegrees - (value - tickBelow) / speedMultiplier;
        float degreesBetweenTicks = TICK_DISTANCE / speedMultiplier;

        // draw ticks from the tick below to 180 degrees going clockwise
        int m = tickBelow / TICK_DISTANCE;
        for (float i = 0; i < 180; i += degreesBetweenTicks) {
            drawTick(canvas, i, m, tickBelowDegrees + i, degreesBetweenTicks, touchPlayer);
            m++;
        }
        // draw ticks from the tick beneath the tick below to 180 degrees going counter clockwise
        m = (tickBelow / TICK_DISTANCE) - 1;
        for (float i = degreesBetweenTicks; i < 180; i += degreesBetweenTicks) {
            drawTick(canvas, i, m, tickBelowDegrees - i, degreesBetweenTicks, touchPlayer);
            m--;
        }
    }

    /**
     * Draws the circle with no ongoing touch event.
     */
    private void drawInactive(Canvas canvas) {
        float baseline = centerY + playerPaints[0].getTextSize() * 0.3f;
        float playerSlice = 360f / model.playerCount();
        for (int p = 0; p < model.playerCount(); p++) {
            float playerAngle = playerToAngle(p);

            // draw the player's name
            canvas.save();
            if (playerAngle < 90 || playerAngle > 270) {
                playerPaints[p].setTextAlign(Paint.Align.RIGHT);
                canvas.rotate(playerAngle, centerX, centerY);
                canvas.drawText(model.playerName(p), circle0.right, baseline, playerPaints[p]);
            } else {
                playerPaints[p].setTextAlign(Paint.Align.LEFT);
                canvas.rotate(playerAngle - 180, centerX, centerY);
                canvas.drawText(model.playerName(p), circle0.left, baseline, playerPaints[p]);
            }
            canvas.restore();

            // draw the player's score
            canvas.save();
            playerPaints[p].setTextAlign(Paint.Align.CENTER);
            if (playerAngle < 180) {
                canvas.rotate(playerAngle - 90, centerX, centerY);
                canvas.drawText(Integer.toString(model.roundScore(p)),
                        centerX, circle4.bottom, roundScore);
            } else {
                canvas.rotate(playerAngle + 90, centerX, centerY);
                canvas.drawText(Integer.toString(model.roundScore(p)),
                        centerX, circle1.top, roundScore);
            }
            canvas.restore();

            drawArc(canvas, playerAngle + NAME_GAP / 2, playerSlice - NAME_GAP, circle);
        }
    }

    private float playerToAngle(int player) {
        return (baseAngle + player * playerSlice) % 360;
    }

    /**
     * Returns the player for {@code angle}, or -1 if that angle is too
     * far from any player.
     */
    private int angleToPlayer(float angle) {
        angle = (angle + 360) % 360;
        float sliceAngle = (angle + 360 - baseAngle) % 360;
        int candidatePlayer = Math.round(sliceAngle / playerSlice);
        float candidatePlayerAngle = playerToAngle(candidatePlayer);
        if (Math.abs(candidatePlayerAngle - angle) < TOUCH_SLOP) {
            return candidatePlayer % model.playerCount();
        }
        return -1; // touch is too far from the nearest player
    }

    private void drawArc(Canvas canvas, float startAngle, float sweepDegrees, Paint paint) {
        if (sweepDegrees <= 0) {
            return;
        }

        path.reset();
        path.arcTo(circle4, startAngle, sweepDegrees);
        path.arcTo(circle1, startAngle + sweepDegrees, -sweepDegrees);
        path.close();
        canvas.drawPath(path, paint);
    }

    /**
     * @param distance distance in degrees from the last touch. Must be < 180.
     * @param value the value for this tick. Used to determine if this is a
     */
    private void drawTick(Canvas canvas, float distance, int value, float angle, float sweep,
                          int player) {
        RectF outside = circle4;
        RectF inside = (value % 5 == 0) ? circle2 : circle3;
        Paint[] ticks = value % 2 == 0 ? evenTicks[player] : oddTicks[player];
        Paint paint = ticks[((int) (distance / 180 * ticks.length))];

        path.reset();
        path.arcTo(outside, angle, sweep);
        path.arcTo(inside, angle + sweep, -sweep);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void dimensionsChanged() {
        width = getWidth();
        height = getHeight();
        centerX = width / 2;
        centerY = height / 2;
        diameter = Math.min(width, height) - (2 * INSETS);

        circle0 = newCircle(0.08f);
        circle1 = newCircle(0.06f);
        circle2 = newCircle(0.04f);
        circle3 = newCircle(0.02f);
        circle4 = newCircle(0.00f);

        // force recompute text size
        setModel(model);
    }

    private RectF newCircle(float insetFraction) {
        int left = (width - diameter) / 2;
        int top = (height - diameter) / 2;
        float inset = diameter * insetFraction;
        float resultDiameter = diameter - 2 * inset;
        return new RectF(left + inset, top + inset,
                left + inset + resultDiameter, top + inset + resultDiameter);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (circle4 == null) {
            return false; // ignore all events until the canvas is drawn
        }
        if (touchPlayer == -1 && event.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (playerSelector.isActive()) {
            return false; // ignore all events while a player is being selected
        }

        float x = event.getX();
        float y = event.getY();
        double rads = Math.atan2(y - centerY, x - centerX);
        float touchDegrees = (float) Math.toDegrees(rads);

        float speedMultiplier = getSpeedMultiplier();
        float deltaTravel = Float.isNaN(speedMultiplier) || Float.isNaN(lastTouchDegrees)
                ? 0f
                : deltaDegrees(lastTouchDegrees, touchDegrees) * speedMultiplier;
        value += deltaTravel;

        switch (event.getAction()) {
        case MotionEvent.ACTION_CANCEL:
            rpmComputer.stop();
            listener.cancelled();
            value = 0f;
            lastTouchDegrees = Float.NaN;
            touchPlayer = -1;
            break;

        case MotionEvent.ACTION_UP:
            rpmComputer.stop();
            listener.selected(touchPlayer, valueToSelection(value));
            value = 0f;
            lastTouchDegrees = Float.NaN;
            touchPlayer = -1;
            break;

        case MotionEvent.ACTION_DOWN:
            /*
             * Center the first touch on "0", which is the center of the range
             * 0 < value < TICK_DISTANCE since we use floor() to map from
             * 'value' (degrees) to 'selection' (int).
             */
            touchPlayer = angleToPlayer(touchDegrees);
            if (touchPlayer == -1) {
                return false;
            }
            value = (model.roundScore(touchPlayer) + 0.5f) * TICK_DISTANCE;
            rpmComputer.run();
            // fall-through
        case MotionEvent.ACTION_MOVE:
            listener.selecting(touchPlayer, valueToSelection(value));
            lastTouchDegrees = touchDegrees;
            break;
        }

        postInvalidate();
        return true;
    }

    /**
     * Returns a selected value (like 1, 2, 3) from degrees of rotation.
     */
    private int valueToSelection(float degrees) {
        return (int) Math.floor(degrees / TICK_DISTANCE);
    }

    /**
     * Returns the current multiplier to convert the change in degrees to the
     * change in units. Always at least one.
     */
    private float getSpeedMultiplier() {
        if (Float.isNaN(rpm)) {
            return 1;
        }

        float scaleFactor = 0.05f;
        float speedMultiplier = Math.abs(rpm) * scaleFactor;
        return Math.max(1, speedMultiplier);
    }

    private float deltaDegrees(float oldDegrees, float newDegrees) {
        float deltaDegrees = newDegrees - oldDegrees;
        if (deltaDegrees > 180) {
            return deltaDegrees - 360;
        } else if (deltaDegrees < -180) {
            return deltaDegrees + 360;
        } else {
            return deltaDegrees;
        }
    }

    /**
     * @param players list of game players in order of elimination. The last
     *     player in the list will be selected.
     */
    public void selectPlayer(List<Integer> players) {
        this.playerSelector.players = players;
        this.playerSelector.startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        getHandler().post(playerSelector);
    }

    private final class PlayerSelector implements Runnable {
        private List<Integer> players;
        private long startTime;

        boolean isActive() {
            return players != null;
        }

        boolean draw(Canvas canvas, long timestamp) {
            long elapsed = timestamp - startTime;
            if (!isActive() || elapsed >= (ELIMINATE_DURATION + NAME_DURATION)) {
                players = null;
                return false;
            }
            if (elapsed > ELIMINATE_DURATION) {
                drawPick(canvas);
                return true;
            }

            // time to eliminate each player
            long perPlayerDuration = ELIMINATE_DURATION / (players.size() - 1);
            int eliminatedCount = (int) (elapsed / perPlayerDuration);

            // time spent performing the current elimination
            long eliminationElapsed = elapsed - (perPlayerDuration * eliminatedCount);
            float eliminationFraction = ((float) eliminationElapsed) / perPlayerDuration;

            int playersRemaining = players.size() - eliminatedCount;
            float baseSweep = 360 / playersRemaining;
            float eliminatedPlayerSweep = baseSweep * (1 - eliminationFraction);
            float remainingPlayerSweep = (360 - eliminatedPlayerSweep) / (playersRemaining - 1);

            float angle = baseAngle;
            for (int i = 0; i < eliminatedCount; i++) {
                angle += computeDeltaAngle(i, 360f / (players.size() - i), 0,
                        360f / (players.size() - i - 1));
            }
            angle += computeDeltaAngle(eliminatedCount, baseSweep,
                    eliminatedPlayerSweep, remainingPlayerSweep);
            angle += ((float) elapsed / ELIMINATE_DURATION) * ELIMINATE_ROTATION;

            drawPlayerArcs(canvas, eliminatedCount, eliminatedPlayerSweep,
                    remainingPlayerSweep, angle);
            return true;
        }

        /**
         * Returns the amount of rotation to player 0 to apply after reducing
         * the sweep of a player to {@code eliminatedPlayerSweep}.
         */
        private float computeDeltaAngle(int totalEliminated, float baseSweep,
                float eliminatedPlayerSweep, float remainingPlayerSweep) {
            float delta = -(baseSweep - eliminatedPlayerSweep) / 2;
            int eliminated = players.get(totalEliminated);
            int size = players.size();
            for (int i = (eliminated + 1) % size; i != 0; i = (i + 1) % size) {
                if (players.indexOf(i) > totalEliminated) {
                    delta += remainingPlayerSweep - baseSweep;
                }
            }
            return delta;
        }

        private void drawPlayerArcs(Canvas canvas, int eliminatedCount, float eliminatedPlayerSweep,
                float remainingPlayerSweep, float baseAngle) {
            for (int p = 0; p < players.size(); p++) {
                int player = players.indexOf(p);
                float sweep;
                if (player < eliminatedCount) {
                    continue;
                } else if (player == eliminatedCount) {
                    sweep = eliminatedPlayerSweep;
                } else {
                    sweep = remainingPlayerSweep;
                }
                drawArc(canvas, baseAngle, sweep, playerPaints[p]);
                baseAngle += sweep;
            }
        }

        private void drawPick(Canvas canvas) {
            int p = players.get(players.size() - 1);
            Paint paint = playerPaints[p];
            drawArc(canvas, 0, 180, paint);
            drawArc(canvas, 180, 180, paint);
            float baseline = centerY + paint.getTextSize() * 0.3f;
            canvas.drawText(model.playerName(p), circle0.centerX(), baseline, paint);
        }

        @Override public void run() {
            if (players != null) {
                invalidate();
                Handler handler = getHandler();
                if (handler != null) {
                    handler.postDelayed(this, FRAMERATE);
                }
            }
        }
    }

    public interface Listener {
        Listener NULL = new Listener() {
            @Override public void selecting(int player, int value) {
            }
            @Override public void selected(int player, int value) {
            }
            @Override public void cancelled() {
            }
        };

        void selecting(int player, int value);
        void selected(int player, int value);
        void cancelled();
    }

    /**
     * When run this computes the new current RPM and schedules an RPM update
     * for the following frame.
     */
    private class RpmComputer implements Runnable {
        /*
         * Circular logs containing timestamps and the corresponding touch angles
         * at those timestamps. The entry at logIndex is the oldest; the entry at
         * logIndex-1 is the most recent.
         */
        private int logIndex = 0;
        private long[] timeLog = new long[LOG_SIZE];
        private float[] degreesLog = new float[LOG_SIZE];
        {
            Arrays.fill(degreesLog, Float.NaN);
        }

        /**
         * Updates the RPM immediately and schedules further updates in the
         * future. Call {@code stop} to discontinue updates.
         */
        @Override public void run() {
            /*
             * Compute the new RPM by summing the deltas in the log. Each
             * iteration of the loop computes the delta between log i and i+1,
             * and adds it to the running total. If we haven't observed a full
             * set of frames then some iterations will not contribute to the
             * total.
             *
             * We cheat a bit by clobbering the oldest log with the newest log
             * before the loop starts. This permits us to handle LOG_SIZE+1
             * inputs with only LOG_SIZE array cells.
             */
            float lastDegrees = degreesLog[logIndex];
            float lastTime = timeLog[logIndex];
            degreesLog[logIndex] = lastTouchDegrees;
            timeLog[logIndex] = SystemClock.uptimeMillis();
            float totalDegrees = 0f;
            long totalTime = 0L;
            for (int j = 0; j < LOG_SIZE; j++) {
                int newIndex = (logIndex + j + 1) % LOG_SIZE;
                float newDegrees = degreesLog[newIndex];
                float newTime = timeLog[newIndex];

                if (!Float.isNaN(lastDegrees)) {
                    totalDegrees += deltaDegrees(lastDegrees, newDegrees);
                    totalTime += (newTime - lastTime);
                }

                lastDegrees = newDegrees;
                lastTime = newTime;
            }

            rpm = (totalTime != 0)
                    ? totalDegrees * MILLIS_PER_MINUTE / totalTime / 360
                    : 0f;

            invalidate();
            logIndex = (logIndex + 1) % LOG_SIZE;
            getHandler().postDelayed(this, UPDATE_RPM_PERIOD);
        }

        public void stop() {
            getHandler().removeCallbacks(rpmComputer);
            Arrays.fill(degreesLog, Float.NaN);
            Arrays.fill(timeLog, 0);
        }
    }
}
