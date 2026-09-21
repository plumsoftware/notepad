package ru.plumsoftware.notepad

import android.app.Application
import android.content.Context
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.data.database.NoteDatabase
import ru.plumsoftware.notepad.data.model.PlatformConfig
import ru.plumsoftware.notepad.widget.WidgetUpdater

class App : Application() {

    // Единый на всё приложение scope для фоновой синхронизации виджетов.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        App.applicationContext = this

        val config = AppMetricaConfig.newConfigBuilder(platformConfig.appMetricaId).build()
        AppMetrica.activate(this, config)

        startWidgetSync()
    }

    /**
     * ЕДИНСТВЕННЫЙ наблюдатель за заметками для обновления виджетов.
     * Раньше это делал каждый экземпляр NoteViewModel (их несколько — по одному на экран),
     * и параллельные updateAll конфликтовали в Glance, из-за чего часть виджетов
     * (закреплённое / ближайшее) обновлялась через раз. Теперь обновление в одном месте.
     */
    private fun startWidgetSync() {
        appScope.launch {
            val db = NoteDatabase.getDatabase(this@App)
            // collectLatest: при пачке правок берём последнее состояние и доводим обновление до конца
            db.noteDao().getAllNotes().collectLatest {
                runCatching { WidgetUpdater.updateAll(this@App) }
            }
        }
    }

    companion object {
        lateinit var applicationContext: Context

        val platformConfig: PlatformConfig by lazy { PlatformConfig.current() }
    }
}
