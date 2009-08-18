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
    HALF_HOUR(30 * 60 * 1000, "in \u00BD hour"), // "1/2 hour"
    HOUR(60 * 60 * 1000, "in 1 hour"),
    TWO_HOURS(120 * 60 * 1000, "in 2 hours"),
    THREE_HOURS(180 * 60 * 1000, "in 3 hours"),
    NEVER(0, "nope!");

    private final long millis;
    private final String label;

    Duration(long millis, String label) {
        this.millis = millis;
        this.label = label;
    }

    long millis() {
        return millis;
    }

    @Override
    public String toString() {
        return label;
    }
}
