package ru.plumsoftware.notepad.ui.addnote

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.filesaver.deleteImagesFromStorage
import ru.plumsoftware.notepad.data.filesaver.saveImageToInternalStorage
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.data.model.Task
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.dialog.FullscreenImageDialog
import ru.plumsoftware.notepad.ui.dialog.LoadingDialog
import ru.plumsoftware.notepad.ui.formatDate
import ru.plumsoftware.notepad.ui.player.playSound
import ru.plumsoftware.notepad.ui.player.rememberExoPlayer
import java.util.Calendar
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
    var tasks by remember {
        mutableStateOf<MutableList<Task>>(
            note?.tasks?.toMutableList() ?: mutableListOf()
        )
    }
    var newTaskText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(note?.color?.toULong() ?: Color.White.value) }
    var isReminder by remember { mutableStateOf(note?.reminderDate != null) }
    var reminderDate by remember { mutableStateOf(note?.reminderDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempSelectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var photos by remember { mutableStateOf<List<String>>(note?.photos ?: emptyList()) }
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val exoPlayer = rememberExoPlayer()
    val context = LocalContext.current

    val pickImages =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.size + photos.size <= 3) {
                photos = photos.toMutableList().apply {
                    uris.forEach { uri ->
                        saveImageToInternalStorage(context, uri)?.let { path -> add(path) }
                    }
                }
            }
        }

    val colors = listOf(
        Color.White.value,
        Color(0xFFFFCDD2).value,
        Color(0xFFC8E6C9).value,
        Color(0xFFBBDEFB).value,
        Color(0xFFFFF9C4).value
    )

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = reminderDate ?: System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        tempSelectedDateMillis = datePickerState.selectedDateMillis
                        showTimePicker = true
                    }
                ) { Text("OK", style = MaterialTheme.typography.labelMedium) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        isReminder = false
                        reminderDate = null
                    }
                ) { Text("Отмена", style = MaterialTheme.typography.labelMedium) }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp),
                title = {
                    Text(
                        "Когда напомнить о событии",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(text = "Выберите время", style = MaterialTheme.typography.titleSmall) },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        tempSelectedDateMillis?.let { dateMillis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = dateMillis
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            reminderDate = calendar.timeInMillis
                        }
                    }
                ) { Text("OK", style = MaterialTheme.typography.labelMedium) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        isReminder = false
                        reminderDate = null
                    }
                ) { Text("Отмена", style = MaterialTheme.typography.labelMedium) }
            }
        )
    }

    // Fullscreen Image Dialog
    fullscreenImagePath?.let { path ->
        FullscreenImageDialog(
            imagePath = path,
            onDismiss = { fullscreenImagePath = null }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Редактировать заметку" else "Добавить заметку",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        shape = MaterialTheme.shapes.extraLarge,
                        onClick = {
                            if (title.isNotBlank()) {
                                val updatedNote = Note(
                                    id = note?.id ?: UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    color = selectedColor.toLong(),
                                    tasks = tasks,
                                    createdAt = note?.createdAt ?: System.currentTimeMillis(),
                                    reminderDate = if (isReminder) reminderDate else null,
                                    photos = photos
                                )
                                if (isEditing) {
                                    if (note.photos != photos) {
                                        deleteImagesFromStorage(
                                            context,
                                            note.photos.filterNot { photos.contains(it) })
                                    }
                                    playSound(context, exoPlayer, R.raw.note_create) //note_edit
                                    viewModel.updateNote(updatedNote, context)
                                } else {
                                    playSound(context, exoPlayer, R.raw.note_create)
                                    viewModel.addNote(updatedNote)
                                }
                                navController.popBackStack()
                            }
                        },
                        enabled = title.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = if (isEditing) "Сохранить" else "Добавить",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge,
                    placeholder = {
                        Text(
                            text = "Заголовок",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.background(Color.Transparent)
                        )
                    },
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = {
                        Text(
                            text = "Описание",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.background(Color.Transparent)
                        )
                    },
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isReminder,
                        onCheckedChange = { checked ->
                            isReminder = checked
                            if (checked && reminderDate == null) {
                                showDatePicker = true
                            } else if (!checked) {
                                reminderDate = null
                            }
                        },
                        enabled = !isLoading,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(text = "Напомнить", style = MaterialTheme.typography.bodyMedium)
                }
                // Display selected reminder date
                reminderDate?.let {
                    Text(
                        text = "Напоминание: ${formatDate(it)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }

                // Photos
                Text("Фотографии", style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    photos.forEach { photoPath ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .padding(end = 8.dp)
                        ) {
                            AsyncImage(
                                model = photoPath,
                                contentDescription = "Note Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { fullscreenImagePath = photoPath }
                            )
                            IconButton(
                                onClick = {
                                    photos = photos.toMutableList().apply { remove(photoPath) }
                                    deleteImagesFromStorage(context, listOf(photoPath))
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Photo",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    if (photos.size < 3) {
                        IconButton(
                            onClick = { pickImages.launch("image/*") },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Photo")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tasks
                Text("Задачи", style = MaterialTheme.typography.bodyLarge)
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
                            },
                            enabled = !isLoading,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = task.text,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                tasks = tasks.toMutableList().apply { removeAt(index) }
                            },
                            enabled = !isLoading
                        ) {
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
                        textStyle = MaterialTheme.typography.bodyMedium,
                        label = {
                            Text(
                                text = "Новая задача",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        enabled = !isLoading
                    )
                    IconButton(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                tasks.add(Task(text = newTaskText))
                                newTaskText = ""
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Picker
                Text("Цвет заметки", style = MaterialTheme.typography.bodyLarge)
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
                                .clickable(enabled = !isLoading) { selectedColor = color }
                        )
                    }
                }
            }

            // Loading Dialog
            if (isLoading) {
                LoadingDialog()
            }
        }
    }
}