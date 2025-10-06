package ru.plumsoftware.notepad.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.dialog.FullscreenImageDialog
import ru.plumsoftware.notepad.ui.dialog.LoadingDialog
import ru.plumsoftware.notepad.ui.elements.NoteCard
import ru.plumsoftware.notepad.ui.formatDate
import ru.plumsoftware.notepad.ui.player.playSound
import ru.plumsoftware.notepad.ui.player.rememberExoPlayer

@Composable
fun NoteListScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    scrollToNoteId: String? = null
) {
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
    val notesToDelete = rememberSaveable(
        saver = Saver(
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
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }

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

    // Scroll to note if noteId is provided
    LaunchedEffect(scrollToNoteId, notes) {
        if (scrollToNoteId != null && notes.isNotEmpty()) {
            val index = notes.indexOfFirst { it.id == scrollToNoteId }
            if (index >= 0) {
                lazyListState.animateScrollToItem(index)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddNote.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
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
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.note_search),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth }
                            ) + fadeIn(),
                            exit = slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth }
                            ) + fadeOut()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(
                                        role = Role.Button
                                    ) {
                                        viewModel.searchNotes(searchQuery)
                                    }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(start = 16.dp, top = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                MaterialTheme.shapes.extraSmall
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
                                        kotlinx.coroutines.delay(400)
                                        viewModel.deleteNote(note, context)
                                        notesToDelete.remove(note.id)
                                    }
                                },
                                onImageClick = { path -> fullscreenImagePath = path },
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

            // Fullscreen Image Dialog
            fullscreenImagePath?.let { path ->
                FullscreenImageDialog(
                    imagePath = path,
                    onDismiss = { fullscreenImagePath = null }
                )
            }
        }
    }
}

