package ru.plumsoftware.notepad.ui.addnote

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.data.model.Task
import ru.plumsoftware.notepad.ui.NoteViewModel
import java.util.UUID

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    note: Note? = null
) {
    val isEditing = note != null
    var title by remember { mutableStateOf(note?.title ?: "") }
    var description by remember { mutableStateOf(note?.description ?: "") }
    var tasks by remember { mutableStateOf<MutableList<Task>>(note?.tasks?.toMutableList() ?: mutableListOf()) }
    var newTaskText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(note?.color?.toULong() ?: Color.White.value) }

    val colors = listOf(
        Color.White.value,
        Color(0xFFFFCDD2).value,
        Color(0xFFC8E6C9).value,
        Color(0xFFBBDEFB).value,
        Color(0xFFFFF9C4).value
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Note" else "Add Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val updatedNote = Note(
                                    id = note?.id ?: UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    color = selectedColor.toLong(),
                                    tasks = tasks,
                                    createdAt = note?.createdAt ?: System.currentTimeMillis()
                                )
                                if (isEditing) {
                                    viewModel.updateNote(updatedNote)
                                } else {
                                    viewModel.addNote(updatedNote)
                                }
                                navController.popBackStack()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text(if (isEditing) "Edit" else "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Tasks
            Text("Tasks", style = MaterialTheme.typography.titleMedium)
            tasks.forEachIndexed { index, task ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = task.isChecked,
                        onCheckedChange = { isChecked ->
                            tasks = tasks.toMutableList().apply {
                                this[index] = task.copy(isChecked = isChecked)
                            }
                        }
                    )
                    Text(
                        text = task.text,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        tasks = tasks.toMutableList().apply { removeAt(index) }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                    }
                }
            }

            // Add Task
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("New Task") }
                )
                IconButton(
                    onClick = {
                        if (newTaskText.isNotBlank()) {
                            tasks.add(Task(text = newTaskText))
                            newTaskText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Picker
            Text("Note Color", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                            .border(
                                width = 2.dp,
                                color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }
        }
    }
}