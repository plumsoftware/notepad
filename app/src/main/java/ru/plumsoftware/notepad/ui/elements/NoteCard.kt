package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

    val backgroundColor = Color(note.color.toULong())
    val isLight = backgroundColor.luminance() > 0.5f

    // --- ЦВЕТА TEKSTA (iOS System Colors) ---
    // Primary Label
    val contentColor = if (isLight) Color.Black.copy(alpha = 0.87f) else Color.White
    // Secondary Label (для описания и задач) - серый, но читаемый
    val secondaryColor = if (isLight) Color(0xFF3C3C43).copy(alpha = 0.6f) else Color(0xFFEBEBF5).copy(alpha = 0.6f)
    // Tertiary Label (для даты) - еще светлее
    val tertiaryColor = if (isLight) Color(0xFF3C3C43).copy(alpha = 0.3f) else Color(0xFFEBEBF5).copy(alpha = 0.3f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp, // Очень легкая тень
                shape = RoundedCornerShape(14.dp), // Squircle
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(top = 0.dp) // Обнуляем верхний отступ для фото
        ) {
            // ФОТО (если есть) - На всю ширину (Full Bleed)
            if (note.photos.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp), // Делаем фото крупнее, как обложку
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(note.photos) { photoPath ->
                        AsyncImage(
                            model = photoPath,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(200.dp) // Фиксированная ширина одного фото
                                .fillMaxHeight()
                                .clickable { onImageClick(photoPath) }
                        )
                        // Тонкий разделитель между фото
                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(0.2f)))
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) { // Внутренний паддинг контента
                // ЗАГОЛОВОК (Title 3 / Headline)
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium, // 20sp SemiBold (из настроек выше)
                        color = contentColor,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ОПИСАНИЕ (Body)
                if (note.description.isNotBlank()) {
                    Text(
                        text = note.description,
                        style = MaterialTheme.typography.bodyLarge, // 17sp Regular
                        color = secondaryColor, // Серый цвет, как в Notes.app
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
                                    .fillMaxWidth(0.6f)
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
                                    imageVector = if (task.isChecked) Icons.Default.CheckCircle else Icons.Outlined.Circle, // Круглый контур или залитый круг
                                    contentDescription = null,
                                    tint = if (task.isChecked) secondaryColor else contentColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = task.text,
                                    style = MaterialTheme.typography.bodyLarge, // 17sp
                                    color = if (task.isChecked) tertiaryColor else contentColor, // Бледнеет при выполнении
                                    textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ФУТЕР (Caption)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.wrapContentWidth(),
                        text = formatDate(note.createdAt),
                        style = MaterialTheme.typography.labelMedium, // 12sp
                        color = tertiaryColor // Самый бледный цвет
                    )

                    if (groupName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
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