package ru.plumsoftware.notepad.ui.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import ru.plumsoftware.notepad.data.theme_saver.ThemeState
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.theme_saver.saveDarkThemePreference
import androidx.core.net.toUri
import ru.plumsoftware.notepad.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    activity: Activity,
    navController: NavController,
    themeState: ThemeState
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Logic
    var isBatteryUnrestricted by remember { mutableStateOf(checkBatteryOptimization(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryUnrestricted = checkBatteryOptimization(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Определяем цвета фона как в iOS (Grouped Background)
    // В светлой теме iOS фон серый, а плашки белые. В темной фон черный, плашки темно-серые.
    val backgroundColor = if (themeState.isDarkTheme) Color.Black else Color(0xFFF2F2F7)
    val sectionColor = if (themeState.isDarkTheme) Color(0xFF1C1C1E) else Color.White

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor // Используем фон экрана
                ),
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    // Кнопка Назад (Шеврон + Текст "Назад")
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { navController.navigateUp() }
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null, // Декоративный элемент, текст рядом есть
                            tint = MaterialTheme.colorScheme.primary, // iOS Blue
                            modifier = Modifier.size(20.dp)
                        )
                        // Текст кнопки (обычно "Назад" или название предыдущего экрана)
                        // В твоем коде настроек использовался "back_button"
                        Text(
                            text = stringResource(R.string.back_button),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Невидимая кнопка справа для идеального центрирования заголовка,
                    // если кнопка "Назад" слева широкая.
                    IconButton(
                        onClick = {},
                        enabled = false,
                        colors = IconButtonDefaults.iconButtonColors(
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.Transparent
                        )
                    ) {
                        // Размер должен примерно совпадать с иконкой слева, чтобы заголовок не "уезжал"
                        Box(modifier = Modifier.size(24.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- СЕКЦИЯ 1: ВНЕШНИЙ ВИД ---
            IOSSettingsGroup(backgroundColor = sectionColor) {
                IOSSettingsItem(
                    icon = Icons.Default.DarkMode,
                    iconColor = Color(0xFF5E5CE6), // iOS Indigo
                    title = stringResource(R.string.dark_theme),
                    showDivider = false, // Единственный элемент, разделитель не нужен
                    trailingContent = {
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
                )
            }

            // Описание секции (Footer text)
            Text(
                text = "Измените оформление приложения на темное или светлое.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 24.dp, end = 16.dp)
            )

            // --- СЕКЦИЯ 2: СИСТЕМА ---
            IOSSettingsGroup(backgroundColor = sectionColor) {
                // Фоновая работа
                IOSSettingsItem(
                    icon = Icons.Default.BatteryStd,
                    iconColor = Color(0xFF34C759), // iOS Green
                    title = "Фоновая работа", // stringResource(R.string.background_mode)
                    showDivider = true, // Есть элементы ниже (если будут), пока false если последний
                    onClick = { requestIgnoreBatteryOptimization(context) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBatteryUnrestricted) "Вкл" else "Выкл",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                )

                // Версия (просто для инфо)
                IOSSettingsItem(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF007AFF), // iOS Blue
                    title = stringResource(R.string.about_app),
                    showDivider = false,
                    onClick = { navController.navigate(Screen.AboutApp.route) },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }
        }
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ IOS НАСТРОЕК ---

@Composable
fun IOSSettingsGroup(
    backgroundColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Отступ группы от краев экрана
            .clip(RoundedCornerShape(10.dp)) // Скругление углов группы
            .background(backgroundColor), // Цвет фона группы
        content = content
    )
}

@Composable
fun IOSSettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    showDivider: Boolean,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit
) {
    // Основной контейнер.
    // ВАЖНО: Убрали padding(bottom), чтобы divider был прижат к низу.
    // Оставляем только отступ слева для иконки и общий клик.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 16.dp), // Отступ только слева, чтобы иконка не липла к краю
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. ИКОНКА (Слева, всегда по центру высоты строки)
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp) // Небольшой отступ самой иконки
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. ПРАВАЯ ЧАСТЬ (Текст + Контент + Разделитель)
        // Занимает все оставшееся место
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Внутренний ряд для Текста и Элемента управления
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp) // Отступ справа от края экрана
                    .padding(vertical = 12.dp), // 🔥 ВОТ ТУТ задаем высоту строки контента
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                trailingContent()
            }

            // 3. РАЗДЕЛИТЕЛЬ
            // Он находится ВНИЗУ колонки, под текстом, но внутри блока,
            // поэтому начинается ровно от текста (Inset Divider)
            if (showDivider) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )
            }
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