package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.plumsoftware.notepad.data.model.Group
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.ui.formatDate

@Composable
fun IOSNoteCard(
    note: Note,
    groups: List<Group>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onNoteUpdated: (Note) -> Unit
) {
    // 1. Имя группы
    val groupName = remember(note.groupId, groups) {
        if (note.groupId == "0") null
        else groups.find { it.id == note.groupId }?.title
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()

    val backgroundColor = Color(note.color.toULong())
    val isLight = backgroundColor.luminance() > 0.5f

    // --- ЦВЕТА TEXTA ---
    val contentColor = if (isLight) Color.Black.copy(alpha = 0.87f) else Color.White
    val secondaryColor = if (isLight) Color(0xFF3C3C43).copy(alpha = 0.6f) else Color(0xFFEBEBF5).copy(alpha = 0.6f)
    val tertiaryColor = if (isLight) Color(0xFF3C3C43).copy(alpha = 0.3f) else Color(0xFFEBEBF5).copy(alpha = 0.3f)

    val displayedBackgroundColor = if (note.color == 0xFFFFFFFF.toLong() && isSystemInDarkTheme) {
        Color(0xFF1C1C1E) // Цвет iOS карточки в dark mode
    } else {
        backgroundColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = displayedBackgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(top = 0.dp)
        ) {
            // ФОТО
            if (note.photos.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Немного уменьшил высоту, так как теперь есть padding вокруг фото
                        .height(190.dp),
                    // Добавляем отступы между элементами
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    // Добавляем отступы содержимого от краев карточки (14dp, как у текста ниже)
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 0.dp)
                ) {
                    items(note.photos) { photoPath ->
                        AsyncImage(
                            model = photoPath,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(200.dp)
                                .fillMaxHeight()
                                // Скругляем углы у самой картинки
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick(photoPath) }
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                // ЗАГОЛОВОК
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ОПИСАНИЕ
                if (note.description.isNotBlank()) {
                    Text(
                        text = note.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = secondaryColor,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ЗАДАЧИ
                if (note.tasks.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        note.tasks.forEachIndexed { index, task ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier
                                    .fillMaxWidth() // Можно оставить 0.6f, если хочешь короткие
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val newStatus = !task.isChecked
                                        val updatedTasks = note.tasks.toMutableList().apply {
                                            this[index] = task.copy(isChecked = newStatus)
                                        }
                                        onNoteUpdated(note.copy(tasks = updatedTasks))
                                    }
                            ) {
                                Icon(
                                    imageVector = if (task.isChecked) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = if (task.isChecked) secondaryColor else contentColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = task.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (task.isChecked) tertiaryColor else contentColor,
                                    textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ФУТЕР
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.wrapContentWidth(),
                        text = formatDate(note.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = tertiaryColor
                    )

                    if (groupName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                // Вектор или painterResource
                                imageVector = Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = tertiaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                modifier = Modifier.wrapContentWidth(),
                                text = groupName,
                                style = MaterialTheme.typography.labelMedium,
                                color = tertiaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}