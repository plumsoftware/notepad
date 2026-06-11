package ru.plumsoftware.notepad.ui.habit.add_habit

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import ru.plumsoftware.notepad.ui.ads.YandexStickyBanner
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import ru.plumsoftware.notepad.App
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.habit.HabitFrequency
import ru.plumsoftware.notepad.data.theme_saver.ThemeState
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.elements.habits.ColorSelectorRow
import ru.plumsoftware.notepad.ui.elements.habits.EmojiPickerDialog
import ru.plumsoftware.notepad.ui.elements.habits.WeekDaySelector
import ru.plumsoftware.notepad.ui.settings.IOSSettingsGroup
import ru.plumsoftware.notepad.ui.settings.IOSSettingsItem
import ru.plumsoftware.notepad.ui.settings.IOSSwitch

@SuppressLint("MutableCollectionMutableState", "UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    activity: Activity,
    navController: NavController,
    viewModel: NoteViewModel,
    themeState: ThemeState,
    habitId: String? = null
) {
    // --- ADS STATE ---
    var myInterstitialAds: InterstitialAd? by remember { mutableStateOf(null) }
    var interstitialRetryCount by remember { mutableIntStateOf(0) }
    val maxRetries = 1

    val interstitialAdsLoader = remember { InterstitialAdLoader(activity) }
    // Межстраничная реклама при создании привычки временно отключена
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
//                            override fun onAdFailedToShow(adError: AdError) {
//                                navController.navigateUp()
//                            }
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

    // --- ЛОГИКА ЭКРАНА ---
    val habits by viewModel.habits.collectAsState()

    val editingHabit by remember(habits, habitId) {
        derivedStateOf {
            if (habitId == null) null
            else habits.find { it.habit.id == habitId }?.habit
        }
    }

    val isEditing = habitId != null
    val context = activity.baseContext

    // States
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🔥") }
    var selectedColor by remember { mutableStateOf(Color(0xFF007AFF)) }
    var isDaily by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(setOf(2, 3, 4, 5, 6)) }
    var hasReminder by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableIntStateOf(9) }
    var reminderMinute by remember { mutableIntStateOf(0) }

    LaunchedEffect(editingHabit) {
        editingHabit?.let { habit ->
            title = habit.title
            emoji = habit.emoji
            selectedColor = Color(habit.color.toULong())
            isDaily = habit.frequency == HabitFrequency.DAILY
            selectedDays = if (habit.repeatDays.isNotEmpty()) habit.repeatDays.toSet() else setOf(
                2, 3, 4, 5, 6
            )
            hasReminder = habit.isReminderEnabled
            reminderHour = habit.reminderHour ?: 9
            reminderMinute = habit.reminderMinute ?: 0
        }
    }

    val backgroundColor =
        if (themeState.isDarkTheme) Color.Black else MaterialTheme.colorScheme.surface
    val sectionColor = if (themeState.isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)

    // Dialogs
    var showTimePicker by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // ==========================================
    // --- ЕДИНАЯ ЛОГИКА ВЫХОДА И СОХРАНЕНИЯ ---
    // ==========================================
    var isExiting by remember { mutableStateOf(false) } // Защита от двойного клика

    val performExit: (isSaveAction: Boolean) -> Unit = { isSaveAction ->
        if (!isExiting) {
            isExiting = true

            // 1. Сохраняем, если это явное действие "Сохранить"
            if (isSaveAction && title.isNotBlank()) {
                if (isEditing) {
                    viewModel.updateHabit(
                        editingHabit!!.copy(
                            title = title,
                            color = selectedColor.value.toLong(),
                            emoji = emoji,
                            frequency = if (isDaily) HabitFrequency.DAILY else HabitFrequency.SPECIFIC_DAYS,
                            repeatDays = if (isDaily) emptyList() else selectedDays.toList(),
                            isReminderEnabled = hasReminder,
                            reminderHour = if (hasReminder) reminderHour else null,
                            reminderMinute = if (hasReminder) reminderMinute else null
                        )
                    )
                } else {
                    viewModel.createHabit(
                        title = title,
                        color = selectedColor.value.toLong(),
                        emoji = emoji,
                        isDaily = isDaily,
                        days = selectedDays,
                        hasReminder = hasReminder,
                        hour = reminderHour,
                        minute = reminderMinute
                    )
                }
            }

            // Межстраничная реклама при выходе временно отключена
//            if (habits.size >= 2 && myInterstitialAds != null) {
//                myInterstitialAds?.show(activity)
//            } else {
                navController.navigateUp()
//            }
        }
    }

    // Перехват системного жеста "Назад" (Свайп / Нижняя кнопка телефона)
    // При свайпе мы НЕ сохраняем данные (isSaveAction = false), но показываем рекламу
    androidx.activity.compose.BackHandler(enabled = !isExiting) {
        performExit(false)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка Назад
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isExiting) { performExit(false) } // ВЫХОД БЕЗ СОХРАНЕНИЯ
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.back_button),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Заголовок
                Text(
                    text = if (isEditing) stringResource(R.string.habit_edit_title) else stringResource(R.string.habit_new_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Кнопка Сохранить
                Text(
                    text = stringResource(R.string.save),
                    color = if (title.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(enabled = title.isNotBlank() && !isExiting) {
                            performExit(true) // ВЫХОД С СОХРАНЕНИЕМ
                        }
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        bottomBar = {
            YandexStickyBanner()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 🔥 СЕКЦИЯ ШАБЛОНОВ (Только если создаем новую, не редактируем)
            if (!isEditing) {
                IOSSectionHeader(
                    text = stringResource(R.string.templates_title),
                    topPadding = 10.dp
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getHabitTemplates()) { template ->
                        TemplateItem(
                            template = template,
                            onClick = {
                                title = context.getString(template.titleRes)
                                emoji = template.emoji
                                selectedColor = template.color
                                isDaily = template.isDaily
                                selectedDays = if (template.isDaily) setOf(2, 3, 4, 5, 6) else template.days
                                hasReminder = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }

            // 1. ПРЕВЬЮ
            IOSSettingsGroup(backgroundColor = sectionColor) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(selectedColor.copy(0.2f))
                            .clickable { showEmojiPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    BasicTextField(
                        value = title, onValueChange = { title = it },
                        textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = {
                            if (title.isEmpty()) Text(
                                text = stringResource(R.string.habit_name_hint),
                                color = Color.Gray,
                                style = MaterialTheme.typography.titleLarge
                            ) else it()
                        }
                    )
                }
            }

            // 2. РАСПИСАНИЕ
            IOSSectionHeader(text = stringResource(R.string.habit_frequency))
            IOSSettingsGroup(backgroundColor = sectionColor) {
                IOSSettingsItem(
                    icon = Icons.Default.Repeat,
                    iconColor = Color.Gray,
                    title = stringResource(R.string.habit_daily),
                    showDivider = !isDaily,
                    trailingContent = { IOSSwitch(isDaily, { isDaily = it }) })
                if (!isDaily) WeekDaySelector(
                    selectedDays,
                    { d ->
                        selectedDays =
                            if (selectedDays.contains(d)) selectedDays - d else selectedDays + d
                    },
                    selectedColor
                )
            }

            // 3. НАПОМИНАНИЕ
            IOSSectionHeader(text = stringResource(R.string.habit_reminder))
            IOSSettingsGroup(backgroundColor = sectionColor) {
                IOSSettingsItem(
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFFFF2D55),
                    title = stringResource(R.string.habit_reminder),
                    showDivider = hasReminder,
                    trailingContent = { IOSSwitch(hasReminder, { hasReminder = it }) })
                if (hasReminder) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = stringResource(R.string.habit_time),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            String.format("%02d:%02d", reminderHour, reminderMinute),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // 4. ЦВЕТ
            IOSSectionHeader(text = stringResource(R.string.habit_color_label))
            IOSSettingsGroup(backgroundColor = sectionColor) {
                ColorSelectorRow(
                    selectedColor,
                    { selectedColor = it })
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(reminderHour, reminderMinute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderHour = timeState.hour; reminderMinute =
                    timeState.minute; showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Отмена") } },
            text = { TimePicker(state = timeState) }
        )
    }
    if (showEmojiPicker) EmojiPickerDialog(
        { showEmojiPicker = false },
        { emoji = it; showEmojiPicker = false })
}

// Компоненты UI
@Composable
fun IOSSectionHeader(text: String, topPadding: Dp = 24.dp) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray,
        modifier = Modifier.padding(start = 20.dp, top = topPadding, bottom = 8.dp)
    )
}

data class HabitTemplate(
    val titleRes: Int,
    val emoji: String,
    val color: Color,
    val isDaily: Boolean,
    val days: Set<Int>
)

fun getHabitTemplates(): List<HabitTemplate> {
    return listOf(
        HabitTemplate(R.string.tpl_water, "💧", Color(0xFF5AC8FA), true, emptySet()),
        HabitTemplate(R.string.tpl_sport, "🏋️", Color(0xFFFF9500), false, setOf(2, 4, 6)),
        HabitTemplate(R.string.tpl_read, "📚", Color(0xFF34C759), true, emptySet()),
        HabitTemplate(R.string.tpl_sleep, "😴", Color(0xFF5856D6), true, emptySet()),
        HabitTemplate(R.string.tpl_meditation, "🧘", Color(0xFFFF2D55), true, emptySet()),
        HabitTemplate(R.string.tpl_walk, "🚶", Color(0xFF007AFF), false, setOf(1, 7))
    )
}

@Composable
fun TemplateItem(
    template: HabitTemplate,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(template.color.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = template.emoji, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(template.titleRes),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}