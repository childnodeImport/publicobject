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
 *  - zoom (done!)
 *  - order
 *  - fill
 *  - border
 *  - create shapes
 *  - align
 *  - rotate (done!)
 *  - scale (done!)
 *  - poly
 *  - select
 *  - precise selection (done!)
 *
 * Even More:
 *  - enable anti-aliasing when the frame rate is > 60Hz; disable it otherwise
 */
public final class DrawView extends View {

  /** The canvas elements, bottom to top. */
  private final List<Element> elements = new ArrayList<Element>();

  private final Matrix zoom = new Matrix();
  private final Matrix baseZoom = new Matrix();

  private Element transformed;
  private final Matrix baseTransform = new Matrix();

  private Action action;

  private float anchorX;
  private float anchorY;
  private float anchorX2;
  private float anchorY2;

  private float lastX;
  private float lastY;
  private float lastX2;
  private float lastY2;

  private Element selected;

  /* effects */
  private final Paint rotateHintPaint = Paints.holographOutline();
  private final int[] rotateHintGradient = new int[] { 0x660000FF, 0, 0 };
  private boolean drawTouchPoints;
  private final Paint anchorPaint = Paints.anchor();
  private final Paint lastPaint = Paints.last();

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

    int count = canvas.save();
    canvas.concat(zoom);
    try {
      for (Element element : elements) {
        element.draw(canvas, element == selected);
      }

      if (drawTouchPoints) {
        canvas.drawCircle(anchorX, anchorY, 5, anchorPaint);
        canvas.drawCircle(anchorX2, anchorY2, 5, anchorPaint);
        canvas.drawCircle(lastX, lastY, 5, lastPaint);
        canvas.drawCircle(lastX2, lastY2, 5, lastPaint);
      }

      if (action == Action.ROTATE) {
        drawRotateHint(canvas);
      }

    } finally {
      canvas.restoreToCount(count);
    }
  }

  /**
   * A blue circle around the center of rotation.
   */
  private void drawRotateHint(Canvas canvas) {
    // TODO: center the radial gradient on the circle, not the touch point?
    float rotateRadius = distance(anchorX2 - anchorX, anchorY2 - anchorY);
    RadialGradient radialGradient = new RadialGradient(lastX2, lastY2, rotateRadius * 2,
        rotateHintGradient, null, Shader.TileMode.MIRROR);
    rotateHintPaint.setShader(radialGradient);
    canvas.drawCircle(anchorX, anchorY, rotateRadius, rotateHintPaint);
  }

  private float distance(float deltaX, float deltaY) {
    return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    int pointCount = event.getPointerCount();
    int eventAction = event.getAction();

    float[] xy = {event.getX(0), event.getY(0)};
    Matrix baseZoomInvert = new Matrix();
    baseZoom.invert(baseZoomInvert);
    baseZoomInvert.mapPoints(xy);
    lastX = xy[0];
    lastY = xy[1];

    if (pointCount > 1) {
      float[] xy2 = {event.getX(1), event.getY(1)};
      baseZoomInvert.mapPoints(xy2);
      lastX2 = xy2[0];
      lastY2 = xy2[1];
    }

    if (eventAction == MotionEvent.ACTION_DOWN || eventAction == MotionEvent.ACTION_POINTER_DOWN) {
      anchorX = lastX;
      anchorY = lastY;

      for (int i = elements.size() - 1; i >= 0; i--) {
        Element element = elements.get(i);
        if (element.contains(lastX, lastY)) {
          action = Action.MOVE;
          selected = null;
          transformed = element;
          baseTransform.set(element.getMatrix());
          invalidate(); // TODO: precise repaint
          return true;
        }
      }

      if (pointCount > 1) {
        anchorX2 = lastX2;
        anchorY2 = lastY2;
        action = Action.ZOOM;
        invalidate(); // TODO: precise repaint
        return true;
      }

    } else if (pointCount > 1) {
      if (eventAction == MotionEvent.ACTION_POINTER_2_DOWN) {
        anchorX2 = lastX2;
        anchorY2 = lastY2;

        if (transformed != null) {
          apply(true); // apply move before starting rotate or scale
          if (transformed.contains(lastX2, lastY2)) {
            action = Action.SCALE;
          } else {
            action = Action.ROTATE;
          }
        } else {
          action = Action.ZOOM;
        }

        return true;

      } else if (eventAction == MotionEvent.ACTION_POINTER_2_UP) {
        apply(true); // commit
        if (transformed != null) {
          action = Action.MOVE;
        } else {
          action = null;
        }
        return true;
      }
    }

    if (eventAction == MotionEvent.ACTION_UP || eventAction == MotionEvent.ACTION_POINTER_UP) {
      apply(true);
      transformed = null;
      action = null;
      invalidate(); // TODO: precise repaint
      return true;
    }

    apply(false);
    return true;
  }

  private void apply(boolean updateBase) {
    float distanceBetweenAnchorPoints = distance(anchorX - anchorX2, anchorY - anchorY2);
    float distanceBetweenLastPoints = distance(lastX - lastX2, lastY - lastY2);
    float scale = distanceBetweenLastPoints / distanceBetweenAnchorPoints;

    if (action == Action.MOVE || action == Action.ROTATE || action == Action.SCALE) {
      Matrix update = new Matrix();
      update.set(baseTransform);

      if (action == Action.ROTATE) {
        float oldTranslateX = anchorX2 - anchorX;
        float oldTranslateY = anchorY2 - anchorY;
        double oldAngle = Math.atan2(oldTranslateY, oldTranslateX);
        float newTranslateX = lastX2 - anchorX;
        float newTranslateY = lastY2 - anchorY;
        double newAngle = Math.atan2(newTranslateY, newTranslateX);
        double rotate = newAngle - oldAngle;
        update.postRotate((float) Math.toDegrees(rotate), anchorX, anchorY);
      } else if (action == Action.SCALE) {
        update.postTranslate(-0.5f * (anchorX + anchorX2), -0.5f * (anchorY + anchorY2));
        update.postScale(scale, scale, 0, 0);
        update.postTranslate(0.5f * (lastX + lastX2), 0.5f * (lastY + lastY2));
      } else if (action == Action.MOVE) {
        update.postTranslate(lastX - anchorX, lastY - anchorY);
      }

      transformed.setMatrix(update);

      if (updateBase) {
        baseTransform.set(update);
        anchorX = lastX;
        anchorY = lastY;
        anchorX2 = lastX2;
        anchorY2 = lastY2;
      }

    } else if (action == Action.ZOOM) {
      Matrix update = new Matrix();
      update.postTranslate(-0.5f * (anchorX + anchorX2), -0.5f * (anchorY + anchorY2));
      update.postScale(scale, scale, 0, 0);
      update.postTranslate(0.5f * (lastX + lastX2), 0.5f * (lastY + lastY2));
      update.postConcat(baseZoom);

      zoom.set(update);

      if (updateBase) {
        baseZoom.set(update);
        anchorX = lastX;
        anchorY = lastY;
        anchorX2 = lastX2;
        anchorY2 = lastY2;
      }
    }

    invalidate(); // TODO: precise repaint
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // fill the maximum width and height available
    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
        MeasureSpec.getSize(heightMeasureSpec));
  }

  enum Action {
    MOVE,
    ROTATE,
    SCALE,
    ZOOM
  }
}
