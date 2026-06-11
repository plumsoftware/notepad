package ru.plumsoftware.notepad.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ru.plumsoftware.notepad.MainActivity
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.theme.onBackgroundDark
import ru.plumsoftware.notepad.ui.theme.onBackgroundLight
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantDark
import ru.plumsoftware.notepad.ui.theme.onSurfaceVariantLight
import ru.plumsoftware.notepad.ui.theme.primaryContainerDark
import ru.plumsoftware.notepad.ui.theme.primaryContainerLight
import ru.plumsoftware.notepad.ui.theme.primaryDark
import ru.plumsoftware.notepad.ui.theme.primaryLight
import ru.plumsoftware.notepad.ui.theme.surfaceDark
import ru.plumsoftware.notepad.ui.theme.surfaceLight

class SmallNoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(surfaceLight, surfaceDark))
                    .cornerRadius(16.dp)
                    .clickable(
                        actionStartActivity<MainActivity>(
                            actionParametersOf(ActionParameters.Key<String>("action") to "new_note")
                        )
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .cornerRadius(10.dp)
                        .background(ColorProvider(primaryContainerLight, primaryContainerDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.plus),
                        contentDescription = null,
                        modifier = GlanceModifier.size(20.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(primaryLight, primaryDark))
                    )
                }
                Spacer(GlanceModifier.width(10.dp))
                Column {
                    Text(
                        "Новая заметка",
                        style = TextStyle(
                            color = ColorProvider(onBackgroundLight, onBackgroundDark),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        "Нажмите, чтобы создать",
                        style = TextStyle(
                            color = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

class SmallNoteWidgetReceiver : androidx.glance.appwidget.GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallNoteWidget()
}
