package ru.plumsoftware.notepad.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ru.plumsoftware.notepad.MainActivity
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.ui.theme.onBackgroundDark
import ru.plumsoftware.notepad.ui.theme.onBackgroundLight
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantDark
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantLight
import ru.plumsoftware.notepad.ui.theme.surfaceDark
import ru.plumsoftware.notepad.ui.theme.surfaceLight
import ru.plumsoftware.notepad.ui.theme.surfaceVariantDark
import ru.plumsoftware.notepad.ui.theme.surfaceVariantLight
import ru.plumsoftware.notepad.ui.theme.textTertiaryDark
import ru.plumsoftware.notepad.ui.theme.textTertiaryLight

class NotesPreviewWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val notes = WidgetDataProvider.getRecentNotes(context, 2)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(surfaceLight, surfaceDark))
                    .cornerRadius(16.dp)
                    .padding(12.dp)
            ) {
                Text(
                    "Заметки",
                    style = TextStyle(
                        color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(GlanceModifier.height(8.dp))

                if (notes.isEmpty()) {
                    Text(
                        "Нет заметок",
                        style = TextStyle(
                            color = ColorProvider(textTertiaryLight, textTertiaryDark),
                            fontSize = 12.sp
                        )
                    )
                } else {
                    notes.forEach { note ->
                        Column(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(ColorProvider(surfaceVariantLight, surfaceVariantDark))
                                .cornerRadius(10.dp)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .clickable(
                                    actionStartActivity<MainActivity>(
                                        actionParametersOf(
                                            ActionParameters.Key<String>("noteId") to note.id
                                        )
                                    )
                                )
                        ) {
                            if (note.title.isNotEmpty()) {
                                Text(
                                    note.title,
                                    style = TextStyle(
                                        color = ColorProvider(onBackgroundLight, onBackgroundDark),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1
                                )
                            }
                            if (note.description.isNotEmpty()) {
                                Text(
                                    note.description,
                                    style = TextStyle(
                                        color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }
}

class NotesPreviewWidgetReceiver : androidx.glance.appwidget.GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NotesPreviewWidget()
}
