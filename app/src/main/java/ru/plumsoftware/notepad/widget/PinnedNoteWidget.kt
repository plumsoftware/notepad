package ru.plumsoftware.notepad.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ru.plumsoftware.notepad.MainActivity
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantDark
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantLight
import ru.plumsoftware.notepad.ui.theme.surfaceDark
import ru.plumsoftware.notepad.ui.theme.surfaceLight

private val ORDER = ActionParameters.Key<String>("noteId")

class PinnedNoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val pinned = runCatching { WidgetDataProvider.getPinnedNote(context) }.getOrNull()

        provideContent {
            if (pinned == null) {
                EmptyPinned()
            } else {
                PinnedContent(pinned)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun EmptyPinned() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(surfaceLight, surfaceDark))
            .cornerRadius(24.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "📌 Нет закреплённых заметок",
            style = TextStyle(
                color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@androidx.compose.runtime.Composable
private fun PinnedContent(item: PinnedNoteItem) {
    val noteColor = if (item.colorLong == 0L) Color(0xFFFFD8D6) else Color(item.colorLong.toULong())
    val isLight = noteColor.luminance() > 0.5f
    val onColor = if (isLight) Color(0xFF1A1A1E) else Color.White
    val secondary = onColor.copy(alpha = 0.55f)
    val open = actionStartActivity<MainActivity>(actionParametersOf(ORDER to item.id))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(noteColor)
            .cornerRadius(24.dp)
            .clickable(open)
            .padding(16.dp)
    ) {
        // Заголовок «ЗАКРЕПЛЁННОЕ» + дата
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "📌 ЗАКРЕПЛЁННОЕ",
                style = TextStyle(
                    color = ColorProvider(secondary, secondary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                item.dateText,
                style = TextStyle(
                    color = ColorProvider(secondary, secondary),
                    fontSize = 12.sp
                )
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        Text(
            item.title,
            style = TextStyle(
                color = ColorProvider(onColor, onColor),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )

        if (item.description.isNotBlank()) {
            Spacer(GlanceModifier.height(4.dp))
            Text(
                item.description,
                style = TextStyle(color = ColorProvider(secondary, secondary), fontSize = 14.sp),
                maxLines = 1
            )
        }

        // Чек-лист (до 2 задач)
        item.tasks.forEach { task ->
            Spacer(GlanceModifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = GlanceModifier
                        .size(18.dp)
                        .cornerRadius(9.dp)
                        .background(
                            if (task.checked) Color(0xFF2663EB)
                            else onColor.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.checked) {
                        Text(
                            "✓",
                            style = TextStyle(
                                color = ColorProvider(Color.White, Color.White),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    task.text,
                    style = TextStyle(
                        color = if (task.checked) ColorProvider(secondary, secondary)
                        else ColorProvider(onColor, onColor),
                        fontSize = 14.sp,
                        textDecoration = if (task.checked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 1
                )
            }
        }

        // Голосовая заметка: аудио в виджете играть нельзя, поэтому показываем расшифровку
        if (item.hasVoice) {
            Spacer(GlanceModifier.height(10.dp))
            val transcript = item.voiceTranscription.ifBlank { "Не удалось распознать текст" }
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(onColor.copy(alpha = 0.08f))
                    .cornerRadius(16.dp)
                    .clickable(open)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎙", style = TextStyle(fontSize = 14.sp))
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    transcript,
                    style = TextStyle(
                        color = if (item.voiceTranscription.isBlank())
                            ColorProvider(secondary, secondary)
                        else ColorProvider(onColor, onColor),
                        fontSize = 13.sp
                    ),
                    maxLines = 2,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }
}

class PinnedNoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PinnedNoteWidget()
}
