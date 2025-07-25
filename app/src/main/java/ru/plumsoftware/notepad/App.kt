package ru.plumsoftware.notepad

import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        App.applicationContext = this
    }

    companion object {
        lateinit var applicationContext: Context
    }
}