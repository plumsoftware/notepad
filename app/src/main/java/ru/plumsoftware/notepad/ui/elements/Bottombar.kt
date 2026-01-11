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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.Home
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
import ru.plumsoftware.notepad.ui.Screen

@Composable
fun BottomBar(
    navController: NavController,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    var selected by remember { mutableIntStateOf(0) }

    // Эффект нажатия (вибрация)
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = selected) {
        when (selected) {
            0 -> onHomeClick()
            1 -> onCalendarClick()
        }
    }

    // 1. Контейнер Бара
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) // Почти непрозрачный фон
            // В iOS тапбар учитывает системные отступы (Navigation Bar)
            .navigationBarsPadding()
    ) {
        // 2. Разделитель (Hairline)
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), // Чуть темнее чем было
            thickness = 0.5.dp, // Очень тонкая линия
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Ряд Иконок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp) // Стандартная высота iOS TabBar (49pt)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround, // Равномерное распределение
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- ТАБ 1: ГЛАВНАЯ ---
            IOSTabItem(
                iconRes = R.drawable.house_fill, // Замени на контурную house, если не выбран (для полного iOS)
                label = stringResource(R.string.menu),
                isSelected = selected == 0,
                onClick = {
                    selected = 0
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )

            // --- ЦЕНТРАЛЬНАЯ КНОПКА (Добавить) ---
            // В iOS приложениях (Instagram, TikTok) центральная кнопка создания
            // стоит в общем ряду, не имеет фона, но выделяется цветом или размером.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Без волны при нажатии (iOS style)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate(Screen.AddNote.route)
                    },
                contentAlignment = Alignment.Center
            ) {
                // В iOS часто кнопку "Создать" делают чуть жирнее или в рамке
                Box(
                    modifier = Modifier
                        .size(32.dp) // Размер кнопки
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)) // Стиль "Кнопка"
                    // Или просто иконка, если хочешь минимализм, убери border и radius
                ) {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = stringResource(R.string.add),
                        tint = MaterialTheme.colorScheme.primary, // Всегда акцентный цвет
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // --- ТАБ 2: КАЛЕНДАРЬ ---
            IOSTabItem(
                iconRes = R.drawable.calendar2_week_fill,
                label = stringResource(R.string.daily_planner),
                isSelected = selected == 1,
                onClick = {
                    selected = 1
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
        }
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
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

// Preview функция
@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    // Создаем mock NavController для Preview
    val mockNavController = rememberNavController()

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            BottomBar(
                navController = mockNavController,
                onHomeClick = { println("Home clicked") },
                onCalendarClick = { println("Calendar clicked") }
            )
        }
    }
}
