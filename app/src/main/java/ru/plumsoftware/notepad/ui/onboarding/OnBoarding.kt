package ru.plumsoftware.notepad.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.settings.IOSSwitch

private const val PAGE_COUNT = 4

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()

    // Выбор разрешений на 4-м экране (без резервных копий)
    var wantNotifications by remember { mutableStateOf(true) }
    var wantMicrophone by remember { mutableStateOf(true) }
    var wantMedia by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { onFinished() }

    fun requestSelectedPermissions() {
        val perms = mutableListOf<String>()
        if (wantNotifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (wantMicrophone) perms.add(Manifest.permission.RECORD_AUDIO)
        if (wantMedia) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (perms.isEmpty()) onFinished() else permissionLauncher.launch(perms.toTypedArray())
    }

    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == PAGE_COUNT - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Верхняя строка: «N из 4» + «Пропустить»
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ob_page_indicator, currentPage + 1, PAGE_COUNT),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            if (!isLastPage) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.clickable { onFinished() }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> FormatPage()
                1 -> VoicePage()
                2 -> SafePage()
                else -> PermissionsPage(
                    wantNotifications = wantNotifications,
                    onNotifications = { wantNotifications = it },
                    wantMicrophone = wantMicrophone,
                    onMicrophone = { wantMicrophone = it },
                    wantMedia = wantMedia,
                    onMedia = { wantMedia = it }
                )
            }
        }

        // Индикатор + кнопки
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageDots(currentPage = currentPage)
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentPage > 0) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(currentPage - 1)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = stringResource(R.string.back_button),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(start = 3.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isLastPage) {
                            requestSelectedPermissions()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isLastPage) stringResource(R.string.ob_allow_start)
                        else stringResource(R.string.onboarding_next),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (isLastPage) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.ob_not_now),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable { onFinished() }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun PageDots(currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(PAGE_COUNT) { i ->
            val selected = i == currentPage
            val width by animateDpAsState(if (selected) 22.dp else 8.dp, label = "dotWidth")
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

// --- Общий каркас страницы: иллюстрация сверху, заголовок и описание снизу ---
@Composable
private fun PageScaffold(
    title: String,
    description: String,
    illustration: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        illustration()
        Spacer(Modifier.weight(1f))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 38.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(24.dp))
    }
}

// --- Страница 1: форматирование ---
@Composable
private fun FormatPage() {
    val primary = MaterialTheme.colorScheme.primary
    PageScaffold(
        title = stringResource(R.string.ob_format_title),
        description = stringResource(R.string.ob_format_desc)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.ob_note_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            val link = SpanStyle(color = primary, textDecoration = TextDecoration.Underline)
            val mockText = buildAnnotatedString {
                append("Позвонить ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Ивану") }
                append(" в 15:00 по номеру ")
                withStyle(link) { append("+7 999 123-45-67") }
                append("\n")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("Не забыть") }
                append(" открыть ")
                withStyle(link) { append("notes.app/brief") }
                append(" и ")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append("сверить цифры") }
            }
            Text(
                text = mockText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormatChip(icon = Icons.Default.FormatBold, highlighted = true)
                FormatChip(icon = Icons.Default.FormatItalic)
                FormatChip(icon = Icons.Default.FormatUnderlined)
                FormatChip(icon = Icons.Outlined.CheckBox)
                FormatChip(icon = Icons.Outlined.Link)
            }
        }
    }
}

@Composable
private fun FormatChip(icon: ImageVector, highlighted: Boolean = false) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (highlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// --- Страница 2: голос, файлы, фото ---
@Composable
private fun VoicePage() {
    PageScaffold(
        title = stringResource(R.string.ob_voice_title),
        description = stringResource(R.string.ob_voice_desc)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Мок-плеер
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow, null,
                        tint = Color.White, modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    MockWaveform(accent = Color(0xFFFF3B30))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "0:12 / 0:47",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.AttachFile,
                    iconBg = Color(0xFFFCE4EC),
                    iconTint = Color(0xFFE91E63),
                    title = stringResource(R.string.ob_files_title),
                    subtitle = stringResource(R.string.ob_files_desc)
                )
                MiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Image,
                    iconBg = Color(0xFFE8F5E9),
                    iconTint = Color(0xFF43A047),
                    title = stringResource(R.string.ob_photo_title),
                    subtitle = stringResource(R.string.ob_photo_desc)
                )
            }
            WideCard(
                icon = Icons.Outlined.AutoAwesome,
                iconBg = Color(0xFFE8F5E9),
                iconTint = Color(0xFF00A86B),
                text = stringResource(R.string.ob_transcribe)
            )
        }
    }
}

// --- Страница 3: напоминания, календарь, корзина, виджеты ---
@Composable
private fun SafePage() {
    PageScaffold(
        title = stringResource(R.string.ob_safe_title),
        description = stringResource(R.string.ob_safe_desc)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFFFE7C2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Notifications, null,
                        tint = Color(0xFFFF9500), modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.ob_reminder_when),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.ob_reminder_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                IOSSwitch(checked = true, onCheckedChange = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CalendarMonth,
                    iconBg = Color(0xFFE6F1FB),
                    iconTint = Color(0xFF2663EB),
                    title = stringResource(R.string.ob_calendar_title),
                    subtitle = stringResource(R.string.ob_calendar_desc)
                )
                MiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Delete,
                    iconBg = Color(0xFFF0F0F2),
                    iconTint = Color(0xFF8E8E93),
                    title = stringResource(R.string.ob_trash_title),
                    subtitle = stringResource(R.string.ob_trash_desc)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.ob_widgets),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// --- Страница 4: разрешения (без резервных копий) ---
@Composable
private fun PermissionsPage(
    wantNotifications: Boolean,
    onNotifications: (Boolean) -> Unit,
    wantMicrophone: Boolean,
    onMicrophone: (Boolean) -> Unit,
    wantMedia: Boolean,
    onMedia: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ob_perm_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 38.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.ob_perm_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(24.dp))

        PermissionRow(
            icon = Icons.Outlined.Notifications,
            iconBg = Color(0xFFFFE7C2),
            iconTint = Color(0xFFFF9500),
            title = stringResource(R.string.ob_perm_notif),
            subtitle = stringResource(R.string.ob_perm_notif_desc),
            checked = wantNotifications,
            onChecked = onNotifications
        )
        Spacer(Modifier.height(12.dp))
        PermissionRow(
            icon = Icons.Outlined.Mic,
            iconBg = Color(0xFFFCE4EC),
            iconTint = Color(0xFFE91E63),
            title = stringResource(R.string.ob_perm_mic),
            subtitle = stringResource(R.string.ob_perm_mic_desc),
            checked = wantMicrophone,
            onChecked = onMicrophone
        )
        Spacer(Modifier.height(12.dp))
        PermissionRow(
            icon = Icons.Outlined.PhotoLibrary,
            iconBg = Color(0xFFE8F5E9),
            iconTint = Color(0xFF43A047),
            title = stringResource(R.string.ob_perm_media),
            subtitle = stringResource(R.string.ob_perm_media_desc),
            checked = wantMedia,
            onChecked = onMedia
        )
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        IOSSwitch(checked = checked, onCheckedChange = onChecked)
    }
}

// --- Мелкие переиспользуемые элементы ---

@Composable
private fun MiniCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun WideCard(icon: ImageVector, iconBg: Color, iconTint: Color, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun MockWaveform(accent: Color) {
    val heights = listOf(
        0.4f, 0.7f, 1f, 0.6f, 0.85f, 0.5f, 0.3f, 0.6f, 0.9f, 0.5f,
        0.35f, 0.7f, 0.55f, 0.8f, 0.45f, 0.3f, 0.6f, 0.5f, 0.75f, 0.4f
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEachIndexed { index, h ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((28 * h).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < 7) accent
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                    )
            )
        }
    }
}
