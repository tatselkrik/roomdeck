package io.github.tatselkrik.roomdeck.remote

import android.util.Log

private const val TAG = "RoomDeckRemote"

internal fun logD(message: String) {
    if (BuildConfig.DEBUG) Log.d(TAG, message)
}

internal fun logI(message: String) {
    if (BuildConfig.DEBUG) Log.i(TAG, message)
}

internal fun logW(message: String) {
    if (BuildConfig.DEBUG) Log.w(TAG, message)
}

internal fun logE(message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        if (throwable == null) Log.e(TAG, message) else Log.e(TAG, message, throwable)
    }
}
