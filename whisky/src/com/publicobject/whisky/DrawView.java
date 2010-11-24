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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * To do:
 *  - zoom
 *  - order
 *  - fill
 *  - border
 *  - create shapes
 *  - align
 *  - rotate (done!)
 *  - poly
 *  - select
 */
public final class DrawView extends View {

  /** The canvas elements, bottom to top. */
  private final List<Element> elements = new ArrayList<Element>();

  private Element selected;

  private Element transformed;
  private final Matrix baseTransform = new Matrix();
  private float anchorX;
  private float anchorY;

  /*
   * The lever point that rotation starts, or NaN for no rotation.
   */
  private float rotateLeverX = Float.NaN;
  private float rotateLeverY = Float.NaN;
  private float rotateCurrentX;
  private float rotateCurrentY;

  /* effects */
  private final Paint rotateHintPaint = Paints.holographOutline();
  private final int[] rotateHintGradient = new int[] { 0x660000FF, 0, 0 };

  public DrawView(Context context) {
    super(context);

    Element a = Element.newTriangle(100, 0xFFFF00);
    a.move(400, 430);
    elements.add(a);

    Element b = Element.newTriangle(200, 0xFF00FF);
    b.move(210, 300);
    elements.add(b);

    Element c = Element.newTriangle(300, 0xFF0000);
    c.move(100, 200);
    elements.add(c);

    Element d = Element.newTriangle(400, 0x00FF00);
    d.move(200, 200);
    elements.add(d);

    Element e = Element.newCircle(100, 0x0000FF);
    e.move(250, 230);
    elements.add(e);

    Element f = Element.newCircle(150, 0x00FFFF);
    f.move(250, 230);
    elements.add(f);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    for (Element element : elements) {
      element.draw(canvas, element == selected);
    }

    if (!Float.isNaN(rotateLeverX)) {
      drawRotateHint(canvas);
    }
  }

  /**
   * A blue circle around the center of rotation.
   */
  private void drawRotateHint(Canvas canvas) {
    // TODO: center the radial gradient on the circle, not the touch point?
    float rotateRadius = radius(rotateLeverX - anchorX, rotateLeverY - anchorY);
    RadialGradient radialGradient = new RadialGradient(rotateCurrentX, rotateCurrentY, rotateRadius * 2,
        rotateHintGradient, null, Shader.TileMode.MIRROR);
    rotateHintPaint.setShader(radialGradient);
    canvas.drawCircle(anchorX, anchorY, rotateRadius, rotateHintPaint);
  }

  private float radius(float dx, float dy) {
    return (float) Math.sqrt(dx * dx + dy * dy);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();

    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      for (int i = elements.size() - 1; i >= 0; i--) {
        Element element = elements.get(i);
        if (element.contains(x, y)) {
          selected = null;
          transformed = element;
          baseTransform.set(element.getMatrix());
          anchorX = x;
          anchorY = y;
          invalidate(); // TODO: precise repaint
          return true;
        }
      }

      if (selected != null) {
        selected = null;
        invalidate(); // TODO: precise repaint
      }

      return false;
    }

    if (transformed == null) {
      return false;
    }

    if (event.getPointerCount() > 1) {
      if (event.getAction() == MotionEvent.ACTION_POINTER_2_DOWN) {
        apply(event, true); // apply move before starting rotate
        rotateLeverX = event.getX(1);
        rotateLeverY = event.getY(1);
        return true;
      } else if (event.getAction() == MotionEvent.ACTION_POINTER_2_UP) {
        apply(event, true); // apply rotate before resuming move
        rotateLeverX = Float.NaN;
        rotateLeverY = Float.NaN;
        return true;
      }
    }

    apply(event, false);

    if (event.getAction() == MotionEvent.ACTION_UP) {
      selected = transformed;
      transformed = null;
      rotateLeverX = Float.NaN;
      rotateLeverY = Float.NaN;
      invalidate(); // TODO: precise repaint
    }

    return true;
  }

  private void apply(MotionEvent event, boolean updateBase) {
    float x = event.getX();
    float y = event.getY();

    Matrix base = new Matrix();
    base.set(baseTransform);
    Matrix newTransform = new Matrix();

    if (!Float.isNaN(rotateLeverX)) {
      rotateCurrentX = event.getX(1);
      rotateCurrentY = event.getY(1);
      float oldTranslateX = rotateLeverX - anchorX;
      float oldTranslateY = rotateLeverY - anchorY;
      double oldAngle = Math.atan2(oldTranslateY, oldTranslateX);
      float newTranslateX = rotateCurrentX - anchorX;
      float newTranslateY = rotateCurrentY - anchorY;
      double newAngle = Math.atan2(newTranslateY, newTranslateX);
      double rotate = newAngle - oldAngle;
      newTransform.setRotate((float) Math.toDegrees(rotate), anchorX, anchorY);
    } else {
      newTransform.setTranslate(x - anchorX, y - anchorY);
    }

    base.postConcat(newTransform);
    transformed.setMatrix(base);

    if (updateBase) {
      baseTransform.set(base);
      anchorX = x;
      anchorY = y;
    }

    invalidate(); // TODO: precise repaint
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // fill the maximum width and height available
    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
        MeasureSpec.getSize(heightMeasureSpec));
  }
}
