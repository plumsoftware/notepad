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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ru.plumsoftware.notepad.MainActivity
import ru.plumsoftware.notepad.ui.theme.onBackgroundDark
import ru.plumsoftware.notepad.ui.theme.onBackgroundLight
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantDark
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantLight
import ru.plumsoftware.notepad.ui.theme.primaryDark
import ru.plumsoftware.notepad.ui.theme.primaryLight
import ru.plumsoftware.notepad.ui.theme.surfaceDark
import ru.plumsoftware.notepad.ui.theme.surfaceLight
import ru.plumsoftware.notepad.ui.theme.textTertiaryDark
import ru.plumsoftware.notepad.ui.theme.textTertiaryLight

class TodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val notes = runCatching { WidgetDataProvider.getTodayNotes(context, 3) }
            .getOrDefault(emptyList())
        val count = runCatching { WidgetDataProvider.getTodayCount(context) }.getOrDefault(0)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(surfaceLight, surfaceDark))
                    .cornerRadius(24.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(14.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "СЕГОДНЯ",
                        style = TextStyle(
                            color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    if (count > 0) {
                        Text(
                            count.toString(),
                            style = TextStyle(
                                color = ColorProvider(primaryLight, primaryDark),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(GlanceModifier.height(8.dp))

                if (notes.isEmpty()) {
                    Text(
                        "Нет заметок за сегодня",
                        style = TextStyle(
                            color = ColorProvider(textTertiaryLight, textTertiaryDark),
                            fontSize = 12.sp
                        )
                    )
                } else {
                    notes.forEach { item ->
                        NoteRow(item)
                        Spacer(GlanceModifier.height(10.dp))
                    }
                }
            }
        }
    }
}

private val ORDER = ActionParameters.Key<String>("noteId")

@androidx.compose.runtime.Composable
private fun NoteRow(item: TodayNoteItem) {
    val barColor = if (item.colorLong == 0L) Color(0xFF9AA0A6) else Color(item.colorLong.toULong())
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(
                actionStartActivity<MainActivity>(actionParametersOf(ORDER to item.id))
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .width(5.dp)
                .height(36.dp)
                .cornerRadius(3.dp)
                .background(barColor)
        ) {}
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                item.title,
                style = TextStyle(
                    color = ColorProvider(onBackgroundLight, onBackgroundDark),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                item.meta,
                style = TextStyle(
                    color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}
