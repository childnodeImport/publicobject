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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public final class DrawView extends View {

  private final List<Element> elements = new ArrayList<Element>();

  private Element draggedElement;
  private float draggedElementOffsetX;
  private float draggedElementOffsetY;

  public DrawView(Context context) {
    super(context);
    elements.add(Element.newTriangle(200, 200));
    elements.add(Element.newCircle(250, 250));
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    Log.i("DrawView", "me width=" + getWidth() + ", height=" + getHeight());
    Log.i("DrawView", "canvas width=" + canvas.getWidth() + ", height=" + canvas.getHeight());

    for (Element element : elements) {
      element.draw(canvas);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    Log.i("DrawView", "onTouchEvent=" + event);

    float x = event.getX();
    float y = event.getY();

    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      for (Element element : elements) { // TODO: iterate in reverse
        if (element.contains(x, y)) {
          draggedElement = element;
          draggedElementOffsetX = x - element.getX();
          draggedElementOffsetY = y - element.getY();
          return true;
        }
      }
      return false;
    }

    if (draggedElement != null) {
      draggedElement.setXy(x - draggedElementOffsetX, y - draggedElementOffsetY);
      if (event.getAction() == MotionEvent.ACTION_UP) {
        draggedElement = null;
      }
      invalidate(); // TODO: precise
      return true;
    }

    return false;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // fill the maximum width and height available
    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
        MeasureSpec.getSize(heightMeasureSpec));
  }
}
