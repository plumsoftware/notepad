package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun IOSPinInputScreen(
    title: String,
    onPinEntered: (String) -> Unit,
    onCancel: (() -> Unit)? = null,
    isError: Boolean = false // Для анимации тряски
) {
    var pin by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    // Анимация тряски (Shake) при ошибке
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            // Тряска влево-вправо
            pin = "" // Очищаем при ошибке
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            repeat(3) {
                offsetX.animateTo(10f, animationSpec = tween(50))
                offsetX.animateTo(-10f, animationSpec = tween(50))
            }
            offsetX.animateTo(0f)
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            // Небольшая задержка, чтобы юзер увидел последнюю точку
            delay(100)
            onPinEntered(pin)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Можно сделать .surface для контраста
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Заголовок
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(40.dp).offset(x = offsetX.value.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.offset {
                    androidx.compose.ui.unit.IntOffset(x = offsetX.value.toInt(), y = 0)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Индикаторы (Точки)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset(x = offsetX.value.dp)
            ) {
                repeat(4) { index ->
                    val filled = index < pin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                            .then(if (!filled) Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape) else Modifier)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Клавиатура (Numpad)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "back") // "" для выравнивания, "back" для удаления
                )

                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        row.forEach { digit ->
                            if (digit.isEmpty()) {
                                Spacer(modifier = Modifier.size(72.dp))
                            } else if (digit == "back") {
                                // Кнопка Удаления (только если есть что удалять или Отмена)
                                if (pin.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                pin = pin.dropLast(1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Удалить", // Или иконка Backspace
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else if (onCancel != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) { onCancel() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Отмена", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(72.dp))
                                }
                            } else {
                                // Цифровая кнопка
                                NumButton(number = digit) {
                                    if (pin.length < 4) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        pin += digit
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun NumButton(number: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) // Серый фон как в iOS
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}