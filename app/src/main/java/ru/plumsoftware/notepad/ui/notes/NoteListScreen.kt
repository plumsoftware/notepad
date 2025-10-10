package ru.plumsoftware.notepad.ui.notes

import android.content.Context
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Note
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
    var listType by remember { mutableIntStateOf(getListTypeFromPreferences(context)) }

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
        containerColor = MaterialTheme.colorScheme.surface
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

                        if (searchQuery.isEmpty()) {
                            viewModel.searchNotes(searchQuery)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.note_search),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = slideInHorizontally { it } + fadeIn(),
                            exit = slideOutHorizontally { it } + fadeOut()
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.searchNotes(searchQuery)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.searchNotes(searchQuery)
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    enabled = !isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = Modifier.wrapContentSize(),
                        onClick = {
                            listType = if (listType == 0) {
                                1
                            } else {
                                0
                            }

                            saveListTypeToPreferences(listType, context)
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        if (listType == 1) {
                            FlowRow(
                                modifier = Modifier
                                    .wrapContentSize(),
                                maxLines = 2,
                                maxItemsInEachRow = 2,
                                verticalArrangement = Arrangement.spacedBy(
                                    space = 4.dp,
                                    alignment = Alignment.CenterVertically
                                ),
                                horizontalArrangement = Arrangement.spacedBy(
                                    space = 4.dp,
                                    alignment = Alignment.CenterHorizontally
                                )
                            ) {
                                Box(
                                    modifier = Modifier.width(8.dp).height(8.dp).background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                )
                                Box(
                                    modifier = Modifier.width(8.dp).height(8.dp).background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                )
                                Box(
                                    modifier = Modifier.width(8.dp).height(8.dp).background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                )
                                Box(
                                    modifier = Modifier.width(8.dp).height(8.dp).background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                )
                            }
                        } else {
                            FlowRow(
                                modifier = Modifier
                                    .wrapContentSize(),
                                maxLines = 2,
                                maxItemsInEachRow = 1,
                                verticalArrangement = Arrangement.spacedBy(
                                    space = 4.dp,
                                    alignment = Alignment.CenterVertically
                                ),
                                horizontalArrangement = Arrangement.spacedBy(
                                    space = 4.dp,
                                    alignment = Alignment.CenterHorizontally
                                )
                            ) {
                                Box(
                                    modifier = Modifier.width(20.dp).height(6.dp).background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                )
                                Box(
                                    modifier = Modifier.width(20.dp).height(6.dp).background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }
                }

                // Fixed Date Label and Notes List
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Notes List
                    if (listType == 1) {
                        // Две колонки
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
                                            delay(400)
                                            viewModel.deleteNote(note, context)
                                            notesToDelete.remove(note.id)
                                        }
                                    },
                                    onImageClick = { path -> fullscreenImagePath = path },
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(94.dp))
                            }
                        }
                    } else {
                        // Одна колонка
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 10.dp)
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
                                            delay(400)
                                            viewModel.deleteNote(note, context)
                                            notesToDelete.remove(note.id)
                                        }
                                    },
                                    onImageClick = { path -> fullscreenImagePath = path },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                                if (note.id == notes.last().id) {
                                    Spacer(modifier = Modifier.height(94.dp))
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 44.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { navController.navigate(Screen.AddNote.route) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = CircleShape,
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
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Note"
                        )
                        Text(
                            text = stringResource(R.string.note_add),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
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

fun getListTypeFromPreferences(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getInt("list_type", 0)
}

fun saveListTypeToPreferences(type: Int, context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putInt("list_type", type)
    }
}

