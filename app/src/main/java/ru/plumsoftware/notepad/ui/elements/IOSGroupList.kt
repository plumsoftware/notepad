package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.database.GroupWithCount
import ru.plumsoftware.notepad.data.model.Group

@Composable
fun IOSGroupList(
    groups: List<GroupWithCount>, // Используем новый тип с данными
    selectedGroupId: String?,
    totalCount: Int,
    onGroupSelected: (String) -> Unit,
    onCreateGroup: () -> Unit, // Упростили коллбэк, диалог внутри экрана
    onDeleteGroup: (Group) -> Unit
) {
    val scrollState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    // Определяем ширину экрана для градиентов
    // (можно убрать градиенты, в iOS 15+ их часто нет, но оставим для эстетики)

    LazyRow(
        state = scrollState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // Отступы сверху/снизу от поиска
        horizontalArrangement = Arrangement.spacedBy(10.dp), // Отступ между капсулами
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp)   // Отступ списка от краев
    ) {
        // 1. Кнопка создания новой группы (+)
        item {
            IOSActionChip(
                icon = Icons.Default.Add,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCreateGroup()
                }
            )
        }

        // Разделитель (вертикальная черта), как в некоторых муз. приложениях
        item {
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
//                    .align(Alignment.CenterVertically)
            )
        }

        // 2. Кнопка "Все"
        item {
            IOSGroupChip(
                title = stringResource(R.string.all), // "Все"
                count = totalCount,
                isSelected = selectedGroupId == "0",
                color = null, // Для "Всех" нет спец цвета, будет черный/белый
                onClick = {
                    onGroupSelected("0")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
        }

        // 3. Список групп
        items(groups, key = { it.group.id }) { item ->
            IOSGroupChip(
                title = item.group.title,
                count = item.noteCount,
                isSelected = selectedGroupId == item.group.id,
                color = Color(item.group.color.toULong()), // Цвет самой категории (точки)
                onClick = {
                    onGroupSelected(item.group.id)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onLongClick = {
                    onDeleteGroup(item.group)
                }
            )
        }
    }
}

@Composable
fun IOSGroupChip(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    // Анимация цветов
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 200), label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 200), label = "textColor"
    )

    Box(
        modifier = Modifier
            .height(36.dp) // Высота стандартной iOS кнопки
            .clip(CircleShape)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick?.invoke() }
                )
            }
            .padding(horizontal = 14.dp), // Внутренний отступ текста
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Если у группы есть свой цвет (точка), показываем её только в неактивном состоянии
            // (в активном всё черно-белое для стиля)
            if (color != null && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Название группы
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = contentColor
            )

            // Счетчик заметок (отображаем, если больше 0)
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) contentColor.copy(alpha = 0.7f) else contentColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// Маленькая круглая кнопка для действия (+ добавить)
@Composable
fun IOSActionChip(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp) // Квадратная (круглая)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) // Серый фон
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, // Синий iOS акцент
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun IOSCreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, color: ULong) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }


    val colors = listOf(
        Color(0xFF4DB6AC), // Teal
        Color(0xFF81C784), // Green
        Color(0xFFFFB74D), // Orange
        Color(0xFFE57373), // Red
        Color(0xFFF06292), // Pink
        Color(0xFFBA68C8), // Purple
        Color(0xFF64B5F6), // Blue
        Color(0xFF4DD0E1), // Cyan
        Color(0xFFA1887F), // Brown
        Color(0xFF90A4AE)  // Blue Grey
    )

    // Выбранный цвет по умолчанию
    var selectedColor by remember { mutableStateOf(colors.first()) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 320.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                    .clickable(enabled = false) {},
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ЗАГОЛОВОК
                Text(
                    text = "Новая папка",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Введите название для этой папки",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ПОЛЕ ВВОДА
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (title.isEmpty()) {
                                    Text(
                                        text = "Название",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                                innerTextField()
                            }
                            if (title.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { title = "" }
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ВЫБОР ЦВЕТА
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(colors) { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color) // Используем объект Color напрямую
                                .clickable { selectedColor = color }
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        // Белая точка если цвет темный, черная если светлый
                                        .background(if (color.luminance() < 0.5) Color.White else Color.Black.copy(alpha = 0.5f))
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(modifier = Modifier.height(48.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Отмена",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = title.isNotBlank()) {
                                if (title.isNotBlank()) {
                                    // 🔥 Отправляем color.value (ULong) в колбэк
                                    onCreate(title, selectedColor.value)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Создать",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (title.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

// Утилитная функция, если её нет (чтобы импорты работали)
@Composable
fun luminance(color: Int): Float {
    val r = android.graphics.Color.red(color) / 255.0
    val g = android.graphics.Color.green(color) / 255.0
    val b = android.graphics.Color.blue(color) / 255.0
    return (0.2126 * r + 0.7152 * g + 0.0722 * b).toFloat()
}