package ru.plumsoftware.notepad.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
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
import ru.plumsoftware.notepad.ui.theme.onBackgroundDark
import ru.plumsoftware.notepad.ui.theme.onBackgroundLight
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantDark
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantLight
import ru.plumsoftware.notepad.ui.theme.surfaceDark
import ru.plumsoftware.notepad.ui.theme.surfaceLight
import ru.plumsoftware.notepad.ui.theme.textTertiaryDark
import ru.plumsoftware.notepad.ui.theme.textTertiaryLight

class HabitsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val habits = WidgetDataProvider.getHabitsForToday(context)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(surfaceLight, surfaceDark))
                    .cornerRadius(16.dp)
                    .padding(12.dp)
            ) {
                Text(
                    "Привычки на сегодня · ${habits.count { it.doneToday }} из ${habits.size}",
                    style = TextStyle(
                        color = ColorProvider(onBackgroundLight, onBackgroundDark),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
                Spacer(GlanceModifier.height(8.dp))

                habits.take(4).forEach { habit ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = androidx.glance.layout.Alignment.CenterVertically
                    ) {
                        Text(
                            if (habit.doneToday) "✓" else "○",
                            style = TextStyle(
                                color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                                fontSize = 12.sp
                            )
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Text(
                            "${habit.emoji} ${habit.title}",
                            style = TextStyle(
                                color = ColorProvider(onBackgroundLight, onBackgroundDark),
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

class HabitsWidgetReceiver : androidx.glance.appwidget.GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitsWidget()
}
