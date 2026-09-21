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
import ru.plumsoftware.notepad.ui.theme.errorLight
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

class AgendaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val reminders = runCatching { WidgetDataProvider.getUpcomingReminders(context, 2) }
            .getOrDefault(emptyList())
        val week = WidgetDataProvider.weekStrip()
        val month = WidgetDataProvider.currentMonthName()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(surfaceLight, surfaceDark))
                    .cornerRadius(24.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "БЛИЖАЙШЕЕ",
                        style = TextStyle(
                            color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        month,
                        style = TextStyle(
                            color = ColorProvider(primaryLight, primaryDark),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(GlanceModifier.height(10.dp))

                // Полоска дней недели
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(
                            modifier = GlanceModifier.defaultWeight().height(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.isToday) {
                                Box(
                                    modifier = GlanceModifier
                                        .width(34.dp)
                                        .height(28.dp)
                                        .cornerRadius(10.dp)
                                        .background(ColorProvider(primaryLight, primaryDark)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.day.toString(),
                                        style = TextStyle(
                                            color = ColorProvider(Color.White, Color.White),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                Text(
                                    day.day.toString(),
                                    style = TextStyle(
                                        color = if (day.isWeekend)
                                            ColorProvider(errorLight, errorLight)
                                        else ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(GlanceModifier.height(10.dp))

                if (reminders.isEmpty()) {
                    Text(
                        "Нет ближайших напоминаний",
                        style = TextStyle(
                            color = ColorProvider(textTertiaryLight, textTertiaryDark),
                            fontSize = 12.sp
                        )
                    )
                } else {
                    reminders.forEach { item ->
                        ReminderRow(item)
                        Spacer(GlanceModifier.height(10.dp))
                    }
                }
            }
        }
    }
}

private val ORDER = ActionParameters.Key<String>("noteId")

@androidx.compose.runtime.Composable
private fun ReminderRow(item: ReminderItem) {
    val barColor = if (item.colorLong == 0L) Color(0xFF9AA0A6) else Color(item.colorLong.toULong())
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(actionStartActivity<MainActivity>(actionParametersOf(ORDER to item.id))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .width(5.dp)
                .height(38.dp)
                .cornerRadius(3.dp)
                .background(barColor)
        ) {}
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                item.title,
                style = TextStyle(
                    color = ColorProvider(onBackgroundLight, onBackgroundDark),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                item.subtitle,
                style = TextStyle(
                    color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
        Spacer(GlanceModifier.width(8.dp))
        Text(item.emoji, style = TextStyle(fontSize = 18.sp))
    }
}

class AgendaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AgendaWidget()
}
