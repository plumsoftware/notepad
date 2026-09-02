package ru.plumsoftware.notepad.ui.addnote

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.luminance
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.foundation.layout.offset
import ru.plumsoftware.notepad.ui.dialog.MoveToGroupDialog
import ru.plumsoftware.notepad.ui.elements.blueShadow
import ru.plumsoftware.notepad.ui.settings.IOSSwitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import ru.plumsoftware.notepad.ui.elements.ColorPickerSheet
import ru.plumsoftware.notepad.ui.elements.NoteEditToolbar
import ru.plumsoftware.notepad.ui.theme.Dimens
import ru.plumsoftware.notepad.ui.theme.noteColorOptions
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
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
import androidx.core.content.ContextCompat
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
import com.yandex.mobile.ads.common.AdRequest
import ru.plumsoftware.notepad.ui.ads.YandexStickyBanner
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
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
    // --- ADS STATE ---
    var rewardedAd: RewardedAd? by remember { mutableStateOf(null) }
    var myInterstitialAds: InterstitialAd? by remember { mutableStateOf(null) }

    var isAdsLoading by remember { mutableStateOf(false) }

    var rewardedRetryCount by remember { mutableIntStateOf(0) }
    var interstitialRetryCount by remember { mutableIntStateOf(0) }
    val maxRetries = 5

    val context = LocalContext.current

    val rewardedAdLoader = remember { RewardedAdLoader(context) }
    val interstitialAdsLoader = remember { InterstitialAdLoader(activity) }

    val rewardedRequest = remember {
        AdRequest.Builder(App.platformConfig.adsConfig.rewardedAdsId).build()
    }

    // --- ADS LOAD LOGIC ---
    // Загрузка Rewarded
    LaunchedEffect(rewardedRetryCount) {
        if (rewardedAd == null && rewardedRetryCount < maxRetries) {
            isAdsLoading = true
            rewardedAdLoader.loadAd(rewardedRequest, object : RewardedAdLoadListener {
                override fun onAdLoaded(rewarded: RewardedAd) {
                    isAdsLoading = false
                    rewardedAd = rewarded
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    rewardedRetryCount++
                }
            })
        } else {
            isAdsLoading = false
        }
    }

    // Межстраничная реклама при создании заметки временно отключена
//    LaunchedEffect(interstitialRetryCount) {
//        if (myInterstitialAds == null && interstitialRetryCount < maxRetries) {
//            interstitialAdsLoader.loadAd(
//                AdRequest.Builder(App.platformConfig.adsConfig.interstitialAdsId).build(),
//                object : InterstitialAdLoadListener {
//                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                        myInterstitialAds = interstitialAd
//                        myInterstitialAds?.setAdEventListener(object : InterstitialAdEventListener {
//                            override fun onAdClicked() {}
//                            override fun onAdDismissed() {
//                                navController.navigateUp()
//                            }
//
//                            override fun onAdFailedToShow(adError: AdError) {
//                                navController.navigateUp()
//                            }
//
//                            override fun onAdImpression(impressionData: ImpressionData?) {}
//                            override fun onAdShown() {}
//                        })
//                    }
//
//                    override fun onAdFailedToLoad(error: AdRequestError) {
//                        interstitialRetryCount++
//                    }
//                }
//            )
//        }
//    }

    // --- State ---
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

    // Папка (выбор через существующий MoveToGroupDialog)
    var selectedGroupId by remember { mutableStateOf(note?.groupId ?: "0") }
    var showMoveDialog by remember { mutableStateOf(false) }
    val groupsWithCount by viewModel.groups.collectAsState()

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
    var showVoiceDialog by remember { mutableStateOf(false) }
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val exoPlayer = rememberExoPlayer()

    // --- Цвета в стиле iOS ---
    val availableColors = listOf(
        Color(0xFFFFFFFF), // Белый (White)
        Color(0xFFF2F2F7), // Светло-серый (System Gray 6)
        Color(0xFFFFF5BA), // Пастельный желтый (Apple Notes Yellow)
        Color(0xFFFFD8D6), // Нежный красный/розовый (Pastel Red)
        Color(0xFFFFE5B4), // Нежный оранжевый (Pastel Orange)
        Color(0xFFD4F0CD), // Мятно-зеленый (Pastel Green)
        Color(0xFFB4D5FA), // Светло-голубой (Pastel Blue)
        Color(0xFFD5C0F9), // Нежно-фиолетовый (Pastel Purple)
        Color(0xFFEBE3D5)  // Нежно-коричневый/бежевый (Pastel Brown)
    )

    var selectedColor by remember {
        mutableStateOf(note?.color?.let { Color(it.toULong()) } ?: availableColors.first())
    }

    // Фон приложения не перекрашивается под цвет заметки — он всегда нейтральный,
    // а карточки и текстовые поля белые (surface). Цвет заметки сохраняется как её свойство.
    val actionItemsColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant

    // --- Helpers: Фото и Голос ---
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

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
                showVoiceDialog = true
            } else {
                Toast.makeText(context, "Распознавание речи недоступно", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Требуется разрешение на микрофон", Toast.LENGTH_SHORT).show()
        }
    }

    val onMicClick = {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
                showVoiceDialog = true
            } else {
                Toast.makeText(context, "Распознавание речи недоступно", Toast.LENGTH_SHORT).show()
            }
        } else {
            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // ==========================================
    // --- ЕДИНАЯ ЛОГИКА ВЫХОДА И СОХРАНЕНИЯ ---
    // ==========================================
    var isExiting by remember { mutableStateOf(false) } // Защита от двойного клика

    val performExit: () -> Unit = {
        if (!isExiting) {
            isExiting = true

            // Сохраняем, если есть текст
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
                    groupId = selectedGroupId
                )

                if (isEditing) {
                    if (note?.photos != photos) {
                        deleteImagesFromStorage(
                            context,
                            note?.photos?.filterNot { photos.contains(it) } ?: emptyList())
                    }
                    playSound(context, exoPlayer, R.raw.note_create)
                    viewModel.updateNote(updatedNote, context)
                } else {
                    playSound(context, exoPlayer, R.raw.note_create)
                    viewModel.addNote(updatedNote)
                }
            }

            // Межстраничная реклама при выходе временно отключена
//            if (myInterstitialAds != null) {
//                myInterstitialAds?.show(activity)
//            } else {
                navController.navigateUp()
//            }
        }
    }

    // Перехват системного жеста "Назад" (Свайп / Нижняя кнопка телефона)
    androidx.activity.compose.BackHandler(enabled = !isExiting) {
        performExit()
    }


    // --- DIALOGS ---
    if (showVoiceDialog) {
        IOSVoiceInputDialog(
            context = context,
            onDismiss = { showVoiceDialog = false },
            onResult = { recognizedText ->
                val separator =
                    if (title.isNotEmpty() && !title.endsWith(" ")) " " else ""
                title += "$separator$recognizedText"
                showVoiceDialog = false
            }
        )
    }

    if (showColorSheet) {
        val palette = noteColorOptions()
        ColorPickerSheet(
            selectedColor = palette.find { it.color?.toArgb()?.toLong() == selectedColor.toArgb().toLong() }?.color,
            onSelect = { color ->
                selectedColor = color ?: Color.White
                showColorSheet = false
            },
            onDismiss = { showColorSheet = false }
        )
    }

    if (showAddTaskDialog) {
        IOSAddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { text ->
                tasks.add(Task(text = text))
                showAddTaskDialog = false
            }
        )
    }

    if (showAddPhotoDialog) {
        IOSAdsDialog(
            onDismiss = { showAddPhotoDialog = false },
            onWatch = {
                showAddPhotoDialog = false
                showAd(rewardedAd, rewardedAdLoader, activity) {
                    scope.launch {
                        pickImages.launch("image/*")
                    }
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = reminderDate ?: System.currentTimeMillis()
        )
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
                TextButton(onClick = { showDatePicker = false; isReminder = false }) {
                    Text(stringResource(R.string.cancel))
                }
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

    fullscreenImagePath?.let {
        FullscreenImageDialog(imagePath = it, onDismiss = { fullscreenImagePath = null })
    }

    if (showMoveDialog) {
        MoveToGroupDialog(
            groups = groupsWithCount.map { it.group },
            onDismiss = { showMoveDialog = false },
            onGroupSelected = { groupId ->
                selectedGroupId = groupId
                showMoveDialog = false
            }
        )
    }

    if (isAdsLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            LoadingDialog()
        }
    }

    val cardColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant
    val sectionLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка Назад — скруглённый квадрат
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardColor)
                        .clickable(enabled = !isExiting) { performExit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = stringResource(R.string.back_button),
                        tint = actionItemsColor,
                        modifier = Modifier
                            .size(18.dp)
                            .offset(x = 3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardColor)
                            .clickable {
                                val shareText = buildString {
                                    if (title.isNotBlank()) append(title)
                                    if (description.isNotBlank()) {
                                        if (isNotEmpty()) append("\n\n")
                                        append(description)
                                    }
                                }
                                if (shareText.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, null))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.share_note),
                            tint = actionItemsColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Кнопка «Готово» — синяя пилюля с синей тенью
                    Row(
                        modifier = Modifier
                            .blueShadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = !isLoading && !isExiting) { performExit() }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column {
                YandexStickyBanner(
                    reserveBottomNavBarSpace = false,
                    includeNavigationBarsPadding = false,
                )
                val wordCount = description.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
                Text(
                    text = stringResource(R.string.word_count, wordCount, description.length),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 8.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = getFriendlyDate(note?.createdAt ?: System.currentTimeMillis()),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // --- Заголовок + микрофон ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
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
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardColor)
                        .clickable { onMicClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.MicNone,
                        contentDescription = null,
                        tint = actionItemsColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Описание (карточка) ---
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardColor)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (description.isEmpty()) {
                    Text(
                        text = stringResource(R.string.desc),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 23.sp),
                        color = placeholderColor
                    )
                }
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = contentColor,
                        fontSize = 16.sp,
                        lineHeight = 23.sp
                    ),
                    cursorBrush = SolidColor(contentColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ЗАДАЧИ ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.tasks).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = sectionLabelColor
                )
                if (tasks.isNotEmpty()) {
                    val done = tasks.count { it.isChecked }
                    Text(
                        text = "$done / ${tasks.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = sectionLabelColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardColor)
            ) {
                tasks.forEachIndexed { index, task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (task.isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isChecked) MaterialTheme.colorScheme.primary else contentColor.copy(0.5f),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val isCh = !task.isChecked
                                    tasks = tasks.toMutableList()
                                        .apply { this[index] = task.copy(isChecked = isCh) }
                                }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = task.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (task.isChecked) contentColor.copy(0.4f) else contentColor,
                                textDecoration = if (task.isChecked) TextDecoration.LineThrough else null
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Close, null,
                            tint = contentColor.copy(0.3f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    tasks = tasks.toMutableList().apply { removeAt(index) }
                                }
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 14.dp),
                        color = contentColor.copy(alpha = 0.08f),
                        thickness = 0.5.dp
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddTaskDialog = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.AddCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.new_task),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ФОТО ---
            Text(
                text = stringResource(R.string.photos).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = sectionLabelColor,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(photos) { path ->
                    Box {
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { fullscreenImagePath = path }
                        )
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
                            Icon(
                                Icons.Default.Close,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                if (photos.size < 5) {
                    item {
                        Column(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardColor)
                                .clickable {
                                    if (photos.size == 4) showAddPhotoDialog = true
                                    else pickImages.launch("image/*")
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.add),
                                style = MaterialTheme.typography.labelMedium,
                                color = contentColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ЦВЕТ ЗАМЕТКИ ---
            Text(
                text = stringResource(R.string.note_color).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = sectionLabelColor,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardColor)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(availableColors) { color ->
                        val isSelected = selectedColor == color
                        val swatchShape = RoundedCornerShape(12.dp)
                        // Тот же цвет, но темнее — для обводки и галочки выбранного элемента.
                        val darker = lerp(color, Color.Black, 0.35f)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(swatchShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) darker
                                    else contentColor.copy(alpha = 0.15f),
                                    shape = swatchShape
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = darker,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- НАПОМИНАНИЕ ---
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardColor)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0xFFFFE7C2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFFF9500),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.remind),
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                    if (isReminder && reminderDate != null) {
                        Text(
                            text = formatDate(reminderDate!!),
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.5f)
                        )
                    }
                }
                IOSSwitch(
                    checked = isReminder && reminderDate != null,
                    onCheckedChange = { checked ->
                        if (checked) {
                            isReminder = true
                            showDatePicker = true
                        } else {
                            isReminder = false
                            reminderDate = null
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- ПАПКА ---
            val currentGroupTitle = groupsWithCount
                .firstOrNull { it.group.id == selectedGroupId }?.group?.title
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardColor)
                    .clickable { showMoveDialog = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.move_to_folder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = currentGroupTitle ?: stringResource(R.string.folder_all),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (isLoading) LoadingDialog()
    }
}

private fun showAd(
    rewardedAd: RewardedAd?,
    rewardedAdLoader: RewardedAdLoader?,
    activity: Activity?,
    onRewarded: () -> Unit
) {
    if (rewardedAd != null && activity != null) {
        var isEarnedReward = false
        rewardedAd.setAdEventListener(object : RewardedAdEventListener {
            override fun onAdShown() {}
            override fun onRewarded(reward: Reward) {
                isEarnedReward = true
            }

            override fun onAdFailedToShow(adError: AdError) {
                onRewarded()
            }

            override fun onAdDismissed() {
                if (isEarnedReward) {
                    onRewarded()
                }
            }

            override fun onAdClicked() {}
            override fun onAdImpression(impressionData: ImpressionData?) {}
        })
        rewardedAd.show(activity)
    } else {
        onRewarded()
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ ДИАЛОГИ ---

@Composable
fun IOSVoiceInputDialog(
    context: Context,
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    var partialText by remember { mutableStateOf("Слушаю...") }
    val speechRecognizer =
        remember { android.speech.SpeechRecognizer.createSpeechRecognizer(context) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    DisposableEffect(Unit) {
        val listener = object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                onDismiss()
            }

            override fun onResults(results: Bundle?) {
                val matches =
                    results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                } else {
                    onDismiss()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches =
                    partialResults?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    partialText = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer.setRecognitionListener(listener)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)

        onDispose {
            speechRecognizer.stopListening()
            speechRecognizer.destroy()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(scale)
                            .background(Color(0xFFFF3B30).copy(alpha = alpha), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFFF3B30).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Mic,
                            contentDescription = "Listening",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = partialText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun IOSAddTaskDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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
                ) { onDismiss() }, contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .clickable(enabled = false) {}) {
                Text(
                    stringResource(R.string.new_task),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleMedium
                )
                BasicTextField(
                    value = text, onValueChange = { text = it },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(0.1f))
                        .padding(12.dp)
                        .focusRequester(focusRequester)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .fillMaxHeight()
                            .background(Color.Gray.copy(0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = text.isNotBlank()) {
                                if (text.isNotBlank()) onAdd(text)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.add),
                            fontWeight = FontWeight.Bold,
                            color = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IOSAdsDialog(onDismiss: () -> Unit, onWatch: () -> Unit) {
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
                ) { onDismiss() }, contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .clickable(enabled = false) {},
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.photo_add_ads_promo_title),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    stringResource(R.string.photo_add_ads_promo_description),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Gray.copy(0.3f)
                )
                Row(modifier = Modifier.height(44.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .fillMaxHeight()
                            .background(Color.Gray.copy(0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onWatch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.watch_ad),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

fun getFriendlyDate(time: Long): String {
    return SimpleDateFormat("d MMMM, HH:mm", Locale.getDefault()).format(Date(time))
}

private fun loadRewardedAd(rewardedAdLoader: RewardedAdLoader?) {
    rewardedAdLoader?.loadAd(
        AdRequest.Builder(App.platformConfig.adsConfig.rewardedAdsId).build(),
        object : RewardedAdLoadListener {
            override fun onAdLoaded(rewardedAd: RewardedAd) {}
            override fun onAdFailedToLoad(error: AdRequestError) {}
        }
    )
}
