package ru.plumsoftware.notepad.ui.notes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.dialog.LoadingDialog
import ru.plumsoftware.notepad.ui.elements.NoteCard
import ru.plumsoftware.notepad.ui.formatDate
import ru.plumsoftware.notepad.ui.player.playSound
import ru.plumsoftware.notepad.ui.player.rememberExoPlayer

@Composable
fun NoteListScreen(navController: NavController, viewModel: NoteViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lazyListState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val currentDate by remember {
        derivedStateOf {
            notes.getOrNull(firstVisibleItemIndex)?.createdAt?.let { formatDate(it) } ?: ""
        }
    }
    val scale = remember { Animatable(1f) }
    val notesToDelete = rememberSaveable(saver = Saver(
        save = { map ->
            map.mapValues { if (it.value) 1 else 0 }.toList().toTypedArray()
        },
        restore = { array ->
            mutableStateMapOf<String, Boolean>().apply {
                array.toMap().forEach { (key, value) -> put(key, value == 1) }
            }
        }
    )) { mutableStateMapOf<String, Boolean>() }
    val coroutineScope = rememberCoroutineScope()
    val exoPlayer = rememberExoPlayer()
    val context = LocalContext.current

    // Trigger animation for date change
    LaunchedEffect(currentDate) {
        if (currentDate.isNotEmpty()) {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 200)
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 200)
            )
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddNote.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note", tint = Color.White)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        viewModel.searchNotes(query)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Поиск заметок...") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.clickable { viewModel.searchNotes(searchQuery) }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    ),
                    enabled = !isLoading
                )

                // Fixed Date Label and Notes List
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Fixed Date Label
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(start = 16.dp, top = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .scale(scale.value)
                    )

                    // Notes List
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 28.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                viewModel = viewModel,
                                navController = navController,
                                isVisible = notesToDelete[note.id] != true,
                                onDelete = {
                                    notesToDelete[note.id] = true
                                    playSound(context, exoPlayer, R.raw.note_delete)
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(200)
                                        viewModel.deleteNote(note)
                                        notesToDelete.remove(note.id)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
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

