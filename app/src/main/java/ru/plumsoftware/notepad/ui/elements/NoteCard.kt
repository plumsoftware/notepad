package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.ui.NoteViewModel

@Composable
fun NoteCard(note: Note, viewModel: NoteViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(note.color.toULong()))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            note.tasks.forEachIndexed { index, task ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = task.isChecked,
                        onCheckedChange = { isChecked ->
                            val updatedTasks = note.tasks.toMutableList().apply {
                                this[index] = task.copy(isChecked = isChecked)
                            }
                            viewModel.updateNote(note.copy(tasks = updatedTasks))
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = task.text,
                        style = TextStyle(
                            fontSize = 14.sp,
                            textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (task.isChecked) 0.5f else 1f
                            )
                        )
                    )
                }
            }
        }
    }
}