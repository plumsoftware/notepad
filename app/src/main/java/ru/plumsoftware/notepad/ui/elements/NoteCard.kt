package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.plumsoftware.notepad.data.model.Group
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.data.model.Tag
import ru.plumsoftware.notepad.ui.LinkTarget
import ru.plumsoftware.notepad.ui.buildNoteAnnotatedString
import ru.plumsoftware.notepad.ui.dialog.LinkActionDialog
import ru.plumsoftware.notepad.ui.formatDate
import ru.plumsoftware.notepad.ui.player.VoicePlayer
import ru.plumsoftware.notepad.ui.theme.Dimens
import ru.plumsoftware.notepad.ui.theme.resolveNoteColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IOSNoteCard(
    note: Note,
    groups: List<Group>,
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    tags: List<Tag> = emptyList(),
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onNoteUpdated: (Note) -> Unit
) {
    val groupInfo = remember(note.groupId, groups) {
        if (note.groupId == "0") null
        else groups.find { it.id == note.groupId }
    }
    val noteTags = remember(note.tagIds, tags) {
        tags.filter { note.tagIds.contains(it.id) }
    }
    var linkTarget by remember { mutableStateOf<LinkTarget?>(null) }
    var descExpanded by remember(note.id) { mutableStateOf(false) }
    var descHasOverflow by remember(note.id) { mutableStateOf(false) }

    val noteColor = resolveNoteColor(note.color)
    // Цвет текста подбираем под фон самой карточки, а не под тему —
    // так на светлой карточке текст тёмный, на тёмной — светлый (читаемо в любой теме).
    val isLightCard = noteColor.luminance() > 0.45f
    val contentColor = if (isLightCard) Color(0xFF1A1A1E) else Color.White
    val secondaryColor = contentColor.copy(alpha = 0.6f)
    val shape = MaterialTheme.shapes.large

    Surface(
        modifier = modifier
            .then(
                if (elevated) Modifier.shadow(
                    elevation = 16.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = noteColor,
                    spotColor = noteColor
                ) else Modifier
            )
            .fillMaxWidth(),
        shape = shape,
        color = noteColor,
        border = BorderStroke(Dimens.cardBorderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        ) {
        Column {
            if (note.photos.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingS),
                    contentPadding = PaddingValues(
                        start = Dimens.cardPaddingHorizontal,
                        end = Dimens.cardPaddingHorizontal,
                        top = Dimens.cardPaddingVertical
                    )
                ) {
                    items(note.photos) { photoPath ->
                        AsyncImage(
                            model = photoPath,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(200.dp)
                                .fillMaxHeight()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onImageClick(photoPath) }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(
                    horizontal = Dimens.cardPaddingHorizontal,
                    vertical = Dimens.cardPaddingVertical
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.isPinned) {
                            Icon(
                                Icons.Outlined.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeSmall),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        groupInfo?.let { group ->
                            Box(
                                modifier = Modifier
                                    .size(Dimens.dotSize)
                                    .clip(CircleShape)
                                    .background(Color(group.color.toULong()))
                            )
                        }
                    }
                    Text(
                        formatDate(note.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryColor
                    )
                }

                if (note.title.isNotBlank()) {
                    Spacer(Modifier.height(Dimens.spacingXs))
                    Text(
                        note.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (note.description.isNotBlank()) {
                    Spacer(Modifier.height(Dimens.spacingXs))
                    val annotated = remember(note.description, note.descriptionSpans) {
                        buildNoteAnnotatedString(
                            text = note.description,
                            spans = note.descriptionSpans,
                            linkColor = linkColorForCard(isLightCard),
                            onLinkClick = { linkTarget = it }
                        )
                    }
                    Text(
                        annotated,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryColor,
                        maxLines = if (descExpanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { result ->
                            if (!descExpanded) descHasOverflow = result.hasVisualOverflow
                        }
                    )
                    if (descHasOverflow && !descExpanded) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(ru.plumsoftware.notepad.R.string.show_more),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = contentColor,
                            modifier = Modifier
                                .padding(top = Dimens.spacingXs)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { descExpanded = true }
                        )
                    }
                }

                // Голосовая заметка (цвет плеера берётся из цвета заметки)
                if (!note.voicePath.isNullOrBlank()) {
                    Spacer(Modifier.height(Dimens.spacingS))
                    VoicePlayer(
                        path = note.voicePath,
                        accentColor = voiceAccentColor(noteColor, isLightCard),
                        transcription = note.voiceTranscription,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (note.tasks.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.spacingS))
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingS)) {
                        note.tasks.forEachIndexed { index, task ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val updatedTasks = note.tasks.toMutableList().apply {
                                            this[index] = task.copy(isChecked = !task.isChecked)
                                        }
                                        onNoteUpdated(note.copy(tasks = updatedTasks))
                                    }
                            ) {
                                Icon(
                                    imageVector = if (task.isChecked) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = secondaryColor,
                                    modifier = Modifier.size(Dimens.iconSizeMedium)
                                )
                                Spacer(modifier = Modifier.width(Dimens.spacingS))
                                Text(
                                    task.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (task.isChecked) secondaryColor else contentColor,
                                    textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Цветные теги заметки
                if (noteTags.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.spacingS))
                    TagChipsRow(noteTags, isLightCard)
                }

                val indicators = buildList {
                    if (note.tasks.isNotEmpty()) add(Icons.Outlined.CheckBox to "Задачи")
                    if (note.photos.isNotEmpty()) add(Icons.Outlined.Image to "Фото")
                    if (note.files.isNotEmpty()) add(Icons.Outlined.AttachFile to "Файлы")
                    if (!note.voicePath.isNullOrBlank()) add(Icons.Outlined.GraphicEq to "Голос")
                    if (note.reminderDate != null) add(Icons.Outlined.Notifications to "Напоминание")
                }
                if (indicators.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.spacingS))
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)) {
                        indicators.forEach { (icon, desc) ->
                            Icon(
                                icon,
                                desc,
                                modifier = Modifier.size(Dimens.iconSizeSmall),
                                tint = secondaryColor
                            )
                        }
                    }
                }

                if (groupInfo != null) {
                    Spacer(Modifier.height(Dimens.spacingS))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacingXs))
                        Text(
                            groupInfo.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        }
    }

    linkTarget?.let { target ->
        LinkActionDialog(target = target, onDismiss = { linkTarget = null })
    }
}

// Акцентный цвет голосового плеера, производный от цвета заметки.
private fun voiceAccentColor(noteColor: Color, isLightCard: Boolean): Color {
    val target = if (isLightCard) Color.Black else Color.White
    val fraction = if (isLightCard) 0.45f else 0.35f
    return androidx.compose.ui.graphics.lerp(noteColor, target, fraction)
}

private fun linkColorForCard(isLightCard: Boolean): Color =
    if (isLightCard) Color(0xFF2663EB) else Color(0xFF8FB6FF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagChipsRow(tags: List<Tag>, isLightCard: Boolean) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.forEach { tag ->
            val color = Color(tag.color.toULong())
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(color.copy(alpha = 0.22f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "#${tag.title}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isLightCard) androidx.compose.ui.graphics.lerp(color, Color.Black, 0.35f)
                    else androidx.compose.ui.graphics.lerp(color, Color.White, 0.25f)
                )
            }
        }
    }
}
