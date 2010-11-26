/*
 * Copyright (C) 2010 Jesse Wilson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicobject.whisky;

import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Reusable paints.
 */
public class Paints {

  public static Paint holographOutline() {
    Paint result = new Paint();
    result.setStyle(Paint.Style.STROKE);
    RadialGradient radialGradient = new RadialGradient(0, 0, 100,
        new int[] { 0x880000FF, 0x440000FF }, null, Shader.TileMode.MIRROR);
    result.setShader(radialGradient);
    result.setStrokeWidth(4);
    return result;
  }

  public static Paint anchor() {
    Paint result = new Paint();
    result.setStyle(Paint.Style.FILL);
    result.setColor(0xFFFF0000);
    return result;
  }

  public static Paint last() {
    Paint result = new Paint();
    result.setStyle(Paint.Style.FILL);
    result.setColor(0xFF00FF00);
    return result;
  }
}
