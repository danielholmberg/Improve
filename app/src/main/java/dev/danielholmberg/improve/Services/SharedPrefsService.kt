package dev.danielholmberg.improve.Services

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsService(private val context: Context, private val prefsKey: String) {

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

    companion object {
        private const val DEFAULT_GET_STRING_ERROR_STRING: String = ""
        private const val DEFAULT_GET_INT_ERROR_INT: Int = -9999
        private const val DEFAULT_GET_BOOLEAN_ERROR_BOOLEAN: Boolean = false
    }
}