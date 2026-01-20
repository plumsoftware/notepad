package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.MainScreenRouteState
import ru.plumsoftware.notepad.ui.Screen

@Composable
fun BottomBar(
    navController: NavController,
    currentScreen: MainScreenRouteState, // Мы знаем, где находимся
    onHomeClick: () -> Unit,
    onHabitsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Цвета для iOS стиля
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = 0.5.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. ЗАМЕТКИ
            BottomTabItem(
                icon = painterResource(R.drawable.house_fill),
                isSelected = currentScreen == MainScreenRouteState.Main,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onHomeClick()
                }
            )

            // 2. ПРИВЫЧКИ
            BottomTabItem(
                icon = rememberVectorPainter(Icons.Rounded.TaskAlt),
                isSelected = currentScreen == MainScreenRouteState.Habits,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onHabitsClick()
                }
            )

            // 3. ЦЕНТРАЛЬНАЯ КНОПКА (+) — ВОТ ЗДЕСЬ ИЗМЕНЕНИЯ
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        // 🔥 ЛОГИКА ПЕРЕХОДА 🔥
                        when (currentScreen) {
                            MainScreenRouteState.Habits -> {
                                // Если мы в привычках -> Создаем привычку
                                navController.navigate(Screen.AddHabit.route)
                            }
                            else -> {
                                // Если мы в заметках или календаре -> Создаем заметку
                                navController.navigate(Screen.AddNote.route)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add),
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 4. КАЛЕНДАРЬ
            BottomTabItem(
                icon = painterResource(R.drawable.calendar2_week_fill),
                isSelected = currentScreen == MainScreenRouteState.Calendar,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCalendarClick()
                }
            )

            // 5. НАСТРОЙКИ
            BottomTabItem(
                icon = rememberVectorPainter(Icons.Default.Settings),
                isSelected = false,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSettingsClick()
                }
            )
        }
    }
}

// Вспомогательный компонент для одной иконки
@Composable
fun RowScope.BottomTabItem(
    icon: Painter,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    // Анимация цвета
    val tint by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Убираем ripple как в iOS
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
    }
}

// Компонент одного Таба (Иконка + Подпись если нужна, но мы делаем без подписи для минимализма, как просил)
@Composable
fun RowScope.IOSTabItem(
    iconRes: Int,
    label: String, // Можно использовать для accessibility
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Анимация цвета
    val tabColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
            alpha = 0.4f
        ),
        animationSpec = tween(durationMillis = 200), label = "tabColor"
    )

    Box(
        modifier = Modifier
            .weight(1f) // Занимает равное пространство
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Важно: в iOS нет Ripple эффекта (кругов при нажатии)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = tabColor,
                modifier = Modifier.size(26.dp) // Чуть крупнее стандартных 24dp
            )
            // Если захочешь добавить подписи снизу (как в стандартном iOS TabBar), раскомментируй:
            /*
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = tabColor
            )
            */
        }
    }
}

