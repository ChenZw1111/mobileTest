package com.org.zrek.accenturetest.util

import android.util.Log

object Logger {
    private const val TAG = "BookingApp"
    private const val DEBUG = true  // 可以通过 BuildConfig.DEBUG 控制

    fun d(message: String, tag: String = TAG) {
        if (DEBUG) Log.d(tag, message)
    }

    fun i(message: String, tag: String = TAG) {
        if (DEBUG) Log.i(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (DEBUG) Log.e(tag, message, throwable)
    }
} 