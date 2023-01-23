package dev.danielholmberg.improve.clean.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dev.danielholmberg.improve.BuildConfig
import java.util.*

class SharedPrefsService(private val context: Context, private val prefsKey: String) {

    lateinit var deviceId: String

    init {
        loadDeviceId()
    }

    fun putInt(key: String?, pref: Int) {
        val preferences: SharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(key, pref)
        editor.apply()
    }

    fun getInt(key: String?): Int {
        val preferences: SharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        return preferences.getInt(key, DEFAULT_GET_INT_ERROR_INT)
    }

    fun putString(key: String?, pref: String?) {
        val preferences: SharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(key, pref)
        editor.apply()
    }

    fun getString(key: String?): String? {
        val preferences: SharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        return preferences.getString(key, DEFAULT_GET_STRING_ERROR_STRING)
    }

    fun putBoolean(key: String?, pref: Boolean?) {
        val preferences: SharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, pref!!)
        editor.apply()
    }

    fun getBoolean(key: String?): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        return preferences.getBoolean(key, DEFAULT_GET_BOOLEAN_ERROR_BOOLEAN)
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        return preferences.getBoolean(key, defaultValue)
    }

    private fun loadDeviceId() {
        // Retrieve the stored Device ID or generate a new
        val deviceIdKey = "device_id"
        val newDeviceId = UUID.randomUUID().toString()
        deviceId = getString(deviceIdKey) ?: newDeviceId
        putString(deviceIdKey, deviceId)

        Log.i(TAG, "Device ID: $deviceId")
    }

    companion object {
        private val TAG = BuildConfig.TAG + SharedPrefsService::class.java.simpleName
        private const val DEFAULT_GET_STRING_ERROR_STRING: String = ""
        private const val DEFAULT_GET_INT_ERROR_INT: Int = -9999
        private const val DEFAULT_GET_BOOLEAN_ERROR_BOOLEAN: Boolean = false
    }
}