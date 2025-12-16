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

        val platformConfig = PlatformConfig.RuStoreConfig()
        val config = AppMetricaConfig.newConfigBuilder(platformConfig.appMetricaId).build()
        // Initializing the AppMetrica SDK.
        AppMetrica.activate(this, config)
    }

    companion object {
        lateinit var applicationContext: Context
        // Не забываем менять платформ конфиг в двух местах
        val platformConfig = PlatformConfig.RuStoreConfig()
    }
}