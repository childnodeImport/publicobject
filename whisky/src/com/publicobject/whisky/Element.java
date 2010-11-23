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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.Shape;
import android.util.Log;

public final class Element {

  private int x;
  private int y;
  private final Shape shape;
  private final Paint paint;

  public Element(int x, int y, Shape shape, Paint paint) {
    this.x = x;
    this.y = y;
    this.shape = shape;
    this.paint = paint;
  }

  public static Element newTriangle(int x, int y) {
    Path path = new Path();
    path.moveTo(0, 100);
    path.lineTo(50, 0);
    path.lineTo(100, 100);
    path.close();
    PathShape shape = new PathShape(path, 1.0f, 1.0f);
    shape.resize(1f, 1f);

    Paint paint = new Paint();
    paint.setColor(0xFF0000);
    paint.setAlpha(128);

    return new Element(x, y, shape, paint);
  }

  public void setXy(float x, float y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public boolean contains(float x, float y) {
    Log.i("Element", "contains x=" + x + " y=" + y + " this=" + this);
    return x >= this.x && x <= this.x + 100 // 100 == width
        && y >= this.y && y <= this.y + 100; // 100 == height
  }

  public static Element newCircle(int x, int y) {
    Path path = new Path();
    path.addCircle(50, 50, 50, Path.Direction.CW);
    PathShape shape = new PathShape(path, 1.0f, 1.0f);
    shape.resize(1f, 1f);

    Paint paint = new Paint();
    paint.setColor(0x0000FF);
    paint.setAlpha(128);

    return new Element(x, y, shape, paint);
  }

  public void draw(Canvas canvas) {
    int count = canvas.save();
    canvas.translate(x, y);
    shape.draw(canvas, paint);
    canvas.restoreToCount(count);
  }

  @Override public String toString() {
    return "Element at " + x + "," + y;
  }
}
