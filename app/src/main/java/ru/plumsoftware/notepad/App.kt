package ru.plumsoftware.notepad

import android.app.Application
import android.content.Context
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ru.plumsoftware.notepad.data.model.PlatformConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        App.applicationContext = this

        val config = AppMetricaConfig.newConfigBuilder(platformConfig.appMetricaId).build()
        AppMetrica.activate(this, config)
    }

    companion object {
        lateinit var applicationContext: Context

        val platformConfig: PlatformConfig by lazy { PlatformConfig.current() }
    }
}