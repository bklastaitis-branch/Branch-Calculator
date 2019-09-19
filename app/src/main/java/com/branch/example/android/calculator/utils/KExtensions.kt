package com.branch.example.android.calculator.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.Toast
import com.branch.example.android.calculator.BuildConfig
import com.branch.example.android.calculator.R
import java.lang.Exception

const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"

// generic
fun Context.getPrefs(): SharedPreferences { return getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE) }
fun Context.throwDebugException(message: String) { if (BuildConfig.DEBUG) { throw Exception(message) } }
fun Context.throwDebugException() { throwDebugException("Debug exception") }
fun Context.toast(message: String) { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }

// prefs
fun Context.putPref(key: Int, i: Any) { putPref(getString(key), i) }
fun Context.putPref(key: String, i: Any) {
    when (i) {
        is Boolean -> getPrefs().edit().putBoolean(key, i).apply()
        is Int -> getPrefs().edit().putInt(key, i).apply()
        is Long -> getPrefs().edit().putLong(key, i).apply()
        is String -> getPrefs().edit().putString(key, i).apply()
        is Set<*> -> {
            i.forEach {
                if (it !is String) {
                    throwDebugException("calling putPref($key, Set<!String>)")
                    return
                }
            }
            @Suppress("UNCHECKED_CAST")
            getPrefs().edit().putStringSet(key, i as Set<String>).apply()
        }
        is Float -> getPrefs().edit().putFloat(key, i).apply()
        else -> throwDebugException("calling putPref but the passed value type, ${i.javaClass}, is not recognized")
    }
}
fun Context.hasPref(i: Int): Boolean {
    return getPrefs().contains(getString(i))
}
fun Context.hasPref(i: String): Boolean {
    return getPrefs().contains(i)
}
fun Context.getStringPref(i: Int): String? {
    return getPrefs().getString(getString(i), null)
}
fun Context.getStringPref(i: String): String? {
    return getPrefs().getString(i, null)
}

fun Context.getStringSetPref(i: Int): Set<String>? {
    return getPrefs().getStringSet(getString(i), null)
}
fun Context.getStringSetPref(i: String): Set<String>? {
    return getPrefs().getStringSet(i, null)
}

fun Context.getIntPref(i: Int): Int {
    return getPrefs().getInt(getString(i), 0)
}
fun Context.getIntPref(i: String): Int {
    return getPrefs().getInt(i, 0)
}

fun Context.getFloatPref(i: Int): Float {
    return getPrefs().getFloat(getString(i), 0f)
}
fun Context.geFloatPref(i: String): Float {
    return getPrefs().getFloat(i, 0f)
}

fun Context.getLongPref(i: Int): Long {
    return getPrefs().getLong(getString(i), 0L)
}
fun Context.geLongPref(i: String): Long {
    return getPrefs().getLong(i, 0L)
}

fun Context.getBooleanPref(i: Int): Boolean {
    return getPrefs().getBoolean(getString(i), false)
}
fun Context.geBooleanPref(i: String): Boolean {
    return getPrefs().getBoolean(i, false)
}

// shortcuts
fun Context.isFirstLaunch(): Boolean {
    return !hasPref(R.string.pref_is_first_launch)
}