package ru.plumsoftware.notepad.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun PermissionRationaleDialog(
    permission: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        shape = MaterialTheme.shapes.small,
        onDismissRequest = onDismiss,
        title = { Text("Требуется разрешение") },
        text = {
            Text(
                when (permission) {
                    android.Manifest.permission.POST_NOTIFICATIONS -> "Разрешение на отправку уведомлений необходимо для напоминаний о заметках."
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Разрешение на запись необходимо для сохранения фотографий."
                    android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.READ_MEDIA_IMAGES -> "Разрешение на чтение хранилища необходимо для добавления фотографий."
                    else -> "Это разрешение необходимо для работы приложения."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Предоставить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}