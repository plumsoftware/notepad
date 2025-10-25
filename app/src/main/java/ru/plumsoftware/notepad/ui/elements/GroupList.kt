package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Group

@Composable
fun GroupList(
    groups: List<Group>,
    selectedGroupId: String?,
    onGroupSelected: (String) -> Unit,
    onCreateGroup: (title: String, color: ULong) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, color ->
                showCreateDialog = false
                onCreateGroup(title, color)
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Spacer(modifier = Modifier.width(8.dp)) // Отступ для градиента
            }

            item {
                GroupItem(
                    isAdd = true,
                    onClick = {
                        showCreateDialog = true
                    },
                    group = null,
                    isSelected = false
                )
            }

            item {
                GroupItem(
                    isAll = true,
                    onClick = { onGroupSelected("0") },
                    group = null,
                    isSelected = selectedGroupId == "0"
                )
            }

            items(groups) { group ->
                GroupItem(
                    onClick = { onGroupSelected(group.id) },
                    group = group,
                    isSelected = selectedGroupId == group.id
                )
            }

            item {
                Spacer(modifier = Modifier.width(8.dp)) // Отступ для градиента
            }
        }

        // Градиент в начале (слева)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(10.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Градиент в конце (справа)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(10.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
    }
}

@Composable
fun GroupItem(
    isAdd: Boolean = false,
    isAll: Boolean = false,
    onClick: () -> Unit,
    group: Group?,
    isSelected: Boolean
) {
    val borderColor = when {
        isAdd -> Color.Transparent // никакой обводки
        isAll -> if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
        else -> if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent // или surfaceContainer, если хочешь
        }
    }

    val borderWidth = if (isAdd) 0.dp else 2.dp

    OutlinedButton(
        modifier = Modifier.wrapContentSize(),
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = when {
                isAdd -> MaterialTheme.colorScheme.primary
                isAll -> Color.Transparent
                else -> Color((group?.color ?: 0L).toULong())
            },
            contentColor = when {
                isAdd -> MaterialTheme.colorScheme.onPrimary
                isAll -> if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                else -> Color.White
            }
        ),
        border = BorderStroke(
            width = borderWidth,
            color = borderColor
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAdd) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_group),
                    tint = Color.White
                )
                Text(
                    text = stringResource(R.string.add_group),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White
                    )
                )
            } else if (isAll) {
                Text(
                    text = stringResource(R.string.all),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )
            } else {
                // Отображение названия обычной группы
                Text(
                    text = group?.title ?: "",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, color: ULong) -> Unit
) {
    val colors = listOf(
        Color(0xFF81C784).value,
        Color(0xFF4DB6AC).value,
        Color(0xFFF48FB1).value,
        Color(0xFFFF8A65).value,
        Color(0xFF64B5F6).value,
        Color(0xFF7986CB).value,
        Color(0xFFAB47BC).value,
        Color(0xFF9575CD).value,
        Color(0xFFFFCA28).value,
        Color(0xFFF9A825).value,
        Color(0xFF90A4AE).value,
        Color(0xFFD7CCC8).value,
        Color(0xFF283593).value,
        Color(0xFF37474F).value,
        Color(0xFF1A237E).value
    )

    var title by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(colors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.create_group),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.enter_group_name)) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    trailingIcon = {
                        if (title.isNotEmpty()) {
                            IconButton(onClick = { title = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.group_color),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    colors.forEach { colorValue ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(colorValue))
                                .border(
                                    width = 2.dp,
                                    color = if (selectedColor == colorValue) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorValue }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), selectedColor)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.create),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}