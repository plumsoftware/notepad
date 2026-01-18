package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.plumsoftware.notepad.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateAppBottomSheet(
    onDismiss: () -> Unit,
    onRateConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stars = listOf(1, 2, 3, 4, 5)
    var selectedStars by remember { mutableIntStateOf(0) }

    // Эффект вибрации при нажатии на звезды
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent, // Прозрачный контейнер для эффекта "парения"
        dragHandle = null, // Убираем стандартную полоску
        scrimColor = Color.Black.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Отступы с краев (iOS margin)
                .padding(bottom = 16.dp)     // Отступ снизу
                .navigationBarsPadding()     // Учет навигационной полоски
        ) {
            // --- 1. ОСНОВНОЙ БЛОК (Заголовок + Звезды) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Заголовок
                Text(
                    text = stringResource(R.string.rate_app_title), // "Оценить приложение"
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                // Подзаголовок (опционально, можно убрать)
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )

                // Звезды
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    stars.forEach { index ->
                        // Анимация цвета звезды
                        val starColor by animateColorAsState(
                            targetValue = if (index <= selectedStars) Color(0xFFFFB340) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            animationSpec = tween(300),
                            label = "starColor"
                        )

                        // Анимация размера при нажатии (Scale effect)
                        val scale by animateFloatAsState(
                            targetValue = if (index == selectedStars) 1.2f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "starScale"
                        )

                        Icon(
                            imageVector = Icons.Rounded.Star, // Rounded звезды выглядят приятнее Outlined
                            contentDescription = "Star $index",
                            tint = starColor,
                            modifier = Modifier
                                .size(44.dp)
                                .scale(scale)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // Убираем серую волну (iOS style)
                                ) {
                                    selectedStars = index
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Небольшая задержка перед закрытием, чтобы увидеть анимацию
                                    onRateConfirmed()
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. КНОПКА ОТМЕНА ("Не сейчас") ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface) // Белый/Черный фон для отмены
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.cancel), // Или "Не сейчас"
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary // Синий цвет
                )
            }
        }
    }
}