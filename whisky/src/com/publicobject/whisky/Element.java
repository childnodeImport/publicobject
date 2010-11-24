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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.PathShape;
import android.util.Log;

/**
 * A shape plus its projection onto the canvas.
 */
public final class Element {

  private final Matrix matrix = new Matrix();
  private final PathShape shape;
  private final Paint paint;
  private final float width;
  private final float height;

  public Element(Path path, Paint paint) {
    this.shape = new PathShape(path, 1.0f, 1.0f);
    this.shape.resize(1f, 1f);
    this.paint = paint;

    RectF bounds = new RectF();
    path.computeBounds(bounds, false);
    width = bounds.width();
    height = bounds.height();
  }

  public static Element newTriangle(int size, int color) {
    Path path = new Path();
    path.moveTo(0, size);
    path.lineTo(size / 2, 0);
    path.lineTo(size, size);
    path.close();

    Paint paint = new Paint();
    paint.setColor(color);
    paint.setAlpha(128);

    return new Element(path, paint);
  }

  public static Element newCircle(int radius, int color) {
    Path path = new Path();
    path.addCircle(radius, radius, radius, Path.Direction.CW);

    Paint paint = new Paint();
    paint.setColor(color);
    paint.setAlpha(128);

    return new Element(path, paint);
  }

  public boolean contains(float x, float y) {
    float[] xy = canvasPointToElementPoint(x, y);
    x = xy[0];
    y = xy[1];
    Log.i("Element", "point=" + x + "," + y + " width=" + width + " height=" + height);

    return x >= 0 && x <= width && y >= 0 && y <= height;
  }

  private float[] canvasPointToElementPoint(float x, float y) {
    Matrix inverse = new Matrix(); // TODO: cache the inverse
    matrix.invert(inverse);
    float[] xy = { x, y };
    inverse.mapPoints(xy);
    return xy;
  }

  public void move(int x, int y) {
    matrix.postTranslate(x, y);
  }

  public Matrix getMatrix() {
    return matrix;
  }

  public void draw(Canvas canvas, boolean selected) {

    if (selected) {
      drawSelectionHint(canvas);
    }

    int count = canvas.save();
    canvas.concat(matrix);
    shape.draw(canvas, paint);
    canvas.restoreToCount(count);
  }

  private void drawSelectionHint(Canvas canvas) {
//    int count = canvas.save();
//    canvas.translate(6, 6);
//    canvas.concat(matrix);
//    shape.draw(canvas, Paints.holographOutline());
//    canvas.restoreToCount(count);
  }

  public void setMatrix(Matrix matrix) {
    this.matrix.set(matrix);
  }

  @Override public String toString() {
    float[] xy = canvasPointToElementPoint(0, 0);
    return "Element at " + xy[0] + "," + xy[1];
  }
}
