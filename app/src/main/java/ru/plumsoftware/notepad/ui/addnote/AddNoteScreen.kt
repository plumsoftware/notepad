package ru.plumsoftware.notepad.ui.addnote

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.plumsoftware.notepad.App
import ru.plumsoftware.notepad.data.model.AdsConfig
import ru.plumsoftware.notepad.ui.elements.DeleteButton
import ru.plumsoftware.notepad.ui.theme.shapes

@SuppressLint("MutableCollectionMutableState", "UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    activity: Activity,
    navController: NavController,
    viewModel: NoteViewModel,
    note: Note? = null
) {
    var rewardedAd: RewardedAd? = null
    var rewardedAdLoader: RewardedAdLoader? = null

    rewardedAdLoader = RewardedAdLoader(LocalContext.current).apply {
        setAdLoadListener(object : RewardedAdLoadListener {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
            }

            override fun onAdFailedToLoad(adRequestError: AdRequestError) {}
        })
    }
    loadRewardedAd(rewardedAdLoader)
    val scrollState = rememberScrollState()

    val isEditing = note != null
    var title by remember { mutableStateOf(note?.title ?: "") }
    var description by remember { mutableStateOf(note?.description ?: "") }
    var tasks by remember {
        mutableStateOf<MutableList<Task>>(
            note?.tasks?.toMutableList() ?: mutableListOf()
        )
    }
    val notes by viewModel.notes.collectAsState()
    var newTaskText by remember { mutableStateOf("") }
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
    var myInterstitialAds: InterstitialAd? = null
    val interstitialAdsLoader = InterstitialAdLoader(activity).apply {
        setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                myInterstitialAds = interstitialAd
            }

            override fun onAdFailedToLoad(error: AdRequestError) {

            }
        })
    }
    val adRequestConfiguration =
        AdRequestConfiguration.Builder(App.platformConfig.adsConfig.interstitialAdsId).build()
    interstitialAdsLoader.loadAd(adRequestConfiguration)
    var isExpandedTasks by remember { mutableStateOf(false) }
    var isAdsLoading by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Добавляем состояние для диалога добавления задачи
    var showAddTaskDialog by remember { mutableStateOf(false) }
    val taskTextFocusRequester = remember { FocusRequester() }

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

    val colors = listOf(
        // Зелёные оттенки
        Color(0xFF81C784).value,      // Приглушённый мох
        Color(0xFF4DB6AC).value,      // Бирюзовый

        // Розовые/красные оттенки
        Color(0xFFF48FB1).value,      // Пудрово-розовый
        Color(0xFFFF8A65).value,      // Терракотовый

        // Синие/голубые оттенки
        Color(0xFF64B5F6).value,      // Сине-голубой
        Color(0xFF7986CB).value,      // Сине-лавандовый

        // Фиолетовые оттенки
        Color(0xFFAB47BC).value,      // Лавандово-сиреневый
        Color(0xFF9575CD).value,      // Серо-фиолетовый

        // Жёлтые/оранжевые оттенки
        Color(0xFFFFCA28).value,      // Тёплый янтарь
        Color(0xFFF9A825).value,      // Горчичный

        // Нейтральные оттенки
        Color(0xFF90A4AE).value,      // Шалфейный серо-голубой
        Color(0xFFD7CCC8).value,      // Песочный бежевый

        // Тёмные оттенки
        Color(0xFF283593).value,      // Тёмно-синий (ночной)
        Color(0xFF37474F).value,      // Графитовый
        Color(0xFF1A237E).value       // Глубокий индиго (самый тёмный)
    )

    var selectedColor by remember { mutableStateOf(note?.color?.toULong() ?: colors.first()) }

    // Focus managers for title and description
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    var isTitleFocused by remember { mutableStateOf(false) }
    var isDescriptionFocused by remember { mutableStateOf(false) }

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
                ) {
                    Text(
                        text = stringResource(R.string.ok_),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        isReminder = false
                        reminderDate = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.cansel_),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp),
                title = {
                    Text(
                        stringResource(R.string.when_to_remind_about_event),
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
            title = {
                Text(
                    text = stringResource(R.string.select_time),
                    style = MaterialTheme.typography.titleSmall
                )
            },
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
                ) {
                    Text(
                        stringResource(R.string.ok_),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        isReminder = false
                        reminderDate = null
                    }
                ) {
                    Text(
                        stringResource(R.string.cansel_),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
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

    // Диалог добавления задачи
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.new_task),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(taskTextFocusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.add_task_placeholder),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (newTaskText.isNotBlank()) {
                                tasks.add(Task(text = newTaskText))
                                newTaskText = ""
                                showAddTaskDialog = false
                            }
                        }
                    ),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTaskText.isNotBlank()) {
                            tasks.add(Task(text = newTaskText))
                            newTaskText = ""
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.add),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddTaskDialog = false
                        newTaskText = ""
                    }
                ) {
                    Text(
                        text = stringResource(R.string.cansel_),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        // Автофокус на текстовое поле при показе диалога
        LaunchedEffect(showAddTaskDialog) {
            if (showAddTaskDialog) {
                delay(100) // Небольшая задержка для корректного отображения
                taskTextFocusRequester.requestFocus()
            }
        }
    }

    LaunchedEffect(photos) {
        if (photos.isNotEmpty()) {
            // Небольшая задержка чтобы дать время на обновление UI
            delay(100L)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        containerColor = Color(selectedColor),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
//                    Text(
//                        modifier = Modifier.fillMaxWidth(),
//                        text = if (isEditing) stringResource(R.string.note_editing) else stringResource(
//                            R.string.note_add
//                        ),
//                        style = MaterialTheme.typography.titleMedium,
//                        textAlign = TextAlign.Start
//                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 18.dp)) {
                        Button(
                            shape = MaterialTheme.shapes.extraLarge,
                            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 6.dp),
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
                                        playSound(context, exoPlayer, R.raw.note_create)
                                        viewModel.updateNote(updatedNote.copy(groupId = note.groupId ?: "0"), context)
                                    } else {
                                        playSound(context, exoPlayer, R.raw.note_create)
                                        viewModel.addNote(updatedNote)
                                    }

                                    if (notes.size >= 5) {
                                        if (myInterstitialAds != null) {
                                            myInterstitialAds.apply {
                                                setAdEventListener(object :
                                                    InterstitialAdEventListener {
                                                    override fun onAdShown() {}
                                                    override fun onAdFailedToShow(adError: AdError) {
                                                        navController.navigateUp()
                                                    }

                                                    override fun onAdDismissed() {
                                                        navController.navigateUp()
                                                    }

                                                    override fun onAdClicked() {
                                                        navController.navigateUp()
                                                    }

                                                    override fun onAdImpression(impressionData: ImpressionData?) {}
                                                })
                                                show(activity)
                                            }
                                        } else {
                                            navController.navigateUp()
                                        }
                                    } else {
                                        navController.navigateUp()
                                    }
                                }
                            },
                            enabled = title.isNotBlank() && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(selectedColor),
                                disabledContentColor = Color(selectedColor).copy(alpha = 0.7f),
                                disabledContainerColor = Color.White.copy(alpha = 0.7f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.save),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Часть 1: Заголовок и описание
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    // Placeholder
                    if (title.isEmpty()) {
                        Text(
                            text = stringResource(R.string.title),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 0.dp,
                                    end = if (title.isNotEmpty()) 48.dp else 0.dp
                                )
                        )
                    }

                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 0.dp,
                                end = if (title.isNotEmpty()) 48.dp else 0.dp
                            )
                            .focusRequester(titleFocusRequester)
                            .onFocusChanged { focusState ->
                                isTitleFocused = focusState.isFocused
                            },
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            innerTextField()
                        }
                    )

                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        AnimatedVisibility(
                            visible = title.isNotEmpty() && isTitleFocused,
                            enter = slideInHorizontally { fullWidth -> fullWidth } + fadeIn(),
                            exit = slideOutHorizontally { fullWidth -> fullWidth } + fadeOut()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = Color.White,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(role = Role.Button) { title = "" }
                                    .padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    // Placeholder
                    if (description.isEmpty()) {
                        Text(
                            text = stringResource(R.string.desc),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 0.dp,
                                    end = if (description.isNotEmpty()) 48.dp else 0.dp
                                )
                        )
                    }

                    BasicTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 0.dp,
                                end = if (description.isNotEmpty()) 48.dp else 0.dp
                            )
                            .focusRequester(descriptionFocusRequester)
                            .onFocusChanged { focusState ->
                                isDescriptionFocused = focusState.isFocused
                            },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            innerTextField()
                        }
                    )

                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        AnimatedVisibility(
                            visible = description.isNotEmpty() && isDescriptionFocused,
                            enter = slideInHorizontally { fullWidth -> fullWidth } + fadeIn(),
                            exit = slideOutHorizontally { fullWidth -> fullWidth } + fadeOut()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = Color.White,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(role = Role.Button) { description = "" }
                                    .padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Часть 2: Напоминание
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
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
                        modifier = Modifier
                            .size(14.dp),
                        enabled = !isLoading,
                        interactionSource = MutableInteractionSource(),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.Transparent,
                            uncheckedColor = Color.White.copy(alpha = 0.7f)
                        ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Display selected reminder date
                    reminderDate?.let {
                        Text(
                            text = String.format(stringResource(R.string.reminder), formatDate(it)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    if (reminderDate == null)
                        Text(
                            text = stringResource(R.string.remind),
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                        )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Часть 3: Фото
                Text(
                    stringResource(R.string.photos),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
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
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(
                                        Color.Transparent,
                                        CircleShape
                                    )
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.delete_icon),
                                    contentDescription = "Delete Photo",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    if (photos.size < 5) {
                        Card(
                            modifier = Modifier
                                .size(80.dp)
                                .padding(end = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.4f),
                                contentColor = Color(selectedColor)
                            ),
                            enabled = !isLoading,
                            onClick = {
                                if (photos.size == 4) {
                                    showAddPhotoDialog = true
                                } else {
                                    pickImages.launch("image/*")
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAdsLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(34.dp))
                                } else {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Photo",
                                        tint = Color(selectedColor)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Часть 4: Задачи
                Row(
                    modifier = Modifier.wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.Start
                    )
                ) {
                    Text(
                        stringResource(R.string.tasks),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        modifier = Modifier
                            .rotate(if (!isExpandedTasks) 180f else 0f)
                            .clickable(
                                enabled = true,
                                role = Role.Button,
                                interactionSource = MutableInteractionSource(),
                                indication = null,
                                onClick = {
                                    isExpandedTasks = !isExpandedTasks
                                }
                            ),
                        tint = Color.White.copy(alpha = 0.7f),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }

                if (!isExpandedTasks) {
                    Spacer(modifier = Modifier.height(8.dp))
                    tasks.forEachIndexed { index, task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(start = 4.dp),
                                checked = task.isChecked,
                                onCheckedChange = { isChecked ->
                                    tasks = tasks.toMutableList().apply {
                                        this[index] = task.copy(isChecked = isChecked)
                                    }
                                },
                                enabled = !isLoading,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.Transparent,
                                    uncheckedColor = Color.White.copy(alpha = 0.7f),
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = task.text,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Image(
                                painter = painterResource(R.drawable.delete_icon),
                                contentDescription = "Delete Photo",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(enabled = !isLoading, onClick = {
                                        tasks = tasks.toMutableList().apply { removeAt(index) }
                                    })
                            )
                        }
                    }

                    // Новая кнопка добавления задачи вместо текстового поля
                    OutlinedButton(
                        onClick = { showAddTaskDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.7f)
                        ),
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.add_task),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))

                // Часть 5: Цвет заметки
                Text(
                    stringResource(R.string.note_color),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 4.dp, bottom = 4.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = 2.dp,
                                    color = if (selectedColor == color) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable(enabled = !isLoading) { selectedColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(70.dp))
            }

            // Loading Dialog
            if (isLoading) {
                LoadingDialog()
            }

            if (showAddPhotoDialog) {
                AlertDialog(
                    onDismissRequest = { showAddPhotoDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.photo_add_ads_promo_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.photo_add_ads_promo_description), // Добавьте этот string resource
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showAddPhotoDialog = false
                                isAdsLoading = true

                                showAd(
                                    rewardedAd = rewardedAd,
                                    rewardedAdLoader = rewardedAdLoader,
                                    activity = activity,
                                    onRewarded = {
                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                delay(500L)
                                            }
                                            isAdsLoading = false
                                            pickImages.launch("image/*")
                                        }
                                    }
                                )
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.watch_ad), // "Смотреть"
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAddPhotoDialog = false }
                        ) {
                            Text(
                                text = stringResource(R.string.cancel), // "Отмена"
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun loadRewardedAd(rewardedAdLoader: RewardedAdLoader?) {
    val adRequestConfiguration = AdRequestConfiguration.Builder(App.platformConfig.adsConfig.rewardedAdsId).build()
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
