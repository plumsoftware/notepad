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
import androidx.compose.material.icons.outlined.Lock
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
        color = MaterialTheme.colorScheme.surface // Можно сделать .surface для контраста
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
                imageVector = Icons.Outlined.Lock,
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
                            .then(
                                if (filled) Modifier.blueShadow(
                                    elevation = 8.dp,
                                    shape = CircleShape
                                ) else Modifier
                            )
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

            PinKeypad(
                onDigit = { digit ->
                    if (pin.length < 4) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        pin += digit.toString()
                    }
                },
                onDelete = {
                    if (pin.isNotEmpty()) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        pin = pin.dropLast(1)
                    }
                },
                onCancel = onCancel
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
