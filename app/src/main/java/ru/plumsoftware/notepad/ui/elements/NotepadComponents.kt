package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.ui.MainScreenRouteState
import ru.plumsoftware.notepad.ui.theme.Dimens
import ru.plumsoftware.notepad.ui.theme.noteColorOptions
import java.util.Calendar

data class NoteDateRange(val startMillis: Long, val endMillis: Long)

private val shortMonths = listOf(
    "янв", "фев", "мар", "апр", "май", "июн",
    "июл", "авг", "сен", "окт", "ноя", "дек"
)

fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun endOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

fun formatShortDate(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "${cal.get(Calendar.DAY_OF_MONTH)} ${shortMonths[cal.get(Calendar.MONTH)]}"
}

fun formatDateRangeLabel(range: NoteDateRange): String {
    val start = Calendar.getInstance().apply { timeInMillis = range.startMillis }
    val end = Calendar.getInstance().apply { timeInMillis = range.endMillis }
    val startDay = start.get(Calendar.DAY_OF_MONTH)
    val endDay = end.get(Calendar.DAY_OF_MONTH)
    val startMonth = shortMonths[start.get(Calendar.MONTH)]
    val endMonth = shortMonths[end.get(Calendar.MONTH)]
    return if (startMonth == endMonth) {
        "$startDay–$endDay $startMonth"
    } else {
        "$startDay $startMonth – $endDay $endMonth"
    }
}

fun filterNotesByDateRange(notes: List<Note>, range: NoteDateRange?): List<Note> {
    if (range == null) return notes
    val from = startOfDay(range.startMillis)
    val to = endOfDay(range.endMillis)
    return notes.filter { it.createdAt in from..to }
}

enum class SortOrder(val label: String) {
    NEWEST("Сначала новые"),
    OLDEST("Сначала старые"),
    WITH_REMINDERS("С напоминаниями"),
    WITH_PHOTOS("С фото"),
    WITH_TASKS("С задачами");

    companion object {
        fun fromIndex(index: Int): SortOrder = entries.getOrElse(index) { NEWEST }
        fun toIndex(order: SortOrder): Int = order.ordinal
    }
}

@Composable
fun NotepadSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(Dimens.searchBarHeight),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.spacingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingS)
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeMedium),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.note_search),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(Dimens.iconSizeMedium)) {
                    Icon(
                        Icons.Rounded.Cancel,
                        contentDescription = stringResource(R.string.clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NotepadSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(Dimens.spacingXs)
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (selected) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                        .clickable { onSelected(index) }
                        .padding(vertical = Dimens.spacingS),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    activeSection: MainScreenRouteState,
    onSectionSelected: (MainScreenRouteState) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onLayoutToggle: () -> Unit,
    isGrid: Boolean,
    dateRange: NoteDateRange?,
    onDateRangeChange: (NoteDateRange?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPaddingHorizontal)
                .padding(bottom = Dimens.spacingS),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingL),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeSectionTitle(
                    title = stringResource(R.string.notes),
                    isActive = activeSection == MainScreenRouteState.Main,
                    onClick = { onSectionSelected(MainScreenRouteState.Main) }
                )
                HomeSectionTitle(
                    title = stringResource(R.string.habits_title),
                    isActive = activeSection == MainScreenRouteState.Habits,
                    onClick = { onSectionSelected(MainScreenRouteState.Habits) }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)) {
                IconButton(onClick = onLayoutToggle) {
                    Icon(
                        if (isGrid) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onFilterClick) {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPaddingHorizontal)
                .padding(bottom = Dimens.spacingM),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotepadSearchBar(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier.weight(1f)
            )
            NoteDateFilterChip(
                dateRange = dateRange,
                onOpenPicker = { showDatePicker = true },
                onClear = { onDateRangeChange(null) }
            )
        }
    }

    if (showDatePicker) {
        NoteDateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { range ->
                onDateRangeChange(range)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun NoteDateFilterChip(
    dateRange: NoteDateRange?,
    onOpenPicker: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier.height(Dimens.searchBarHeight),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = { if (dateRange == null) onOpenPicker() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spacingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
        ) {
            if (dateRange == null) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = stringResource(R.string.daily_planner),
                    modifier = Modifier.size(Dimens.iconSizeMedium),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = formatDateRangeLabel(dateRange),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClick = onOpenPicker)
                )
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(Dimens.iconSizeMedium)
                ) {
                    Icon(
                        Icons.Rounded.Cancel,
                        contentDescription = stringResource(R.string.clear),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (NoteDateRange) -> Unit
) {
    val state = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        onConfirm(NoteDateRange(start, end))
                    }
                },
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null
            ) {
                Text(stringResource(R.string.ok_))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.padding(bottom = Dimens.spacingS),
            headline = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 16.dp)
                ) {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        Text(
                            text = formatDateRangeLabel(NoteDateRange(start, end)),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.date_range_start),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.date_range_end),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsBottomSheet(
    onDismiss: () -> Unit,
    onAddHabit: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetHeight = (LocalConfiguration.current.screenHeightDp * 0.9f).dp
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
            Button(
                onClick = onAddHabit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPaddingHorizontal)
                    .padding(bottom = Dimens.spacingL)
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(Dimens.spacingS))
                Text(
                    stringResource(R.string.add),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun ScreenHeaderTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
private fun HomeSectionTitle(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        ),
        color = if (isActive) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/** @deprecated Use [HomeTopBar] */
@Composable
fun NotesTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onLayoutToggle: () -> Unit,
    isGrid: Boolean,
    modifier: Modifier = Modifier
) {
    HomeTopBar(
        activeSection = MainScreenRouteState.Main,
        onSectionSelected = {},
        query = query,
        onQueryChange = onQueryChange,
        onFilterClick = onFilterClick,
        onLayoutToggle = onLayoutToggle,
        isGrid = isGrid,
        dateRange = null,
        onDateRangeChange = {},
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    current: SortOrder,
    onSelect: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    NotepadBottomSheet(onDismiss = onDismiss) {
        Text(
            stringResource(R.string.filter_dialog_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal)
        )
        Spacer(Modifier.height(Dimens.spacingL))

        SortOrder.entries.forEach { order ->
            SortRow(
                label = order.label,
                isSelected = current == order,
                onClick = {
                    onSelect(order)
                    onDismiss()
                }
            )
        }
        Spacer(Modifier.height(Dimens.spacingXxl))
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
fun SortRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Dimens.screenPaddingHorizontal, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSizeMedium)
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = Dimens.cardBorderWidth
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerSheet(
    selectedColor: Color?,
    onSelect: (Color?) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = noteColorOptions()
    NotepadBottomSheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.padding(Dimens.screenPaddingHorizontal)) {
            Text(
                stringResource(R.string.note_color),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(Dimens.spacingL))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingM)) {
                items(colors) { option ->
                    ColorDot(
                        color = option.color ?: MaterialTheme.colorScheme.surface,
                        label = option.label,
                        isSelected = selectedColor == option.color,
                        onClick = { onSelect(option.color) }
                    )
                }
            }
            Spacer(Modifier.height(Dimens.spacingXxl))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun ColorDot(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(Dimens.colorDotSize)
                .clip(CircleShape)
                .background(color)
                .border(
                    if (isSelected) 2.dp else Dimens.cardBorderWidth,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeMedium),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(Dimens.spacingXs))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PinKeypad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val rows = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
        listOf(-1, 0, -2)
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingM),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXl)) {
                row.forEach { digit ->
                    when (digit) {
                        -1 -> Box(modifier = Modifier.size(Dimens.pinButtonSize)) {
                            if (onCancel != null) {
                                TextButton(onClick = onCancel) {
                                    Text(
                                        stringResource(R.string.cancel),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        -2 -> IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(Dimens.pinButtonSize)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Backspace,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeLarge),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        else -> PinButton(digit = digit, onClick = { onDigit(digit) })
                    }
                }
            }
        }
    }
}

@Composable
fun NoteEditToolbar(
    onTasksClick: () -> Unit,
    onImageClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onColorClick: () -> Unit,
    onReminderClick: () -> Unit,
    wordCountText: String? = null
) {
    Column {
        wordCountText?.let { text ->
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.spacingXs),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(Dimens.cardBorderWidth, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .wrapContentHeight()
                    .padding(horizontal = Dimens.spacingL),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    androidx.compose.material.icons.Icons.Outlined.CheckBox to onTasksClick,
                    androidx.compose.material.icons.Icons.Outlined.Image to onImageClick,
                    androidx.compose.material.icons.Icons.Outlined.Mic to onVoiceClick,
                    Icons.Outlined.Palette to onColorClick,
                    androidx.compose.material.icons.Icons.Outlined.Notifications to onReminderClick
                ).forEach { (icon, action) ->
                    IconButton(onClick = action) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSizeLarge),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinButton(digit: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(Dimens.pinButtonSize),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                digit.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
