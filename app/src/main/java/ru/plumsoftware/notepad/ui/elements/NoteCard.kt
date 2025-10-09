package ru.plumsoftware.notepad.ui.elements

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(animationSpec = tween(durationMillis = 400)),
        exit = scaleOut(animationSpec = tween(durationMillis = 400))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .border(
                    0.dp,
                    Color(note.color.toULong()).copy(alpha = 0.15f),
                    MaterialTheme.shapes.large
                )
                .clickable { navController.navigate(Screen.EditNote.createRoute(note.id)) },
            colors = CardDefaults.cardColors(containerColor = Color(note.color.toULong())),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
//                    DeleteButton(
//                        onDelete = onDelete,
//                        enabled = true
//                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = note.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                note.tasks.forEachIndexed { index, task ->
                    var checked by remember { mutableStateOf(task.isChecked) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 12.dp,
                            alignment = Alignment.Start
                        ),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .padding(start = 6.dp)
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                checked = !checked
                                val updatedTasks = note.tasks.toMutableList().apply {
                                    this[index] = task.copy(isChecked = isChecked)
                                }
                                viewModel.updateNote(note.copy(tasks = updatedTasks), context)
                            },
                            colors = CheckboxDefaults.colors(
                                uncheckedColor = Color.White.copy(alpha = 0.7f),
                                checkedColor = Color(note.color.toULong())
                            ),
                            modifier = Modifier.size(10.dp),
                            interactionSource = MutableInteractionSource()
                        )
                        Text(
                            modifier = Modifier.clickable(
                                enabled = true,
                                indication = null,
                                interactionSource = MutableInteractionSource(),
                                onClick = {
                                    checked = !checked
                                    val updatedTasks = note.tasks.toMutableList().apply {
                                        this[index] = task.copy(isChecked = checked)
                                    }
                                    viewModel.updateNote(note.copy(tasks = updatedTasks), context)
                                }),
                            text = task.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                                color = Color.White.copy(
                                    alpha = if (task.isChecked) 0.5f else 1f
                                )
                            ),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                note.reminderDate?.let { reminderDate ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.Start
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            tint = Color.White.copy(alpha = 0.7f),
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Напоминание"
                        )
                        Text(
                            text = formatDate(reminderDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                note.reminderDate?.let {
                    if (it > 0 && note.tasks.isNotEmpty())
                        Spacer(modifier = Modifier.height(6.dp))
                }
                // Photos
                if (note.photos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        note.photos.forEach { photoPath ->
                            AsyncImage(
                                model = photoPath,
                                contentDescription = "Note Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(end = 8.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { onImageClick(photoPath) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal,
                    text = SimpleDateFormat(
                        "EEE, dd MMM yyyy",
                        Locale.getDefault()
                    ).format(Date(note.createdAt))
                )
            }
        }
    }
}