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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("MutableCollectionMutableState", "UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    activity: Activity,
    navController: NavController,
    viewModel: NoteViewModel,
    note: Note? = null
) {
    // --- Ads Setup (Твой код, без изменений) ---
    var rewardedAd: RewardedAd? = null
    var rewardedAdLoader: RewardedAdLoader? = null
    rewardedAdLoader = RewardedAdLoader(LocalContext.current).apply {
        setAdLoadListener(object : RewardedAdLoadListener {
            override fun onAdLoaded(rewarded: RewardedAd) { rewardedAd = rewarded }
            override fun onAdFailedToLoad(error: AdRequestError) {}
        })
    }
    val adRequestConfiguration = AdRequestConfiguration.Builder(App.platformConfig.adsConfig.rewardedAdsId).build()
    rewardedAdLoader.loadAd(adRequestConfiguration)

    var myInterstitialAds: InterstitialAd? = null
    val interstitialAdsLoader = InterstitialAdLoader(activity).apply {
        setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(interstitialAd: InterstitialAd) { myInterstitialAds = interstitialAd }
            override fun onAdFailedToLoad(error: AdRequestError) {}
        })
    }
    val interstitialConfig = AdRequestConfiguration.Builder(App.platformConfig.adsConfig.interstitialAdsId).build()
    interstitialAdsLoader.loadAd(interstitialConfig)

    // --- State ---
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val isEditing = note != null

    // Data States
    var title by remember { mutableStateOf(note?.title ?: "") }
    var description by remember { mutableStateOf(note?.description ?: "") }
    var tasks by remember { mutableStateOf<MutableList<Task>>(note?.tasks?.toMutableList() ?: mutableListOf()) }
    var photos by remember { mutableStateOf<List<String>>(note?.photos ?: emptyList()) }

    // UI Logic
    var isReminder by remember { mutableStateOf(note?.reminderDate != null) }
    var reminderDate by remember { mutableStateOf(note?.reminderDate) }
    var tempSelectedDateMillis by remember { mutableStateOf<Long?>(null) }

    // Modals visibility
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val exoPlayer = rememberExoPlayer()
    var isAdsLoading by remember { mutableStateOf(false) }

    // Colors
    // Цвета в виде объектов Color для удобства
    // Пастельная палитра в стиле iOS Notes / Reminders
// Оптимизирована для черного текста (luminance > 0.5)
    val availableColors = listOf(
        Color(0xFFFFFFFF), // White (Default) - Чистый белый
        Color(0xFFEBEBF5), // System Gray 6 (Light) - Нейтральный серый

        Color(0xFFFFB3AC), // Soft Red (iOS Red Tint)
        Color(0xFFFFDCA8), // Soft Orange (iOS Orange Tint)
        Color(0xFFFFF0A6), // Soft Yellow (iOS Yellow Tint)
        Color(0xFFC7F0BD), // Soft Green (iOS Green Tint)
        Color(0xFFB5EAD7), // Mint (Popular Aesthetic)
        Color(0xFFBCE9FF), // Soft Teal / Cyan
        Color(0xFFA8D3FF), // Soft Blue (iOS Blue Tint)
        Color(0xFFC4D9FF), // Periwinkle (Indigo Tint)
        Color(0xFFE4C2FA), // Soft Purple (iOS Purple Tint)
        Color(0xFFFFC2D1), // Soft Pink (iOS Pink Tint)
        Color(0xFFE6BEB3), // Soft Brown

        // Более насыщенные, но безопасные варианты (если хочется цвета поярче)
        Color(0xFF81D4FA), // Sky Blue
        Color(0xFFC5E1A5), // Light Green
        Color(0xFFFFCC80)  // Orange Peel
    )

    var selectedColor by remember { mutableStateOf(note?.color?.let { Color(it.toULong()) } ?: availableColors.last()) }

    // --- Animation & Contrast ---
    val animatedBackgroundColor by animateColorAsState(
        targetValue = selectedColor,
        animationSpec = tween(durationMillis = 500),
        label = "bgColor"
    )

    // Контраст: Если фон темный -> текст белый, иначе -> черный
    // Яркость фона
    val isLightBg = luminance(selectedColor.toArgb()) > 0.5f

    // Проверка: Является ли фон "нейтральным" (белый/черный)
    // Если да - используем Primary (синий), иначе - контрастный (Ч/Б)
    val isNeutralBg = selectedColor == Color.White || selectedColor == Color.Black || selectedColor == Color.Transparent

    // Цвет КНОПОК и ИКОНОК (Navigation, Action Icons)
    val actionItemsColor = if (isNeutralBg) {
        MaterialTheme.colorScheme.primary // Синий на белом/черном
    } else {
        if (isLightBg) Color.Black else Color.White // Черный на светлом, Белый на темном (цветном)
    }

    // На белом - черный, на черном - белый, на цветном - по яркости
    val contentTextColor = if (isLightBg) Color.Black else Color.White

    val contentColor = if (isLightBg) Color.Black else Color.White
    val placeholderColor = contentColor.copy(alpha = 0.4f)
    val dividerColor = contentColor.copy(alpha = 0.1f)

    // Helpers
    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            if (photos.size < 5) {
                photos = photos.toMutableList().apply {
                    saveImageToInternalStorage(context, it)?.let { path -> add(path) }
                }
            }
        }
    }

    // --- Функция сохранения ---
    // Добавляем явное указание типа : () -> Unit или Unit в конце
    val onSaveClick: () -> Unit = {
        if (title.isNotBlank() || description.isNotBlank()) {
            val updatedNote = Note(
                id = note?.id ?: UUID.randomUUID().toString(),
                title = title,
                description = description,
                color = selectedColor.value.toLong(),
                tasks = tasks,
                createdAt = note?.createdAt ?: System.currentTimeMillis(),
                reminderDate = if (isReminder) reminderDate else null,
                photos = photos,
                groupId = note?.groupId ?: "0"
            )

            if (isEditing) {
                if (note.photos != photos) {
                    deleteImagesFromStorage(context, note.photos.filterNot { photos.contains(it) })
                }
                playSound(context, exoPlayer, R.raw.note_create)
                viewModel.updateNote(updatedNote, context)
            } else {
                playSound(context, exoPlayer, R.raw.note_create)
                viewModel.addNote(updatedNote)
            }

            if (notes.size >= 5 && myInterstitialAds != null) {
                myInterstitialAds.show(activity)
            }
            navController.navigateUp()
        } else {
            navController.navigateUp()
        }
        Unit
    }

    // --- DIALOGS (IOS STYLE) ---

    // 1. Цвет (Шторка снизу)
    if (showColorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showColorSheet = false },
            containerColor = if (isLightBg) Color(0xFFF2F2F7) else Color(0xFF1C1C1E), // iOS system bg
            dragHandle = { Box(modifier = Modifier.padding(top = 8.dp).width(36.dp).height(5.dp).clip(CircleShape).background(Color.Gray.copy(0.4f))) }
        ) {
            Column(modifier = Modifier.padding(bottom = 40.dp, start = 16.dp, end = 16.dp, top = 16.dp)) {
                Text(
                    text = stringResource(R.string.note_color),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isLightBg) Color.Black else Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(availableColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                    color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.3f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        }
    }

    // 2. Задача (iOS Alert)
    if (showAddTaskDialog) {
        IOSAddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { text ->
                tasks.add(Task(text = text))
                showAddTaskDialog = false
            }
        )
    }

    // 3. Фото за рекламу (iOS Alert)
    if (showAddPhotoDialog) {
        IOSAdsDialog(
            onDismiss = { showAddPhotoDialog = false },
            onWatch = {
                showAddPhotoDialog = false
                isAdsLoading = true
                showAd(rewardedAd, rewardedAdLoader, activity) {
                    scope.launch {
                        isAdsLoading = false
                        pickImages.launch("image/*")
                    }
                }
            }
        )
    }

    // System Date Pickers (как просил - системные)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = reminderDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    tempSelectedDateMillis = datePickerState.selectedDateMillis
                    showTimePicker = true
                }) { Text(stringResource(R.string.ok_)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false; isReminder = false }) { Text(stringResource(R.string.cancel)) }
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
                        val finalCalendar = Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        reminderDate = finalCalendar.timeInMillis
                    }
                }) { Text(stringResource(R.string.ok_)) }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    // Fullscreen
    fullscreenImagePath?.let {
        FullscreenImageDialog(imagePath = it, onDismiss = { fullscreenImagePath = null })
    }

    Scaffold(
        containerColor = animatedBackgroundColor,
        topBar = {
            // --- IOS TOP BAR (Прозрачный) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .statusBarsPadding(), // Учитываем статус бар
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка Назад (Шеврон + Текст "Назад")
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSaveClick() }
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = null, // Декоративный элемент, текст рядом есть
                        tint = actionItemsColor , // iOS Blue
                        modifier = Modifier.size(20.dp)
                    )
                    // Текст кнопки (обычно "Назад" или название предыдущего экрана)
                    // В твоем коде настроек использовался "back_button"
                    Text(
                        text = stringResource(R.string.back_button),
                        style = MaterialTheme.typography.bodyLarge,
                        color = actionItemsColor
                    )
                }
//                // Кнопка НАЗАД (Шеврон)
//                TextButton(
//                    onClick = onSaveClick,
//                    colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
//                ) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
//                        contentDescription = null,
//                        modifier = Modifier.size(20.dp)
//                    )
//                    Text(
//                        text = stringResource(R.string.notes),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }

                // Кнопка СОХРАНИТЬ (Готово)
                // Показываем более ярко, если есть изменения (здесь упрощенно всегда "Готово")
                TextButton(
                    onClick = onSaveClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        bottomBar = {
            // --- IOS TOOLBAR (Плоский, часть фона) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
                    .imePadding(), // Чтобы поднимался над клавиатурой
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Чеклист
                IconButton(onClick = { showAddTaskDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.CheckBox,
                        contentDescription = "Checklist",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. Фото
                IconButton(onClick = {
                    if (photos.size < 5) {
                        if (photos.size == 4) showAddPhotoDialog = true else pickImages.launch("image/*")
                    }
                }) {
                    if (isAdsLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = contentColor, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Photo",
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 3. Цвет
                IconButton(onClick = { showColorSheet = true }) {
                    Icon(
                        painter = painterResource(R.drawable.palette_icon),
                        contentDescription = "Color",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 4. Напоминание
                IconButton(onClick = { showDatePicker = true; isReminder = true }) {
                    Icon(
                        imageVector = if (isReminder) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = "Reminder",
                        tint = if (isReminder) contentColor else contentColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { padding ->
        // --- КОНТЕНТ ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 12.dp)
                .verticalScroll(scrollState)
        ) {
            // Дата создания (сверху, серая)
            Text(
                text = getFriendlyDate(note?.createdAt ?: System.currentTimeMillis()),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // ЗАГОЛОВОК (Large Title)
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (title.isEmpty()) {
                    Text(
                        text = stringResource(R.string.title),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = placeholderColor
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    ),
                    cursorBrush = SolidColor(contentColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ТЕКСТ (Body)
            Box(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                if (description.isEmpty() && tasks.isEmpty()) {
                    Text(
                        text = stringResource(R.string.desc),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 24.sp),
                        color = placeholderColor
                    )
                }
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = contentColor,
                        fontSize = 17.sp, // iOS size
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(contentColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // НАПОМИНАНИЕ (Чип внутри контента, если активно)
            if (isReminder && reminderDate != null) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(contentColor.copy(alpha = 0.08f))
                        .clickable { isReminder = false; reminderDate = null }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, null, tint = contentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(reminderDate!!),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Close, null, tint = contentColor.copy(0.6f), modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ЗАДАЧИ
            if (tasks.isNotEmpty()) {
                tasks.forEachIndexed { index, task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (task.isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isChecked) contentColor.copy(0.5f) else contentColor,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val isCh = !task.isChecked
                                    tasks = tasks.toMutableList().apply { this[index] = task.copy(isChecked = isCh) }
                                }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = task.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (task.isChecked) contentColor.copy(0.5f) else contentColor,
                                textDecoration = if (task.isChecked) TextDecoration.LineThrough else null
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Close, null,
                            tint = contentColor.copy(0.3f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { tasks = tasks.toMutableList().apply { removeAt(index) } }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ФОТОГРАФИИ
            if (photos.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(photos) { path ->
                        Box {
                            AsyncImage(
                                model = path,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { fullscreenImagePath = path }
                            )
                            // Кнопка удалить (круглый крестик)
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.6f))
                                    .clickable {
                                        photos = photos.toMutableList().apply { remove(path) }
                                        deleteImagesFromStorage(context, listOf(path))
                                    }
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp)) // Отступ снизу
            }
        }

        if (isLoading) LoadingDialog()
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ ДИАЛОГИ (ВСТАВИТЬ ВНИЗ ФАЙЛА) ---

@Composable
fun IOSAddTaskDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember{MutableInteractionSource()}, indication=null){onDismiss()}, contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.width(280.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha=0.95f)).clickable(enabled=false){}) {
                Text(stringResource(R.string.new_task), fontWeight=FontWeight.Bold, modifier=Modifier.padding(16.dp).align(Alignment.CenterHorizontally), style=MaterialTheme.typography.titleMedium)
                BasicTextField(
                    value = text, onValueChange = { text = it },
                    modifier = Modifier.padding(horizontal=16.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.Gray.copy(0.1f)).padding(12.dp).focusRequester(focusRequester)
                )
                Row(modifier = Modifier.padding(top=16.dp).height(44.dp)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable{onDismiss()}, contentAlignment=Alignment.Center) {
                        Text(stringResource(R.string.cancel), color=MaterialTheme.colorScheme.primary)
                    }
                    Box(modifier = Modifier.width(0.5.dp).fillMaxHeight().background(Color.Gray.copy(0.3f)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable(enabled=text.isNotBlank()){ if(text.isNotBlank()) onAdd(text)}, contentAlignment=Alignment.Center) {
                        Text(stringResource(R.string.add), fontWeight=FontWeight.Bold, color=if(text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun IOSAdsDialog(onDismiss: () -> Unit, onWatch: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember{MutableInteractionSource()}, indication=null){onDismiss()}, contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.width(280.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha=0.95f)).clickable(enabled=false){}, horizontalAlignment=Alignment.CenterHorizontally) {
                Text(stringResource(R.string.photo_add_ads_promo_title), fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=16.dp, bottom=4.dp), style=MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.photo_add_ads_promo_description), style=MaterialTheme.typography.bodySmall, textAlign= TextAlign.Center, modifier=Modifier.padding(horizontal=16.dp))
                HorizontalDivider(modifier=Modifier.padding(top=16.dp), color=Color.Gray.copy(0.3f))
                Row(modifier = Modifier.height(44.dp)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable{onDismiss()}, contentAlignment=Alignment.Center) {
                        Text(stringResource(R.string.cancel), color=MaterialTheme.colorScheme.primary)
                    }
                    Box(modifier = Modifier.width(0.5.dp).fillMaxHeight().background(Color.Gray.copy(0.3f)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable{onWatch()}, contentAlignment=Alignment.Center) {
                        Text(stringResource(R.string.watch_ad), fontWeight=FontWeight.Bold, color=MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// Дата для шапки: "15 января, 10:30"
fun getFriendlyDate(time: Long): String {
    return SimpleDateFormat("d MMMM, HH:mm", Locale.getDefault()).format(Date(time))
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
