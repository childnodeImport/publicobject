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

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

public final class Names {
    /**
     * Returns styled text containing alternating player names and scores.
     */
    public static SpannableStringBuilder styleScores(Game game) {
        int maxTotal = game.maxTotal();
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        for (int p = 0, size = game.playerCount(); p < size; p++) {
            if (p > 0) {
                ssb.append("  ");
            }
            String name = game.playerName(p);
            String total = Integer.toString(game.playerTotal(p));
            ssb.append(name);
            ssb.append("\u00a0");

            ssb.append(total);
            ssb.setSpan(new ForegroundColorSpan(game.playerColor(p)),
                    ssb.length() - total.length(), ssb.length(), 0);
            if (maxTotal == game.playerTotal(p)) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        ssb.length() - total.length(), ssb.length(), 0);
            }
        }
        return ssb;
    }
}
