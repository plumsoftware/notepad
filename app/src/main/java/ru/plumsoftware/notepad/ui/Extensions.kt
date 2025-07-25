package ru.plumsoftware.notepad.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("d MMMM yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}