package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.plumsoftware.notepad.ui.theme.deleteColor

@Composable
fun DeleteButton(onDelete: () -> Unit, enabled: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(deleteColor)
            .clickable(enabled = enabled, role = Role.Button) {
                onDelete()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(6.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}