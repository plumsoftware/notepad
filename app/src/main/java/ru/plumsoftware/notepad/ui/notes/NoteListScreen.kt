package ru.plumsoftware.notepad.ui.notes

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.App
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Group
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.dialog.FullscreenImageDialog
import ru.plumsoftware.notepad.ui.dialog.LoadingDialog
import ru.plumsoftware.notepad.ui.elements.IOSGroupList
import ru.plumsoftware.notepad.ui.formatDate
import ru.plumsoftware.notepad.ui.player.playSound
import ru.plumsoftware.notepad.ui.player.rememberExoPlayer
import androidx.core.net.toUri
import ru.plumsoftware.notepad.ui.MainScreenRouteState
import ru.plumsoftware.notepad.ui.dialog.MoveToGroupDialog
import ru.plumsoftware.notepad.ui.elements.BottomBar
import ru.plumsoftware.notepad.ui.elements.CalendarView
import ru.plumsoftware.notepad.ui.elements.IOSCreateGroupDialog
import ru.plumsoftware.notepad.ui.elements.IOSNoteCard
import ru.plumsoftware.notepad.ui.elements.RateAppBottomSheet
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    scrollToNoteId: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val notes by viewModel.notes.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lazyListState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    var needToBlur by remember { mutableStateOf(false) }
    var noteToMove by remember { mutableStateOf<Note?>(null) }

    val isSearchBarVisible by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemScrollOffset < 150 && lazyListState.firstVisibleItemIndex == 0
        }
    }

    val needToShowRateDialog by viewModel.needToShowRateDialog.collectAsState()
    var showRateBottomSheet by remember { mutableStateOf(false) }

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
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(0) }
    val previousNotesCount = remember { mutableStateOf(0) }
    val selectedGroupId by viewModel.selectedGroupId.collectAsState() // "0" = "All"
    var mainScreenState by remember { mutableStateOf (MainScreenRouteState.Main) }
    val instaOpenAddNoteScreen = viewModel.openAddNoteScreen.collectAsState()
    val totalNotesCount by viewModel.totalNotesCount.collectAsState()

    var showCreateGroupDialog by remember { mutableStateOf(false) }

    // Добавляем состояние для BottomSheet меню
    var showMenuBottomSheet by remember { mutableStateOf(false) }

    var isSearchFocused by remember { mutableStateOf(false) }

    // Добавляем FocusManager для управления фокусом
    val focusManager = LocalFocusManager.current

    // ПРОВЕРКА ДЛЯ ПОКАЗА ДИАЛОГА ОЦЕНКИ
    LaunchedEffect(notes.size) {
        // Проверяем условия для показа диалога оценки
        if (notes.size > 1) {
            viewModel.checkShouldShowRateDialog(notes.size)
        }

        delay(100L)
        if (notes.size > previousNotesCount.value && notes.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
        }
        previousNotesCount.value = notes.size
    }

    // ПОКАЗ ДИАЛОГА ОЦЕНКИ КОГДА УСЛОВИЯ ВЫПОЛНЕНЫ
    LaunchedEffect(needToShowRateDialog) {
        needToBlur = needToShowRateDialog
        if (needToShowRateDialog) {
            showMenuBottomSheet = false
            // Небольшая задержка для лучшего UX
            delay(500L)
            showRateBottomSheet = true
        }
    }

    LaunchedEffect(key1 = showMenuBottomSheet) {
        needToBlur = showMenuBottomSheet
    }

    LaunchedEffect(key1 = showFilterDialog) {
        needToBlur = showFilterDialog
    }

    LaunchedEffect(key1 = mainScreenState) {
        viewModel.loading(true)
        delay(500L)
        viewModel.loading(false)
    }

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

    LaunchedEffect(selectedGroupId) {
        viewModel.searchNotes("")
    }

    val displayedNotes = remember(notes, selectedFilter) {
        when (selectedFilter) {
            0 -> notes.sortedByDescending { it.createdAt }
            1 -> notes.sortedBy { it.createdAt }
            2 -> notes.filter { it.reminderDate != null }
            3 -> notes.filter { it.photos.isNotEmpty() }
            4 -> notes.filter { it.tasks.isNotEmpty() }
            else -> notes
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

    LaunchedEffect(key1 = instaOpenAddNoteScreen) {
        if (instaOpenAddNoteScreen.value) {
            navController.navigate(Screen.AddNote.route)
        }
    }

    // Применение фильтрации к заметкам
    val filteredNotes = remember(notes, selectedFilter) {
        when (selectedFilter) {
            0 -> notes.sortedByDescending { it.createdAt }
            1 -> notes.sortedBy { it.createdAt }
            2 -> notes.filter { it.reminderDate != null }
            3 -> notes.filter { it.photos.isNotEmpty() }
            4 -> notes.filter { it.tasks.isNotEmpty() }
            else -> notes
        }
    }

    // BottomSheet для меню
    if (showMenuBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Black.copy(alpha = 0.5f) // Затемнение фона
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Опция "О приложении"
                MenuOption(
                    text = stringResource(R.string.about_app),
                    onClick = {
                        showMenuBottomSheet = false
                        // Переход на экран "О приложении"
                        navController.navigate(Screen.AboutApp.route)
                    }
                )

                // Разделитель
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                // Опция "Настройки"
                MenuOption(
                    text = stringResource(R.string.settings),
                    onClick = {
                        showMenuBottomSheet = false
                        // Переход на экран настроек
                        navController.navigate(Screen.Settings.route)
                    }
                )

                Spacer(modifier = Modifier.height(46.dp))
            }
        }
    }

    if (showRateBottomSheet) {
        RateAppBottomSheet(
            onDismiss = {
                showRateBottomSheet = false
                // При закрытии диалога без оценки сбрасываем флаг
                viewModel.setNeedToShowRateDialog(false)
            },
            onRateConfirmed = {
                showRateBottomSheet = false
                // Пользователь оценил приложение
                viewModel.setAppRated()
                coroutineScope.launch {
                    delay(2000L)
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                App.platformConfig.rateUrl.toUri()
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                App.platformConfig.rateUrl.toUri()
                            )
                        )
                    }
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.blur(
            radius = if (needToBlur) 10.dp else 0.dp
        ),
        bottomBar = {
            BottomBar(
                navController = navController,
                onHomeClick = {
                    mainScreenState = MainScreenRouteState.Main
                },
                onCalendarClick = {
                    mainScreenState = MainScreenRouteState.Calendar
                }
            )
        },
        floatingActionButton = {
            // ... код без изменений ...
        }
    ) { padding ->
        // Добавляем обработчик клика вне текстового поля
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (isSearchFocused) {
                        focusManager.clearFocus()
                    }
                }
        ) {
            when (mainScreenState) {
                MainScreenRouteState.Main -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Анимированная строка поиска
                        AnimatedVisibility(
                            visible = isSearchBarVisible,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(durationMillis = 300)
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 300)
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = tween(durationMillis = 300)
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 300)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Кнопка меню (слева от поиска) с анимацией
                                AnimatedVisibility(
                                    visible = !isSearchFocused,
                                    enter = slideInHorizontally(
                                        initialOffsetX = { -it }, // появление слева
                                        animationSpec = tween(durationMillis = 300)
                                    ) + fadeIn(
                                        animationSpec = tween(durationMillis = 300)
                                    ),
                                    exit = slideOutHorizontally(
                                        targetOffsetX = { -it }, // скрытие влево
                                        animationSpec = tween(durationMillis = 300)
                                    ) + fadeOut(
                                        animationSpec = tween(durationMillis = 300)
                                    )
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .size(28.dp)
                                            .clickable(
                                                enabled = true,
                                                role = Role.Button,
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = {
                                                    showMenuBottomSheet = true
                                                }
                                            ),
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = stringResource(R.string.settings),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }

                                // Search Bar - занимает всё доступное пространство
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { query ->
                                        searchQuery = query
                                        if (searchQuery.isEmpty()) {
                                            viewModel.searchNotes(searchQuery)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .onFocusChanged {
                                            isSearchFocused = it.isFocused
                                        },
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
                                            // После поиска снимаем фокус
                                            focusManager.clearFocus()
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

                                // Группа иконок справа с анимацией
                                AnimatedVisibility(
                                    visible = !isSearchFocused,
                                    enter = slideInHorizontally(
                                        initialOffsetX = { it }, // появление справа
                                        animationSpec = tween(durationMillis = 300)
                                    ) + fadeIn(
                                        animationSpec = tween(durationMillis = 300)
                                    ),
                                    exit = slideOutHorizontally(
                                        targetOffsetX = { it }, // скрытие вправо
                                        animationSpec = tween(durationMillis = 300)
                                    ) + fadeOut(
                                        animationSpec = tween(durationMillis = 300)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .wrapContentSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Кнопка фильтрации
                                        Icon(
                                            modifier = Modifier
                                                .wrapContentSize()
                                                .size(20.dp)
                                                .clickable(
                                                    enabled = true,
                                                    role = Role.Button,
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    onClick = {
                                                        showFilterDialog = true
                                                    }),
                                            painter = painterResource(R.drawable.filter),
                                            contentDescription = "Filter",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )

                                        // Кнопка переключения типа списка
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable(
                                                    enabled = true,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    role = Role.Button,
                                                    indication = null,
                                                    onClick = {
                                                        listType = if (listType == 0) 1 else 0
                                                        saveListTypeToPreferences(listType, context)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (listType == 1) {
                                                FlowRow(
                                                    modifier = Modifier.wrapContentSize(),
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
                                                        modifier = Modifier
                                                            .width(8.dp)
                                                            .height(8.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.7f
                                                                )
                                                            )
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .width(8.dp)
                                                            .height(8.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.7f
                                                                )
                                                            )
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .width(8.dp)
                                                            .height(8.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.7f
                                                                )
                                                            )
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .width(8.dp)
                                                            .height(8.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.7f
                                                                )
                                                            )
                                                    )
                                                }
                                            } else {
                                                FlowRow(
                                                    modifier = Modifier.wrapContentSize(),
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
                                                        modifier = Modifier
                                                            .width(20.dp)
                                                            .height(6.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.7f
                                                                )
                                                            )
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .width(20.dp)
                                                            .height(6.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.7f
                                                                )
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        IOSGroupList(
                            groups = groups, // Теперь тут GroupsWithCounts
                            selectedGroupId = selectedGroupId,
                            totalCount = totalNotesCount,
                            onGroupSelected = { id -> viewModel.selectGroup(id) },
                            onCreateGroup = {
                                showCreateGroupDialog = true // Открываем диалог
                            },
                            onDeleteGroup = { group ->
                                viewModel.deleteFolder(group)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Fixed Date Label and Notes List
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f) // Добавляем weight чтобы занимать оставшееся пространство
                        ) {
                            // Отображаем пустое состояние, если заметок нет и нет загрузки
                            if (displayedNotes.isEmpty() && !isLoading) {
                                EmptyNotesState()
                            } else {
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
                                        items(displayedNotes, key = { it.id }) { note ->
                                            // Логика удаления (оставляем твою анимацию)
                                            if (notesToDelete[note.id] != true) {

                                                // Переменная для управления меню конкретной заметки
                                                var showNoteMenu by remember { mutableStateOf(false) }

                                                Box {
                                                    IOSNoteCard(
                                                        note = note,
                                                        groups = groups.map { it.group },
                                                        modifier = Modifier.fillMaxWidth(), // Для списка и грида работает
                                                        onClick = {
                                                            navController.navigate(Screen.EditNote.createRoute(note.id))
                                                        },
                                                        onLongClick = {
                                                            showNoteMenu = true
                                                            playSound(context, exoPlayer, R.raw.note_create)
                                                        },
                                                        onImageClick = { path ->
                                                            fullscreenImagePath = path // Восстановили полный экран
                                                        },
                                                        onNoteUpdated = { updatedNote ->
                                                            // Обновляем заметку через ViewModel (сохраняем галочку)
                                                            viewModel.updateNote(updatedNote, context)
                                                            // Если хочешь звук при нажатии галочки:
                                                            // playSound(context, exoPlayer, R.raw.note_create)
                                                        }
                                                    )

                                                    // --- КОНТЕКСТНОЕ МЕНЮ (При долгом нажатии) ---
                                                    DropdownMenu(
                                                        expanded = showNoteMenu,
                                                        onDismissRequest = { showNoteMenu = false },
                                                        offset = DpOffset(x = 10.dp, y = 0.dp),
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        // Переместить в группу (сверни свой диалог выбора группы сюда или вызывай callback)
                                                        DropdownMenuItem(
                                                            text = { Text("В папку") },
                                                            onClick = {
                                                                showNoteMenu = false
                                                                noteToMove = note
                                                                // ТУТ ЛОГИКА ДЛЯ ОТКРЫТИЯ ДИАЛОГА ВЫБОРА ГРУППЫ
                                                                // Тебе нужно будет поднять state showMoveToFolderDialog на уровень выше
                                                                // или сделать callback onShowMoveDialog(note)
                                                            },
                                                            leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
                                                        )

                                                        // Удалить
                                                        DropdownMenuItem(
                                                            text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                                                            onClick = {
                                                                showNoteMenu = false
                                                                notesToDelete[note.id] = true // Запуск твоей анимации удаления
                                                                playSound(context, exoPlayer, R.raw.note_delete)
                                                                coroutineScope.launch {
                                                                    delay(400)
                                                                    viewModel.deleteNote(note, context)
                                                                }
                                                            },
                                                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        item {
                                            Spacer(modifier = Modifier.height(94.dp))
                                        }
                                    }
                                } else {
                                    // Одна колонка
                                    LazyColumn(
                                        state = lazyListState,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 10.dp)
                                    ) {
                                        items(displayedNotes, key = { it.id }) { note ->
                                            // Логика удаления (оставляем твою анимацию)
                                            if (notesToDelete[note.id] != true) {

                                                // Переменная для управления меню конкретной заметки
                                                var showNoteMenu by remember { mutableStateOf(false) }

                                                Box {
                                                    IOSNoteCard(
                                                        note = note,
                                                        groups = groups.map { it.group },
                                                        modifier = Modifier.fillMaxWidth(), // Для списка и грида работает
                                                        onClick = {
                                                            navController.navigate(Screen.EditNote.createRoute(note.id))
                                                        },
                                                        onLongClick = {
                                                            showNoteMenu = true
                                                            playSound(context, exoPlayer, R.raw.note_create)
                                                        },
                                                        onImageClick = { path ->
                                                            fullscreenImagePath = path // Восстановили полный экран
                                                        },
                                                        onNoteUpdated = { updatedNote ->
                                                            // Обновляем заметку через ViewModel (сохраняем галочку)
                                                            viewModel.updateNote(updatedNote, context)
                                                            // Если хочешь звук при нажатии галочки:
                                                            // playSound(context, exoPlayer, R.raw.note_create)
                                                        }
                                                    )

                                                    // --- КОНТЕКСТНОЕ МЕНЮ (При долгом нажатии) ---
                                                    DropdownMenu(
                                                        expanded = showNoteMenu,
                                                        onDismissRequest = { showNoteMenu = false },
                                                        offset = DpOffset(x = 10.dp, y = 0.dp),
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        // Переместить в группу (сверни свой диалог выбора группы сюда или вызывай callback)
                                                        DropdownMenuItem(
                                                            text = { Text("В папку") },
                                                            onClick = {
                                                                showNoteMenu = false
                                                                noteToMove = note
                                                                // ТУТ ЛОГИКА ДЛЯ ОТКРЫТИЯ ДИАЛОГА ВЫБОРА ГРУППЫ
                                                                // Тебе нужно будет поднять state showMoveToFolderDialog на уровень выше
                                                                // или сделать callback onShowMoveDialog(note)
                                                            },
                                                            leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
                                                        )

                                                        // Удалить
                                                        DropdownMenuItem(
                                                            text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                                                            onClick = {
                                                                showNoteMenu = false
                                                                notesToDelete[note.id] = true // Запуск твоей анимации удаления
                                                                playSound(context, exoPlayer, R.raw.note_delete)
                                                                coroutineScope.launch {
                                                                    delay(400)
                                                                    viewModel.deleteNote(note, context)
                                                                }
                                                            },
                                                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                MainScreenRouteState.Calendar -> {
                    CalendarContent(
                        notes = notes,
                        viewModel = viewModel, // передаем viewModel
                        navController = navController, // передаем navController
                        groups = groups.map { it.group }, // передаем группы
                        notesToDelete = notesToDelete,
                        onDelete = { note ->
                            notesToDelete[note.id] = true
                            playSound(context, exoPlayer, R.raw.note_delete)
                            coroutineScope.launch {
                                delay(400)
                                viewModel.deleteNote(note, context)
                            }
                        },
                        onImageClick = { path -> fullscreenImagePath = path }
                    )
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

            // Filter Dialog
            if (showFilterDialog) {
                FilterDialog(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { filter ->
                        selectedFilter = filter
                        showFilterDialog = false
                    },
                    onDismiss = { showFilterDialog = false }
                )
            }
        }
    }

    if (noteToMove != null) {
        // Твой код диалога выбора группы (Alert или BottomSheet)
        MoveToGroupDialog(
            groups = groups.map { it.group },
            onDismiss = { noteToMove = null },
            onGroupSelected = { groupId ->
                viewModel.moveNoteToGroup(noteToMove!!, groupId)
                noteToMove = null
            }
        )
    }
    if (showCreateGroupDialog) {
        IOSCreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onCreate = { title, color ->
                showCreateGroupDialog = false
                viewModel.addFolder(Group(title = title, color = color.toLong()))
            }
        )
    }
}

// Компонент для отображения пустого состояния
@Composable
fun EmptyNotesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.empty_notes_list),
            contentDescription = stringResource(R.string.empty_notes_description),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_notes_list),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class) // 1. Исправляем animateItem
@Composable
private fun CalendarContent(
    notes: List<Note>,
    viewModel: NoteViewModel,
    navController: NavController,
    groups: List<Group>,
    onDelete: (Note) -> Unit,
    onImageClick: (String) -> Unit,
    notesToDelete: SnapshotStateMap<String, Boolean>
) {
    var selectedDateNotes by remember { mutableStateOf<List<Note>?>(null) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedWeek by remember { mutableStateOf<Int?>(null) }

    // Стейт для диалога перемещения в группу (нужен для календаря отдельно, если экран не общий)
    var noteToMove by remember { mutableStateOf<Note?>(null) }

    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val exoPlayer = rememberExoPlayer()

    // --- Сам Диалог перемещения ---
    if (noteToMove != null) {
        MoveToGroupDialog(
            groups = groups,
            onDismiss = { noteToMove = null },
            onGroupSelected = { groupId ->
                viewModel.moveNoteToGroup(noteToMove!!, groupId)
                noteToMove = null
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CalendarView(
            notes = notes,
            selectedDate = selectedDate,
            selectedWeek = selectedWeek,
            onDayClick = { date, notesForDay, week ->
                selectedDate = date
                selectedDateNotes = notesForDay
                selectedWeek = week
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surface)
        )

        // Отображаем заметки выбранной даты
        selectedDateNotes?.let { notesForDate ->
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                R.string.calendar_notes_for_date,
                                selectedDate?.let { formatCalendarDate(it) } ?: ""
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                selectedDateNotes = null
                                selectedDate = null
                                selectedWeek = null
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                }

                if (notesForDate.isNotEmpty()) {
                    items(notesForDate, key = { it.id }) { note ->
                        // Проверяем, не удаляется ли заметка визуально
                        if (notesToDelete[note.id] != true) {
                            var showNoteMenu by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .animateItem() // 2. Анимация теперь работает
                                    .padding(vertical = 8.dp)
                            ) {
                                // 3. Используем IOSNoteCard вместо NoteCard
                                IOSNoteCard(
                                    note = note,
                                    groups = groups,
                                    modifier = Modifier.fillMaxWidth(), // Для списка и грида работает
                                    onClick = {
                                        navController.navigate(Screen.EditNote.createRoute(note.id))
                                    },
                                    onLongClick = {
                                        showNoteMenu = true
                                        playSound(context, exoPlayer, R.raw.note_create)
                                    },
                                    onImageClick = onImageClick,
                                    onNoteUpdated = { updatedNote ->
                                        // Обновляем заметку через ViewModel (сохраняем галочку)
                                        viewModel.updateNote(updatedNote, context)
                                        // Если хочешь звук при нажатии галочки:
                                        // playSound(context, exoPlayer, R.raw.note_create)
                                    }
                                )

                                // Контекстное меню (как в списке заметок)
                                DropdownMenu(
                                    expanded = showNoteMenu,
                                    onDismissRequest = { showNoteMenu = false },
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("В папку") },
                                        onClick = {
                                            showNoteMenu = false
                                            noteToMove = note // Открываем диалог
                                        },
                                        leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showNoteMenu = false
                                            onDelete(note)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.calendar_no_notes_for_date, ""),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.calendar_select_date),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Функция для форматирования даты
private fun formatCalendarDate(date: Date): String {
    val calendar = Calendar.getInstance().apply { time = date }
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)

    val monthName = when (month) {
        Calendar.JANUARY -> "января"
        Calendar.FEBRUARY -> "февраля"
        Calendar.MARCH -> "марта"
        Calendar.APRIL -> "апреля"
        Calendar.MAY -> "мая"
        Calendar.JUNE -> "июня"
        Calendar.JULY -> "июля"
        Calendar.AUGUST -> "августа"
        Calendar.SEPTEMBER -> "сентября"
        Calendar.OCTOBER -> "октября"
        Calendar.NOVEMBER -> "ноября"
        Calendar.DECEMBER -> "декабря"
        else -> ""
    }

    return "$day $monthName $year"
}

// Компонент для опций меню
@Composable
fun MenuOption(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = true,
                onClick = {
                    onClick()
                },
                role = Role.Button
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        )
    }
}

// Диалоговое окно фильтрации
@Composable
fun FilterDialog(
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .padding(vertical = 20.dp)
            ) {
                // Заголовок
                Text(
                    text = stringResource(R.string.filter_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Опции фильтрации
                FilterOption(
                    text = stringResource(R.string.filter_option_new),
                    isSelected = selectedFilter == 0,
                    onClick = { onFilterSelected(0) }
                )

                FilterOption(
                    text = stringResource(R.string.filter_option_old),
                    isSelected = selectedFilter == 1,
                    onClick = { onFilterSelected(1) }
                )

                FilterOption(
                    text = stringResource(R.string.filter_option_with_reminders),
                    isSelected = selectedFilter == 2,
                    onClick = { onFilterSelected(2) }
                )

                FilterOption(
                    text = stringResource(R.string.filter_option_with_photos),
                    isSelected = selectedFilter == 3,
                    onClick = { onFilterSelected(3) }
                )

                FilterOption(
                    text = stringResource(R.string.filter_option_with_tasks),
                    isSelected = selectedFilter == 4,
                    onClick = { onFilterSelected(4) }
                )
            }
        }
    }
}

// Компонент опции фильтрации
@Composable
fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

// Остальные функции остаются без изменений
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

