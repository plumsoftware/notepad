package ru.plumsoftware.notepad.widget.add_note

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.theme.onPrimaryLight

class AddNoteWidget : GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            AddNoteWidgetContent()
        }
    }
}

@Composable
fun AddNoteWidgetContent() {
    val backgroundColor = ColorProvider(
        day = Color(0xFF1e73fc),
        night = Color(0xFF1e73fc)
    )
    val iconColor = ColorProvider(
        day = Color(0xFFFFFFFF),
        night = Color(0xFFFFFFFF)
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(200.dp)
            .clickable(actionRunCallback<AddNoteAction>()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(R.drawable.plus),
            contentDescription = "Add Note",
            modifier = GlanceModifier.size(24.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(iconColor)
        )
    }
}
