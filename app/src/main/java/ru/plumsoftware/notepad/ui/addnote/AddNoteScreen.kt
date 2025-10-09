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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@SuppressLint("MutableCollectionMutableState", "UnrememberedMutableInteractionSource")
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
        // Зелёный → приглушённый мох/мягкий оливковый
        Color(0xFF81C784).value,   // был 0xFFA5D6A7 → теперь глубже и спокойнее

        // Розовый → приглушённый пудрово-розовый, почти серо-розовый
        Color(0xFFF48FB1).value,   // был 0xFFF8BBD0 → меньше насыщенности, больше серого

        // Голубой → приглушённый сине-голубой (как утреннее небо)
        Color(0xFF64B5F6).value,   // был 0xFF90CAF9 → темнее и спокойнее

        // Янтарь → приглушённый тёплый янтарь, чуть темнее
        Color(0xFFFFCA28).value,   // был 0xFFFFD54F → теперь глубже и сдержаннее

        // Фиолетовый → приглушённый лавандово-сиреневый
        Color(0xFFAB47BC).value,   // был 0xFFCE93D8 → насыщеннее, но не яркий

        // Лавандовый → приглушённый серо-фиолетовый
        Color(0xFF9575CD).value,   // был 0xFFB39DDB → глубже, ближе к пыльной лаванде

        // Сине-лавандовый → приглушённый мягкий индиго
        Color(0xFF7986CB).value,   // был 0xFF9FA8DA → темнее, с серым подтоном

        // Бирюзовый → приглушённый морской бирюзовый
        Color(0xFF4DB6AC).value,   // был 0xFF80CBC4 → глубже, как вода в тени

        // Коралл → приглушённый терракотово-персиковый
        Color(0xFFFF8A65).value    // был 0xFFFFAB91 → теплее, но не "фруктовый"
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
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
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
                Spacer(modifier = Modifier.height(20.dp))
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
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
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
                Spacer(modifier = Modifier.height(40.dp))

                // Reminder Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .padding(bottom = 18.dp)
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
                    Spacer(modifier = Modifier.width(10.dp))
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

                // Photos
                Text(
                    stringResource(R.string.photos),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                        Card(
                            modifier = Modifier
                                .size(80.dp)
                                .padding(end = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.4f),
                                contentColor = Color(selectedColor)
                            ),
                            enabled = !isLoading,
                            onClick = { pickImages.launch("image/*") }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Photo",
                                    tint = Color(selectedColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tasks
                Text(
                    stringResource(R.string.tasks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
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
                                uncheckedColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
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

                // Add Task
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        label = {
                            Text(
                                modifier = Modifier.clip(MaterialTheme.shapes.extraLarge),
                                text = stringResource(R.string.new_task),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
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
                        Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Color Picker
                Text(
                    stringResource(R.string.note_color),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
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
            }

            // Loading Dialog
            if (isLoading) {
                LoadingDialog()
            }
        }
    }
}