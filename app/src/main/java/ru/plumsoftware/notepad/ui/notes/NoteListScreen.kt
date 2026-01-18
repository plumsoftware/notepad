package ru.plumsoftware.notepad.ui.notes

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import ru.plumsoftware.notepad.ui.elements.IOSActionSheetOption
import ru.plumsoftware.notepad.ui.elements.IOSCalendarView
import ru.plumsoftware.notepad.ui.elements.IOSCreateGroupDialog
import ru.plumsoftware.notepad.ui.elements.IOSNoteCard
import ru.plumsoftware.notepad.ui.elements.IOSPinInputScreen
import ru.plumsoftware.notepad.ui.elements.IOSTopBar
import ru.plumsoftware.notepad.ui.elements.RateAppBottomSheet
import ru.plumsoftware.notepad.ui.elements.getNotesForDate
import ru.plumsoftware.notepad.ui.rememberBiometricPrompt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    // Получаем количество секреток из ViewModel
    val secretCount by viewModel.secretNotesCount.collectAsState()
    val isSecureUnlocked = viewModel.isSecureFolderUnlocked

    // Состояния для показа экрана ПИН
    var showPinCreateScreen by remember { mutableStateOf(false) }
    var showPinConfirmScreen by remember { mutableStateOf(false) }
    var tempCreatedPin by remember { mutableStateOf("") } // Временный пин для подтверждения
    var showPinUnlockScreen by remember { mutableStateOf(false) }
    var isPinError by remember { mutableStateOf(false) }

    // Биометрия
    val biometricPrompt = rememberBiometricPrompt(
        onSuccess = { viewModel.unlockSecureFolder() },
        onFail = { /* Можно показать тост */ }
    )

    // Функция обработки клика
    val handleSecureClick = {
        if (selectedGroupId == NoteViewModel.SECURE_FOLDER_ID) {
            // Уже в ней
        } else if (isSecureUnlocked) {
            // Уже открыто -> переходим
            viewModel.selectGroup(NoteViewModel.SECURE_FOLDER_ID)
        } else {
            // ЗАБЛОКИРОВАНО -> ЗАПУСКАЕМ БИОМЕТРИЮ / ПИН
            if (viewModel.isPinSet()) {
                // Пин есть -> сначала Биометрия
                // biometricPrompt.authenticate() // Если реализовал класс-обертку
                // или сразу флаг на показ диалога
                showPinUnlockScreen = true
            } else {
                // Пина нет -> создание
                showPinCreateScreen = true
            }
        }
    }

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
    // IOS Action Sheet (Меню)
    if (showMenuBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.Transparent, // Прозрачный фон, чтобы видеть отступы
            contentColor = MaterialTheme.colorScheme.primary,
            dragHandle = null, // Убираем полоску сверху, в iOS её нет в таких меню
            scrimColor = Color.Black.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Отступы с краев
                    .padding(bottom = 16.dp)     // Отступ снизу
                    .navigationBarsPadding()     // Учитываем полоску навигации телефона
            ) {
                // --- 1. ОСНОВНОЙ БЛОК ОПЦИЙ ---
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                ) {
                    // Заголовок (опционально, в iOS часто его нет, но можно оставить пустым или добавить описание)
                    // Если заголовка нет, просто опции

                    // Опция "О приложении"
                    IOSActionSheetOption(
                        text = stringResource(R.string.about_app),
                        onClick = {
                            showMenuBottomSheet = false
                            navController.navigate(Screen.AboutApp.route)
                        },
                        showDivider = true
                    )

                    // Опция "Настройки"
                    IOSActionSheetOption(
                        text = stringResource(R.string.settings),
                        onClick = {
                            showMenuBottomSheet = false
                            navController.navigate(Screen.Settings.route)
                        },
                        showDivider = false
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- 2. КНОПКА ОТМЕНА ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface) // Белый/Черный фон для Отмены
                        .clickable { showMenuBottomSheet = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cancel), // "Отмена"
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
                .padding(top = 12.dp)
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
                            visible = isSearchBarVisible || isSearchFocused, // Если поиск активен, бар не скрываем
                            enter = slideInVertically { -it } + fadeIn(),
                            exit = slideOutVertically { -it } + fadeOut()
                        ) {
                            IOSTopBar(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { query ->
                                    searchQuery = query
                                    if (searchQuery.isEmpty()) {
                                        viewModel.searchNotes("") // Сбрасываем поиск
                                    } else {
                                        // Можно добавить debounce, если нужно
                                        viewModel.searchNotes(query)
                                    }
                                },
                                isSearchFocused = isSearchFocused,
                                onFocusChange = { focused -> isSearchFocused = focused },
                                onSettingsClick = { showMenuBottomSheet = true },
                                onFilterClick = { showFilterDialog = true },
                                onLayoutToggle = {
                                    listType = if (listType == 0) 1 else 0
                                    saveListTypeToPreferences(listType, context)
                                },
                                listType = listType
                            )
                        }

                        //Spacer(modifier = Modifier.height(12.dp))
                        IOSGroupList(
                            groups = groups,
                            selectedGroupId = selectedGroupId,
                            totalCount = totalNotesCount,
                            secretCount = secretCount,
                            onGroupSelected = { id -> viewModel.selectGroup(id) },
                            onSecureClick = { handleSecureClick() },
                            onCreateGroup = { showCreateGroupDialog = true },
                            onDeleteGroup = { group -> viewModel.deleteFolder(group) }
                        )
                        //Spacer(modifier = Modifier.height(8.dp))

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
    if (showPinCreateScreen) {
        IOSPinInputScreen(
            title = "Придумайте код-пароль",
            onPinEntered = { pin ->
                tempCreatedPin = pin
                showPinCreateScreen = false
                showPinConfirmScreen = true
            },
            onCancel = { showPinCreateScreen = false }
        )
    }
    if (showPinConfirmScreen) {
        IOSPinInputScreen(
            title = "Повторите код-пароль",
            onPinEntered = { pin ->
                if (pin == tempCreatedPin) {
                    viewModel.savePin(pin)
                    viewModel.unlockSecureFolder() // Успех
                    showPinConfirmScreen = false
                } else {
                    isPinError = true // Тряска
                }
            },
            onCancel = {
                showPinConfirmScreen = false
                showPinCreateScreen = true // Вернуться к созданию
            },
            isError = isPinError
        )
        // Сброс ошибки при изменении ввода происходит внутри компонента или через delay
        LaunchedEffect(isPinError) {
            if(isPinError) { delay(500); isPinError = false }
        }
    }
    if (showPinUnlockScreen && !isSecureUnlocked) { // Показывать только если не разблокировано
        IOSPinInputScreen(
            title = "Введите код-пароль",
            onPinEntered = { pin ->
                if (viewModel.checkPin(pin)) {
                    viewModel.unlockSecureFolder()
                    showPinUnlockScreen = false
                } else {
                    isPinError = true
                }
            },
            onCancel = { showPinUnlockScreen = false },
            isError = isPinError
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

@OptIn(ExperimentalFoundationApi::class) // Нужен для animateItem / animateItemPlacement
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
    // Выбранная дата (по умолчанию - сейчас)
    var selectedDate by remember { mutableStateOf<Date?>(Date()) }

    // Режим календаря: true = месяц, false = неделя
    var isMonthView by remember { mutableStateOf(true) }

    // Получаем список заметок на выбранную дату
    val selectedDateNotes = remember(selectedDate, notes) {
        selectedDate?.let { date ->
            getNotesForDate(notes, date)
        } ?: emptyList()
    }

    val lazyListState = rememberLazyListState()
    var noteToMove by remember { mutableStateOf<Note?>(null) }
    val context = LocalContext.current

    // Диалог "В папку" (появляется при выборе в меню)
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- КАЛЕНДАРЬ ---
        IOSCalendarView(
            notes = notes,
            selectedDate = selectedDate ?: Date(),
            isMonthExpanded = isMonthView,
            onDateSelected = { date ->
                selectedDate = date
                isMonthView = false // При клике на день сворачиваем до недели
            },
            onExpandChange = { isExpanded ->
                isMonthView = isExpanded
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                // Тень в стиле iOS
                .shadow(4.dp, spotColor = Color.Black.copy(alpha = 0.05f))
                .padding(bottom = 8.dp)
        )

        // --- СПИСОК ЗАМЕТОК ---
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Заголовок даты над списком
            item {
                Text(
                    text = selectedDate?.let { getFancyDateTitle(it) } ?: "",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (selectedDateNotes.isNotEmpty()) {
                // Если заметки есть
                items(selectedDateNotes, key = { it.id }) { note ->
                    if (notesToDelete[note.id] != true) {
                        var showNoteMenu by remember { mutableStateOf(false) }

                        // Анимация элемента списка
                        Box(
                            modifier = Modifier
                                .animateItem() // Если ошибка версии, удали эту строку или замени на .animateItemPlacement()
                                .padding(vertical = 6.dp)
                        ) {
                            IOSNoteCard(
                                note = note,
                                groups = groups,
                                onClick = { navController.navigate(Screen.EditNote.createRoute(note.id)) },
                                onLongClick = { showNoteMenu = true },
                                onImageClick = onImageClick,
                                onNoteUpdated = { updatedNote ->
                                    viewModel.updateNote(updatedNote, context)
                                }
                            )

                            // Контекстное меню
                            DropdownMenu(
                                expanded = showNoteMenu,
                                onDismissRequest = { showNoteMenu = false },
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.move_to_folder)) },
                                    onClick = {
                                        showNoteMenu = false; noteToMove = note
                                    },
                                    leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showNoteMenu = false; onDelete(note)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                }

                // Пустое место снизу для прокрутки
                item { Spacer(modifier = Modifier.height(100.dp)) }

            } else {
                // ПУСТОЕ СОСТОЯНИЕ (Если нет заметок)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // "Здесь пока пусто"
                            Text(
                                text = stringResource(R.string.empty_notes_list),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // "Нет заметок на [Дата]"
                            val dateStr = selectedDate?.let { getFancyDateTitle(it) } ?: ""
                            Text(
                                text = stringResource(R.string.calendar_no_notes_for_date, dateStr),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// Форматирует дату для заголовка списка (Например: "Понедельник, 12 Октября")
fun getFancyDateTitle(date: Date): String {
    val fmt = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    return fmt.format(date).replaceFirstChar { it.uppercase() }
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }, // Закрытие по клику на фон
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 48.dp) // Большие отступы с боков для iOS стиля
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. ЗАГОЛОВОК
                Text(
                    text = stringResource(R.string.filter_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

                // 2. ОПЦИИ (Убрали RadioButton, добавили галочки справа)
                IOSFilterOption(
                    text = stringResource(R.string.filter_option_new),
                    isSelected = selectedFilter == 0,
                    onClick = { onFilterSelected(0) },
                    showDivider = true
                )

                IOSFilterOption(
                    text = stringResource(R.string.filter_option_old),
                    isSelected = selectedFilter == 1,
                    onClick = { onFilterSelected(1) },
                    showDivider = true
                )

                IOSFilterOption(
                    text = stringResource(R.string.filter_option_with_reminders),
                    isSelected = selectedFilter == 2,
                    onClick = { onFilterSelected(2) },
                    showDivider = true
                )

                IOSFilterOption(
                    text = stringResource(R.string.filter_option_with_photos),
                    isSelected = selectedFilter == 3,
                    onClick = { onFilterSelected(3) },
                    showDivider = true
                )

                IOSFilterOption(
                    text = stringResource(R.string.filter_option_with_tasks),
                    isSelected = selectedFilter == 4,
                    onClick = { onFilterSelected(4) },
                    showDivider = false // У последнего элемента разделитель не нужен
                )

                // Разделитель перед отменой (жирный)
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

                // 3. КНОПКА ОТМЕНА (Текстовая, на всю ширину)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cancel), // Убедись, что ресурс есть, или напиши "Отмена"
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Новый компонент опции фильтрации в стиле iOS
@Composable
fun IOSFilterOption(
    text: String,
    isSelected: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Текст слева
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge, // 17sp стандарт iOS
                color = MaterialTheme.colorScheme.onSurface
            )

            // Галочка справа (только если выбрано)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, // Синий цвет галочки
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Разделитель (Inset separator - отступ слева 16dp)
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 0.5.dp
            )
        }
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

