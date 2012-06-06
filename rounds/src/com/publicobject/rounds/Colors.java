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

import android.graphics.Color;

public final class Colors {
    private static final int yellow = Color.rgb(255, 255, 0);
    private static final int gold = Color.rgb(255, 187, 51);
    private static final int orange = Color.rgb(242, 129, 0);
    private static final int red = Color.rgb(204, 0, 0);
    private static final int moss = Color.rgb(102, 153, 0);
    private static final int lime = Color.rgb(153, 204, 0);
    private static final int coral = Color.rgb(255, 68, 68);
    private static final int magenta = Color.rgb(255, 0, 214);
    private static final int green = Color.rgb(26, 209, 0);
    private static final int mint = Color.rgb(164, 231, 179);
    private static final int pink = Color.rgb(233, 179, 211);
    private static final int lavender = Color.rgb(170, 102, 204);
    private static final int teal = Color.rgb(51, 181, 229);
    private static final int sky = Color.rgb(0, 153, 204);
    private static final int blue = Color.rgb(58, 90, 255);
    private static final int purple = Color.rgb(153, 51, 204);

    public static final int[] COLORS = {
            yellow, gold, orange, red,
            moss, lime, coral, magenta,
            green, mint, pink, lavender,
            teal, sky, blue, purple,
    };

    public static final int[] DEFAULT_COLORS = {
            teal, coral, lime, gold, lavender, yellow, blue, pink
    };
}
