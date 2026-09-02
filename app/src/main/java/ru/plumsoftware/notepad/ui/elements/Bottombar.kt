package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.theme.Dimens

@Composable
fun BottomBar(
    navController: NavController,
    isHomeSelected: Boolean,
    isSettingsSelected: Boolean,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    val fabSize = 54.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingS)
    ) {
        // Плавающая скруглённая панель (без тени)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.bottomBarHeight)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = Dimens.spacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabItem(
                icon = rememberVectorPainter(Icons.AutoMirrored.Outlined.TextSnippet),
                label = stringResource(R.string.notes),
                isSelected = isHomeSelected,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onHomeClick()
                }
            )

            // Пустое место под кнопку добавления
            Spacer(modifier = Modifier.weight(1f))

            BottomTabItem(
                icon = rememberVectorPainter(Icons.Outlined.Settings),
                label = stringResource(R.string.settings),
                isSelected = isSettingsSelected,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSettingsClick()
                }
            )
        }

        // Кнопка добавления заметки — крупнее и приподнята над панелью (~15%)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -(fabSize * 0.15f) - 4.dp)
                .clickable(
                    interactionSource = androidx.compose.runtime.remember {
                        MutableInteractionSource()
                    },
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate(Screen.AddNote.route)
                }
        ) {
            NotepadCircleFabIcon(modifier = Modifier.size(fabSize))
        }
    }
}

@Composable
fun NotepadCircleFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        NotepadCircleFabIcon()
    }
}

@Composable
fun NotepadCircleFabIcon(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .blueShadow(elevation = 12.dp, shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.add),
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun RowScope.BottomTabItem(
    icon: Painter,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(200),
        label = "tint"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(Dimens.iconSizeLarge)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = tint,
                maxLines = 1
            )
        }
    }
}
