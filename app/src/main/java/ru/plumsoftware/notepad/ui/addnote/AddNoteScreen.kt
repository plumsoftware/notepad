package ru.plumsoftware.notepad.ui.addnote

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color.luminance
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import java.util.UUID
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.App
import java.util.Calendar

@SuppressLint("MutableCollectionMutableState", "UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    activity: Activity,
    navController: NavController,
    viewModel: NoteViewModel,
    note: Note? = null
) {
    // --- Ads Setup (Без изменений) ---
    var rewardedAd: RewardedAd? = null
    var rewardedAdLoader: RewardedAdLoader? = null
    rewardedAdLoader = RewardedAdLoader(LocalContext.current).apply {
        setAdLoadListener(object : RewardedAdLoadListener {
            override fun onAdLoaded(rewarded: RewardedAd) {
                rewardedAd = rewarded
            }

            override fun onAdFailedToLoad(error: AdRequestError) {}
        })
    }
    loadRewardedAd(rewardedAdLoader)

    // --- Ads Interstitial (Без изменений) ---
    var myInterstitialAds: InterstitialAd? = null
    val interstitialAdsLoader = InterstitialAdLoader(activity).apply {
        setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                myInterstitialAds = interstitialAd
            }

            override fun onAdFailedToLoad(error: AdRequestError) {}
        })
    }
    val adRequestConfiguration =
        AdRequestConfiguration.Builder(App.platformConfig.adsConfig.interstitialAdsId).build()
    interstitialAdsLoader.loadAd(adRequestConfiguration)

    // --- State ---
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val isEditing = note != null

    // Data States
    var title by remember { mutableStateOf(note?.title ?: "") }
    var description by remember { mutableStateOf(note?.description ?: "") }
    var tasks by remember {
        mutableStateOf<MutableList<Task>>(
            note?.tasks?.toMutableList() ?: mutableListOf()
        )
    }
    var photos by remember { mutableStateOf<List<String>>(note?.photos ?: emptyList()) }

    // UI Logic States
    var newTaskText by remember { mutableStateOf("") }
    var isReminder by remember { mutableStateOf(note?.reminderDate != null) }
    var reminderDate by remember { mutableStateOf(note?.reminderDate) }

    // Dialogs & Pickers Visibility
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) } // State для шторки цветов

    var tempSelectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val exoPlayer = rememberExoPlayer()

    var isAdsLoading by remember { mutableStateOf(false) }

    // --- Colors & Contrast Logic ---
    val colors = listOf(
        Color(0xFF81C784).value, Color(0xFF4DB6AC).value, Color(0xFFF48FB1).value,
        Color(0xFFFF8A65).value, Color(0xFF64B5F6).value, Color(0xFF7986CB).value,
        Color(0xFFAB47BC).value, Color(0xFF9575CD).value, Color(0xFFFFCA28).value,
        Color(0xFFF9A825).value, Color(0xFF90A4AE).value, Color(0xFFD7CCC8).value,
        Color(0xFF283593).value, Color(0xFF37474F).value, Color(0xFF1A237E).value
    )
    var selectedColor by remember { mutableStateOf(note?.color?.toULong() ?: colors.first()) }

    // Анимация фона
    val animatedBackgroundColor by animateColorAsState(
        targetValue = Color(selectedColor),
        animationSpec = tween(durationMillis = 500), // Чуть быстрее для отзывчивости
        label = "backgroundColorAnimation"
    )

    // Определяем цвет контента (черный или белый) в зависимости от яркости фона
    val contentColor = if (luminance(selectedColor.toInt()) > 0.5f) Color.Black else Color.White
    val secondaryContentColor = contentColor.copy(alpha = 0.7f)

    // --- Focus Managers ---
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val taskTextFocusRequester = remember { FocusRequester() }

    // --- Helpers ---
    val pickImages =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                if (photos.size < 5) {
                    photos = photos.toMutableList().apply {
                        saveImageToInternalStorage(context, it)?.let { path -> add(path) }
                    }
                }
            }
        }

    // --- Функция сохранения ---
    val onSaveClick = {
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
                    deleteImagesFromStorage(context, note.photos.filterNot { photos.contains(it) })
                }
                playSound(context, exoPlayer, R.raw.note_create)
                viewModel.updateNote(updatedNote.copy(groupId = note.groupId), context)
            } else {
                playSound(context, exoPlayer, R.raw.note_create)
                viewModel.addNote(updatedNote)
            }

            // Logic for Interstitial Ads
            if (notes.size >= 5 && myInterstitialAds != null) {
                myInterstitialAds.show(activity)
            }
            navController.navigateUp()
        }
    }

    // --- UI Structure ---

    // Bottom Sheet для выбора цвета
    if (showColorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showColorSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(R.string.note_color),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(space = 12.dp, alignment = Alignment.Top),
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp, alignment = Alignment.Start),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.forEach { colorULong ->
                        val color = Color(colorULong)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == colorULong) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorULong }
                        ) {
                            if (selectedColor == colorULong) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = if (luminance(colorULong.toInt()) > 0.5f) Color.Black else Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Date/Time Dialogs (оставил логику как была, но сократил код визуально)
    if (showDatePicker) {
        // ... (код DatePickerDialog без изменений, используй тот же) ...
        // Для краткости я его свернул, но вставь сюда свой код диалога
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = reminderDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false; tempSelectedDateMillis =
                    datePickerState.selectedDateMillis; showTimePicker = true
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false; isReminder = false
                }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    tempSelectedDateMillis?.let { dateMillis ->
                        // 1. Создаем календарь и устанавливаем выбранную ДАТУ
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = dateMillis

                        // ВАЖНО: DatePicker часто возвращает время в UTC.
                        // Чтобы сохранить именно тот день, который выбрал пользователь,
                        // но с его локальным временем, лучше явно вытащить год/месяц/день:
                        val dateCalendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
                        val year = dateCalendar.get(Calendar.YEAR)
                        val month = dateCalendar.get(Calendar.MONTH)
                        val day = dateCalendar.get(Calendar.DAY_OF_MONTH)

                        // 2. Устанавливаем в итоговый календарь правильные компоненты
                        val finalCalendar = Calendar.getInstance() // Текущее время и зона
                        finalCalendar.set(Calendar.YEAR, year)
                        finalCalendar.set(Calendar.MONTH, month)
                        finalCalendar.set(Calendar.DAY_OF_MONTH, day)

                        // 3. Добавляем ВРЕМЯ из TimePicker
                        finalCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        finalCalendar.set(Calendar.MINUTE, timePickerState.minute)
                        finalCalendar.set(Calendar.SECOND, 0)
                        finalCalendar.set(Calendar.MILLISECOND, 0)

                        // 4. Сохраняем результат
                        reminderDate = finalCalendar.timeInMillis
                    }
                }) { Text("OK") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text(stringResource(R.string.new_task)) },
            text = {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(taskTextFocusRequester),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTaskText.isNotBlank()) {
                        tasks.add(Task(text = newTaskText)); newTaskText = ""; showAddTaskDialog =
                            false
                    }
                }) { Text(stringResource(R.string.add)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text(
                        stringResource(R.string.cancel)
                    )
                }
            }
        )
        LaunchedEffect(Unit) { taskTextFocusRequester.requestFocus() }
    }

    // Ads Dialog for Photo
    if (showAddPhotoDialog) {
        // ... (Твой код AlertDialog для рекламы без изменений) ...
        AlertDialog(
            onDismissRequest = { showAddPhotoDialog = false },
            title = { Text(stringResource(R.string.photo_add_ads_promo_title)) },
            confirmButton = {
                TextButton(onClick = {
                    showAddPhotoDialog = false; isAdsLoading = true
                    showAd(rewardedAd, rewardedAdLoader, activity) {
                        scope.launch { isAdsLoading = false; pickImages.launch("image/*") }
                    }
                }) { Text(stringResource(R.string.watch_ad)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddPhotoDialog = false }) {
                    Text(
                        stringResource(R.string.cancel)
                    )
                }
            }
        )
    }

    // Fullscreen Image
    fullscreenImagePath?.let {
        FullscreenImageDialog(
            imagePath = it,
            onDismiss = { fullscreenImagePath = null })
    }


    Scaffold(
        containerColor = animatedBackgroundColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = contentColor
                        )
                    }
                },
                actions = {
                    // Кнопка "Сохранить" теперь галочка
                    IconButton(
                        onClick = onSaveClick,
                        enabled = title.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.save),
                            tint = if (title.isNotBlank()) contentColor else contentColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        // Нижняя панель инструментов для быстрого доступа
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black.copy(alpha = 0.1f),
                contentColor = contentColor,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
            ) {
                // 1. Кнопка "Палитра"
                IconButton(onClick = { showColorSheet = true }, modifier = Modifier.weight(1f)) {
                    Icon(
                        painter = painterResource(R.drawable.palette_icon),
                        contentDescription = "Color",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    ) // Замени на свою иконку палитры
                }

                // 2. Кнопка "Фото"
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (photos.size < 5) {
                            if (photos.size == 4) showAddPhotoDialog =
                                true else pickImages.launch("image/*")
                        }
                    }
                ) {
                    if (isAdsLoading) CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = contentColor
                    )
                    else Icon(
                        Icons.Default.Image,
                        contentDescription = "Add Photo",
                        tint = contentColor
                    )
                }

                // 3. Кнопка "Задача"
                IconButton(onClick = { showAddTaskDialog = true }, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Rounded.CheckBox,
                        contentDescription = "Add Task",
                        tint = contentColor
                    )
                }

                // 4. Кнопка "Напоминание"
                IconButton(
                    onClick = { showDatePicker = true; isReminder = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (isReminder) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = "Reminder",
                        tint = if (isReminder) contentColor else secondaryContentColor
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // --- Title ---
            Box(modifier = Modifier.fillMaxWidth()) {
                if (title.isEmpty()) {
                    Text(
                        text = stringResource(R.string.title),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = secondaryContentColor
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester),
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    ),
                    cursorBrush = SolidColor(contentColor)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Description ---
            Box(modifier = Modifier.fillMaxWidth()) {
                if (description.isEmpty()) {
                    Text(
                        text = stringResource(R.string.desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = secondaryContentColor
                    )
                }
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(descriptionFocusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                    cursorBrush = SolidColor(contentColor)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Reminder Info (Если выбрано) ---
            if (isReminder && reminderDate != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(contentColor.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                        .padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(reminderDate!!),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = contentColor,
                        modifier = Modifier.clickable { isReminder = false; reminderDate = null }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Photos Grid ---
            if (photos.isNotEmpty()) {
                Text(
                    stringResource(R.string.photos),
                    style = MaterialTheme.typography.titleSmall,
                    color = secondaryContentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos) { photoPath ->
                        Box(modifier = Modifier.size(100.dp)) {
                            AsyncImage(
                                model = photoPath,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { fullscreenImagePath = photoPath }
                            )
                            // Кнопка удаления фото
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .clickable {
                                        photos = photos.toMutableList().apply { remove(photoPath) }
                                        deleteImagesFromStorage(context, listOf(photoPath))
                                    }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Tasks List ---
            if (tasks.isNotEmpty()) {
                Text(
                    stringResource(R.string.tasks),
                    style = MaterialTheme.typography.titleSmall,
                    color = secondaryContentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                tasks.forEachIndexed { index, task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = task.isChecked,
                            onCheckedChange = { isChecked ->
                                tasks = tasks.toMutableList()
                                    .apply { this[index] = task.copy(isChecked = isChecked) }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = contentColor,
                                uncheckedColor = secondaryContentColor,
                                checkmarkColor = animatedBackgroundColor
                            )
                        )
                        Text(
                            text = task.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = if (task.isChecked) TextDecoration.LineThrough else null
                            ),
                            color = if (task.isChecked) secondaryContentColor else contentColor,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            tasks = tasks.toMutableList().apply { removeAt(index) }
                        }) {
                            Icon(Icons.Default.Close, null, tint = secondaryContentColor)
                        }
                    }
                }
            }

            // Отступ снизу, чтобы контент не перекрывался BottomBar
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (isLoading) LoadingDialog()
    }
}

private fun loadRewardedAd(rewardedAdLoader: RewardedAdLoader?) {
    val adRequestConfiguration =
        AdRequestConfiguration.Builder(App.platformConfig.adsConfig.rewardedAdsId).build()
    rewardedAdLoader?.loadAd(adRequestConfiguration)
}

private fun showAd(
    rewardedAd: RewardedAd?,
    rewardedAdLoader: RewardedAdLoader?,
    activity: Activity?,
    onRewarded: () -> Unit
) {
    var isRewarded = false
    rewardedAd?.apply {
        setAdEventListener(object : RewardedAdEventListener {
            override fun onAdShown() {
                // Called when ad is shown.
            }

            override fun onRewarded(reward: Reward) {
                isRewarded = true
                // ВЫЗЫВАЕМ onRewarded СРАЗУ ПОСЛЕ НАГРАДЫ
//                 onRewarded()
            }

            override fun onAdFailedToShow(adError: AdError) {
                // Called when an RewardedAd failed to show
                rewardedAd.setAdEventListener(null)
                loadRewardedAd(rewardedAdLoader = rewardedAdLoader)
            }

            override fun onAdDismissed() {
                // Called when ad is dismissed.
                rewardedAd.setAdEventListener(null)
                loadRewardedAd(rewardedAdLoader = rewardedAdLoader)

                // ЕСЛИ НАГРАДА НЕ БЫЛА ВРУЧЕНА, НО РЕКЛАМА БЫЛА ПОЛНОСТЬЮ ПРОСМОТРЕНА
                if (isRewarded) {
                    // Можно также вызвать onRewarded() здесь, если хотите давать награду за любой просмотр
                    onRewarded()
                }
            }

            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                // Called when an impression is recorded for an ad.
            }
        })
        if (activity != null)
            show(activity = activity)
    } ?: run {
        // Если реклама не загружена, всё равно разрешаем добавить фото
        onRewarded()
    }
}
