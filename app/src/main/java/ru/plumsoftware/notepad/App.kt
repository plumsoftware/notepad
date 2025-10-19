package ru.plumsoftware.notepad

import android.app.Application
import android.content.Context
import ru.plumsoftware.notepad.data.model.AdsConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        App.applicationContext = this
    }

    companion object {
        lateinit var applicationContext: Context
        val adsConfig = AdsConfig.RuStoreAds()
    }
}