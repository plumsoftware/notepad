package ru.plumsoftware.notepad

import android.app.Application
import android.content.Context
import ru.plumsoftware.notepad.data.model.AdsConfig
import ru.plumsoftware.notepad.data.model.PlatformConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        App.applicationContext = this
    }

    companion object {
        lateinit var applicationContext: Context
        val platformConfig = PlatformConfig.RuStoreConfig()
    }
}