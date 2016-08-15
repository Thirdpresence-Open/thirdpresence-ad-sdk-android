package com.thirdpresence.adsdk.sdk.internal;

import android.util.Log;

/**
 *
 * TLog is a wrapper class for Android Log class
 *
 */
public class TLog {
    public final static String LOG_TAG = "TPRLOG";
    public static boolean enabled = false;

    private TLog() {
    };

    public static void v(String message) {
        if (enabled) {
            Log.v(LOG_TAG, message);
        }
    }

    public static void d(String message) {
        if (enabled) {
            Log.d(LOG_TAG, message);
        }
    }

    public static void i(String message) {
        if (enabled) {
            Log.i(LOG_TAG, message);
        }
    }

    public static void w(String message) {
        if (enabled) {
            Log.w(LOG_TAG, message);
        }
    }

    public static void e(String message) {
        if (enabled) {
            Log.e(LOG_TAG, message);
        }
    }


}
