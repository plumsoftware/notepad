package ru.plumsoftware.notepad.ui.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import ru.plumsoftware.notepad.data.theme_saver.ThemeState
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.theme_saver.saveDarkThemePreference
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    activity: Activity,
    navController: NavController,
    themeState: ThemeState
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Состояние оптимизации батареи
    var isBatteryUnrestricted by remember { mutableStateOf(checkBatteryOptimization(context)) }

    // Слушатель жизненного цикла: обновляем статус, когда пользователь возвращается из настроек
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryUnrestricted = checkBatteryOptimization(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.settings), // Убедитесь, что ресурс существует
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Пустая иконка для баланса заголовка по центру
                    IconButton(
                        enabled = false,
                        onClick = {},
                        colors = IconButtonDefaults.iconButtonColors(disabledContentColor = Color.Transparent)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(top = 18.dp, start = 14.dp, end = 14.dp)
        ) {
            // --- 1. ТЕМА ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.dark_theme),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IOSSwitch(
                    checked = themeState.isDarkTheme,
                    onCheckedChange = { checked ->
                        themeState.isDarkTheme = checked
                        saveDarkThemePreference(checked, context)
                        if (checked) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                    }
                )
            }

            // Разделитель (как в iOS)
            HorizontalDivider(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 0.5.dp
            )

            // --- 2. ФОНОВАЯ РАБОТА (ОПТИМИЗАЦИЯ БАТАРЕИ) ---
            IOSSettingsRow(
                title = "Фоновая работа", // Или stringResource(R.string.background_mode)
                statusText = if (isBatteryUnrestricted) "Разрешено" else "Ограничено",
                isStatusPositive = isBatteryUnrestricted,
                onClick = {
//                    if (!isBatteryUnrestricted) {
                        requestIgnoreBatteryOptimization(context)
//                    }
                }
            )
        }
    }
}

// Вспомогательный компонент для строки настроек в стиле iOS
@Composable
fun IOSSettingsRow(
    title: String,
    statusText: String,
    isStatusPositive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 16.dp), // Чуть больше отступ для кликабельности
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                // Если "Разрешено" - зеленый (iOS Green), иначе - серый
                color = if (isStatusPositive) Color(0xFF34C759) else Color.Gray
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Стрелочка, указывающая на переход (шеврон)
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- ФУНКЦИИ ЛОГИКИ ---

// Проверка: true, если ограничений НЕТ (приложение в белом списке)
fun checkBatteryOptimization(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

// Запрос на отключение оптимизации
@SuppressLint("BatteryLife")
fun requestIgnoreBatteryOptimization(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Фоллбек на общие настройки, если прямой интент не сработал
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    }
}

// Кастомный iOS-style Switch
@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val animationDuration = 200
    val thumbSize = 28.dp  // Увеличил тумблер
    val trackHeight = 32.dp // Увеличил высоту трека
    val trackWidth = 52.dp  // Увеличил ширину трека

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - 2.dp else 2.dp,
        animationSpec = tween(durationMillis = animationDuration),
        label = "thumb_animation"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF34C759) else Color(0xFFE9E9EA),
        animationSpec = tween(durationMillis = animationDuration),
        label = "track_color_animation"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked) Color.White else Color.White,
        animationSpec = tween(durationMillis = animationDuration),
        label = "thumb_color_animation"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            }
    ) {
        // Трек (фон)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = trackColor,
                    shape = RoundedCornerShape(50)
                )
        )

        // Тумблер (ползунок)
        Box(
            modifier = Modifier
                .size(thumbSize)
                .offset(x = thumbOffset, y = 2.dp)
                .background(
                    color = thumbColor,
                    shape = CircleShape
                )
        )
    }
}