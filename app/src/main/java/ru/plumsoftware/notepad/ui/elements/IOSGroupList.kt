package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.database.GroupWithCount
import ru.plumsoftware.notepad.data.model.Group
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.theme.Dimens

@Composable
fun IOSGroupList(
    groups: List<GroupWithCount>,
    selectedGroupId: String?,
    totalCount: Int,
    secretCount: Int,
    onGroupSelected: (String) -> Unit,
    onSecureClick: () -> Unit,
    onCreateGroup: () -> Unit,
    onDeleteGroup: (Group) -> Unit
) {
    val scrollState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenPaddingHorizontal)
            .padding(bottom = Dimens.spacingM),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        LazyRow(
            state = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingXs),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FolderAddChip(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCreateGroup()
                    }
                )
            }

            item {
                FolderChip(
                    title = stringResource(R.string.folder_all),
                    count = totalCount,
                    isSelected = selectedGroupId == "0",
                    color = null,
                    onClick = {
                        onGroupSelected("0")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            }

            item {
                FolderSecureChip(
                    count = secretCount,
                    isSelected = selectedGroupId == NoteViewModel.SECURE_FOLDER_ID,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSecureClick()
                    }
                )
            }

            items(groups, key = { it.group.id }) { item ->
                FolderChip(
                    title = item.group.title,
                    count = item.noteCount,
                    isSelected = selectedGroupId == item.group.id,
                    color = Color(item.group.color.toULong()),
                    onClick = {
                        onGroupSelected(item.group.id)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onLongClick = { onDeleteGroup(item.group) }
                )
            }
        }
    }
}

@Composable
private fun FolderAddChip(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacingM),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_group),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconSizeMedium)
        )
    }
}

@Composable
fun FolderSecureChip(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FolderChip(
        title = stringResource(R.string.secure_folder),
        count = count,
        isSelected = isSelected,
        color = null,
        leadingIcon = Icons.Default.Lock,
        onClick = onClick
    )
}

@Composable
fun FolderChip(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.Transparent
        },
        animationSpec = tween(200),
        label = "folderBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "folderText"
    )

    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick?.invoke() }
                )
            }
            .padding(horizontal = Dimens.spacingM),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isSelected) {
                        contentColor
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.spacingXs))
            }

            if (color != null && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(Dimens.dotSize)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(Dimens.spacingS))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )

            if (count > 0) {
                Spacer(modifier = Modifier.width(Dimens.spacingXs))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Алиасы для обратной совместимости
@Composable
fun IOSSecureGroupChip(count: Int, isSelected: Boolean, onClick: () -> Unit) =
    FolderSecureChip(count, isSelected, onClick)

@Composable
fun IOSGroupChip(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) = FolderChip(title, count, isSelected, color, onClick, onLongClick)

@Composable
fun IOSActionChip(icon: ImageVector, onClick: () -> Unit) = FolderAddChip(onClick)

@Composable
fun IOSCreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, color: ULong) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val colors = listOf(
        Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFFFFB74D), Color(0xFFE57373),
        Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF64B5F6), Color(0xFF4DD0E1),
        Color(0xFFA1887F), Color(0xFF90A4AE)
    )
    var selectedColor by remember { mutableStateOf(colors.first()) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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
                Text(
                    text = stringResource(R.string.create_group),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.enter_group_name),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                                        text = stringResource(R.string.enter_group_name),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                                innerTextField()
                            }
                            if (title.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = stringResource(R.string.clear),
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
                                .background(color)
                                .clickable { selectedColor = color }
                        ) {
                            if (isSelected) {
                                val isDark = color.luminance() < 0.5f
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color.White else Color.Black.copy(alpha = 0.5f))
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
                            text = stringResource(R.string.cancel),
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
                                    onCreate(title, selectedColor.value)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.create),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (title.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
