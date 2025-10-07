package ru.plumsoftware.notepad.ui.addnote

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Done
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
import ru.plumsoftware.notepad.data.model.AdsConfig
import ru.plumsoftware.notepad.ui.elements.DeleteButton
import ru.plumsoftware.notepad.ui.theme.shapes

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    activity: Activity,
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
        AdRequestConfiguration.Builder(AdsConfig.HuaweiAppGalleryAds().interstitialAdsId).build()
    interstitialAdsLoader.loadAd(adRequestConfiguration)

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
        // Мягкий зелёный → чуть глубже
        Color(0xFFA5D6A7).value,  // было 0xFFC8E6C9 → теперь сочнее, но не кислотный

        // Розовый → чуть насыщеннее и темнее
        Color(0xFFF8BBD0).value,  // было 0xFFFFCDD2 → теперь мягкий розовый с контрастом ~7:1 для белого

        // Голубой → чуть темнее
        Color(0xFF90CAF9).value,  // было 0xFFBBDEFB → теперь лучше контраст

        // Жёлтый → заменим на тёплый янтарь (чистый жёлтый плохо сочетается с белым)
        Color(0xFFFFE082).value,  // вместо почти белого жёлтого — мягкий янтарь, контраст ~10:1

        // Фиолетовый → чуть насыщеннее
        Color(0xFFCE93D8).value,  // было 0xFFE1BEE7 → теперь лучше читается

        // Лавандовый → чуть глубже
        Color(0xFFB39DDB).value,  // было 0xFFD1C4E9

        // Сине-лавандовый → чуть темнее
        Color(0xFF9FA8DA).value,  // было 0xFFC5CAE9

        // Бирюзовый → чуть насыщеннее
        Color(0xFF80CBC4).value,  // было 0xFFB2DFDB → классический мягкий бирюзовый

        // Персиковый → заменим на тёплый коралл
        Color(0xFFFFAB91).value   // было 0xFFFFCCBC → теперь контрастный и тёплый
    )

    var selectedColor by remember { mutableStateOf(note?.color?.toULong() ?: colors.first()) }

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
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back", tint = Color.White)
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
                                        viewModel.updateNote(updatedNote, context)
                                    } else {
                                        playSound(context, exoPlayer, R.raw.note_create)
                                        viewModel.addNote(updatedNote)
                                    }
                                    myInterstitialAds?.apply {
                                        setAdEventListener(object : InterstitialAdEventListener {
                                            override fun onAdShown() {}
                                            override fun onAdFailedToShow(adError: AdError) {
                                                navController.popBackStack()
                                            }

                                            override fun onAdDismissed() {
                                                navController.popBackStack()
                                            }

                                            override fun onAdClicked() {
                                                navController.popBackStack()
                                            }

                                            override fun onAdImpression(impressionData: ImpressionData?) {}
                                        })
                                        show(activity)
                                    }
                                    navController.navigateUp()
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
                            ),
                        textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = Color.White),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            innerTextField()
                        }
                    )

                    // ✅ Правильно: выравниваем через внешний Box
                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        AnimatedVisibility(
                            visible = title.isNotEmpty(),
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
                Spacer(modifier = Modifier.height(30.dp))
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
                            ),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, color = Color.White),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            innerTextField()
                        }
                    )

                    // ✅ Правильно: выравниваем через внешний Box
                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        AnimatedVisibility(
                            visible = description.isNotEmpty(),
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
                    Text(
                        text = stringResource(R.string.remind),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                // Display selected reminder date
                reminderDate?.let {
                    Text(
                        text = String.format(stringResource(R.string.reminder), formatDate(it)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }

                // Photos
                Text(stringResource(R.string.photos), style = MaterialTheme.typography.bodyLarge)
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
                Text(stringResource(R.string.tasks), style = MaterialTheme.typography.bodyLarge)
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
                        DeleteButton(onDelete = {
                            tasks = tasks.toMutableList().apply { removeAt(index) }
                        }, enabled = !isLoading)
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        label = {
                            Text(
                                modifier = Modifier.clip(MaterialTheme.shapes.extraLarge),
                                text = stringResource(R.string.new_task),
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
                Text(
                    stringResource(R.string.note_color),
                    style = MaterialTheme.typography.bodyLarge
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
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