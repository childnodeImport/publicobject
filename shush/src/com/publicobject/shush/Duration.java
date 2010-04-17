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

/**
 * How long until the alarm is restored.
 */
enum Duration {
    NEVER(             0, "nope!"),
    HALF_HOUR(        30, "in \u00BD hour"), // "1/2 hour"
    HOUR(         1 * 60, "in 1 hour"),
    TWO_HOURS(    2 * 60, "in 2 hours"),
    THREE_HOURS(  3 * 60, "in 3 hours"),
    FOUR_HOURS(   4 * 60, "in 4 hours"),
    FIVE_HOURS(   5 * 60, "in 5 hours"),
    SIX_HOURS(    6 * 60, "in 6 hours"),
    SEVEN_HOURS(  7 * 60, "in 7 hours"),
    EIGHT_HOURS(  8 * 60, "in 8 hours"),
    NINE_HOURS(   9 * 60, "in 9 hours"),
    TEN_HOURS(   10 * 60, "in 10 hours"),
    ELEVEN_HOURS(11 * 60, "in 11 hours"),
    TWELVE_HOURS(12 * 60, "in 12 hours");

    private final long millis;
    private final String label;

    Duration(long minutes, String label) {
        this.millis = minutes * 1000 * 60;
        this.label = label;
    }

    long millis() {
        return millis;
    }

    @Override public String toString() {
        return label;
    }
}
