package ru.plumsoftware.notepad.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.glance.text.Text
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Group

@Composable
fun MoveToGroupDialog(
    groups: List<Group>,
    onDismiss: () -> Unit,
    onGroupSelected: (String) -> Unit
) {
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
                ) { onDismiss() }, // Закрытие по клику на фон
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 320.dp) // Фиксированная ширина как у алерта iOS
                    .clip(RoundedCornerShape(14.dp)) // Squircle
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                    .clickable(enabled = false) {}, // Блокируем клик сквозь
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. ЗАГОЛОВОК
                Text(
                    text = stringResource(R.string.move_to_folder),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Тонкий разделитель перед списком
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // 2. СПИСОК ПАПОК
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp) // Ограничиваем высоту, если папок много
                ) {
                    // Пункт "Все заметки" (Убрать из папки)
                    item {
                        IOSGroupItem(
                            title = stringResource(R.string.all),
                            color = null, // Дефолтная иконка
                            onClick = { onGroupSelected("0") }
                        )
                        // Разделитель с отступом слева (iOS Style inset divider)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }

                    // Список пользовательских папок
                    items(groups) { group ->
                        IOSGroupItem(
                            title = group.title,
                            color = Color(group.color.toULong()),
                            onClick = { onGroupSelected(group.id) }
                        )
                        // Разделитель для всех, кроме последнего?
                        // В LazyColumn проще рисовать для всех, визуально это не портит вид внутри скролла
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }

                // Разделитель перед кнопкой отмены
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // 3. КНОПКА ОТМЕНА
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary // Акцентный синий цвет
                    )
                }
            }
        }
    }
}

// Элемент списка внутри диалога
@Composable
private fun IOSGroupItem(
    title: String,
    color: Color?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка
        if (color != null) {
            // Цветная папка
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        } else {
            // Иконка "Все заметки" (нейтральная)
            Icon(
                imageVector = Icons.Default.AllInbox, // Или другая подходящая иконка
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Текст
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge, // 17sp стандарт iOS
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}