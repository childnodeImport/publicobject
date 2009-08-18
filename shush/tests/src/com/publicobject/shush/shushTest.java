package com.publicobject.shush;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.publicobject.shush.shushTest \
 * com.publicobject.shush.tests/android.test.InstrumentationTestRunner
 */
public class shushTest extends ActivityInstrumentationTestCase<shush> {

    public shushTest() {
        super("com.publicobject.shush", shush.class);
    }

}
