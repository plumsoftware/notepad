package ru.plumsoftware.notepad.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NoteViewModelFactory(private val application: Application, private val openAddNote: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(application, openAddNote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}