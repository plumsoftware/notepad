package ru.plumsoftware.notepad.data.theme_saver

import android.content.Context
import androidx.core.content.edit

fun getDarkThemePreference(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("dark_theme", false)
}

fun saveDarkThemePreference(isDarkTheme: Boolean, context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putBoolean("dark_theme", isDarkTheme)
    }
}