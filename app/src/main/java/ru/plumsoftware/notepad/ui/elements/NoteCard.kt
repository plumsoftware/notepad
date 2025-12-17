package ru.plumsoftware.notepad.ui.elements

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Group
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.formatDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun NoteCard(
    note: Note,
    viewModel: NoteViewModel,
    navController: NavController,
    isVisible: Boolean,
    onDelete: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    groups: List<Group>,
    onGroupSelected: (Note, String) -> Unit
) {
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val showMoveToFolderDialog = remember { mutableStateOf(false) }

    // 1. Определяем базовый цвет фона
    val backgroundColor = Color(note.color.toULong())

    // 2. Умный контраст: Если фон светлый -> текст черный, иначе -> белый
    // luminance возвращает яркость от 0.0 до 1.0. Порог 0.5 - золотая середина.
    val isLightBackground = backgroundColor.luminance() > 0.5f
    val contentColor = if (isLightBackground) Color(0xFF1C1B1F) else Color.White
    val secondaryContentColor = contentColor.copy(alpha = 0.7f)
    val tertiaryContentColor = contentColor.copy(alpha = 0.5f)

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(animationSpec = tween(durationMillis = 300)) + fadeIn(),
        exit = scaleOut(animationSpec = tween(durationMillis = 300)) + fadeOut()
    ) {
        Box(modifier = modifier) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp) // Небольшой внешний отступ
                    .clip(MaterialTheme.shapes.large) // Более аккуратные углы
                    .clickable { navController.navigate(Screen.EditNote.createRoute(note.id)) },
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp) // Внутренние отступы
                ) {
                    // --- Header: Title ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold, // Не Black, просто Bold
                                lineHeight = 28.sp
                            ),
                            color = contentColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 24.dp), // Чтобы текст не наезжал на кнопку меню
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Меню будет позиционироваться Box-ом выше, но отступ мы учли
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- Description ---
                    if (note.description.isNotBlank()) {
                        Text(
                            text = note.description,
                            style = MaterialTheme.typography.bodyLarge, // Обычный текст, не заголовок
                            color = secondaryContentColor,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // --- Tasks ---
                    if (note.tasks.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Показываем максимум 4 задачи, чтобы карточка не была бесконечной
                            note.tasks.take(4).forEachIndexed { index, task ->
                                var checked by remember { mutableStateOf(task.isChecked) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            checked = isChecked
                                            val updatedTasks = note.tasks.toMutableList().apply {
                                                this[index] = task.copy(isChecked = isChecked)
                                            }
                                            viewModel.updateNote(note.copy(tasks = updatedTasks), context)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = contentColor, // Квадратик цвета текста
                                            uncheckedColor = secondaryContentColor,
                                            checkmarkColor = backgroundColor // Галочка цвета фона (вырез)
                                        ),
                                        modifier = Modifier.size(18.dp) // Чуть аккуратнее
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = task.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = if (checked) TextDecoration.LineThrough else null
                                        ),
                                        color = if (checked) tertiaryContentColor else contentColor,
                                        modifier = Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            checked = !checked
                                            val updatedTasks = note.tasks.toMutableList().apply {
                                                this[index] = task.copy(isChecked = checked)
                                            }
                                            viewModel.updateNote(note.copy(tasks = updatedTasks), context)
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (note.tasks.size > 4) {
                                Text(
                                    text = "+ ещё ${note.tasks.size - 4}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tertiaryContentColor,
                                    modifier = Modifier.padding(start = 30.dp, top = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // --- Photos ---
                    if (note.photos.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(note.photos) { photoPath ->
                                AsyncImage(
                                    model = photoPath,
                                    contentDescription = "Note Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(70.dp) // Чуть компактнее
                                        .clip(MaterialTheme.shapes.medium) // Скругление фото
                                        .clickable { onImageClick(photoPath) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // --- Footer: Date & Reminder ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reminder Chip
                        if (note.reminderDate != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        color = Color.Black.copy(alpha = 0.05f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDate(note.reminderDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp)) // Spacer для выравнивания даты справа
                        }

                        // Created Date
                        Text(
                            text = formatDate(note.createdAt), // Используем твою функцию helper
                            style = MaterialTheme.typography.labelSmall,
                            color = tertiaryContentColor
                        )
                    }
                }
            }

            // --- Menu Button (Overlay) ---
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = {
                        expanded.value = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = contentColor // Цвет иконки подстраивается под фон
                    )
                }

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer, // Меню на стандартном фоне
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.go)) },
                        onClick = {
                            expanded.value = false
                            navController.navigate(Screen.EditNote.createRoute(note.id))
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.move_to_folder)) },
                        onClick = {
                            expanded.value = false
                            showMoveToFolderDialog.value = true
                        },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded.value = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }

    // Dialog logic remains the same...
    if (showMoveToFolderDialog.value) {
        // ... твой код диалога без изменений, он нормальный ...
        AlertDialog(
            onDismissRequest = { showMoveToFolderDialog.value = false },
            title = {
                Text(
                    text = stringResource(R.string.move_to_folder),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GroupItem(
                            isAll = true,
                            onClick = {
                                showMoveToFolderDialog.value = false
                                onGroupSelected(note, "0")
                            },
                            group = null,
                            isSelected = true
                        )
                        groups.forEach { group ->
                            GroupItem(
                                onClick = {
                                    showMoveToFolderDialog.value = false
                                    onGroupSelected(note, group.id)
                                },
                                group = group,
                                isSelected = false
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}