package ru.plumsoftware.notepad.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

/** Обновляет все виджеты, связанные с заметками (вызывается при изменении данных). */
object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        val appContext = context.applicationContext
        runCatching { TodayWidget().updateAll(appContext) }
        runCatching { AgendaWidget().updateAll(appContext) }
        runCatching { PinnedNoteWidget().updateAll(appContext) }
    }
}
