package com.omega.PomodoroTimer;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented tomato_progress_bar, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under tomato_progress_bar.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.omega.PomodoroTimer", appContext.getPackageName());
    }
}
