package ru.plumsoftware.notepad.data.theme_saver

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ThemeState(initialValue: Boolean) {
    var isDarkTheme by mutableStateOf(initialValue)
}