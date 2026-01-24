package ru.plumsoftware.notepad.ui.elements.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.notepad.data.theme_saver.ThemeState

@Composable
fun HabitCard(
    title: String,
    emoji: String,
    streak: Int, // Серия дней
    color: Color,
    isCompletedToday: Boolean,
    onToggle: () -> Unit,
    onLongClick: () -> Unit, // <-- Обязательно для меню
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Анимация масштаба кнопки при нажатии
    val scale by animateFloatAsState(
        targetValue = if (isCompletedToday) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Анимация цвета кнопки (если не выполнено - серое/прозрачное, выполнено - цвет привычки)
    val buttonColor by animateColorAsState(
        targetValue = if (isCompletedToday) color else Color.Transparent,
        animationSpec = tween(300),
        label = "color"
    )

    val sectionColor = MaterialTheme.colorScheme.surface

    // Карточка (Белая / Темная)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.05f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() }, // Обычный клик - редактирование
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick() // Долгий клик - меню
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = sectionColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ЛЕВАЯ ЧАСТЬ (Иконка + Инфо)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Иконка (или заглушка) на цветном фоне
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(color.copy(alpha = 0.15f)), // Полупрозрачный фон
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineSmall, // Размер эмодзи
                        modifier = Modifier.padding(bottom = 2.dp) // Визуальная коррекция
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    // Название привычки
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp // Стандарт iOS
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Строка стрика (огня)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF9500), // Оранжевый огонь
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streak дн.", // stringResource(R.string.days_streak, streak)
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // ПРАВАЯ ЧАСТЬ (Кнопка выполнения)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .scale(scale) // Пружина
                    .clip(CircleShape)
                    .background(buttonColor) // Анимированный фон
                    .then(
                        // Если НЕ выполнено - рисуем цветную рамку
                        if (!isCompletedToday) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Убираем ripple
                    ) {
                        // Вибрация и коллбэк
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Галочка (появляется только если выполнено)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isCompletedToday,
                    enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(1.dp) // Делаем визуально жирнее
                    )
                }
            }
        }
    }
}