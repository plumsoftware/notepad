package ru.plumsoftware.notepad.ui.dialog

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.LinkTarget

@Composable
fun LinkActionDialog(
    target: LinkTarget,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val (title, message, confirmText) = when (target) {
        is LinkTarget.Url -> Triple(
            stringResource(R.string.open_link_title),
            target.url,
            stringResource(R.string.open_link_confirm)
        )
        is LinkTarget.Phone -> Triple(
            stringResource(R.string.call_number_title),
            target.number,
            stringResource(R.string.call_number_confirm)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                openLinkTarget(context, target)
                onDismiss()
            }) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

fun openLinkTarget(context: Context, target: LinkTarget) {
    try {
        val intent = when (target) {
            is LinkTarget.Url -> {
                val raw = target.url
                val normalized = if (raw.startsWith("http://") || raw.startsWith("https://")) {
                    raw
                } else {
                    "https://$raw"
                }
                Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
            }
            is LinkTarget.Phone -> {
                val digits = target.number.filter { it.isDigit() || it == '+' }
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
            }
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.cant_open_link, Toast.LENGTH_SHORT).show()
    }
}
