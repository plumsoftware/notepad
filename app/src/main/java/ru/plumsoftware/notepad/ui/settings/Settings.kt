package ru.plumsoftware.notepad.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import ru.plumsoftware.notepad.data.theme_saver.ThemeState
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.theme_saver.saveDarkThemePreference
import androidx.core.net.toUri
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.elements.IOSPinInputScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    activity: Activity,
    navController: NavController,
    themeState: ThemeState,
    viewModel: NoteViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Logic
    var isBatteryUnrestricted by remember { mutableStateOf(checkBatteryOptimization(context)) }

    // Проверка статуса уведомлений (Android 13+)
    var areNotificationsEnabled by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryUnrestricted = checkBatteryOptimization(context)
                // Обновляем статус уведомлений при возврате
                areNotificationsEnabled =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        NotificationManagerCompat.from(context).areNotificationsEnabled()
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Состояния для диалогов безопасности
    var showPinCreateScreen by remember { mutableStateOf(false) } // Для создания/смены
    var showPinConfirmScreen by remember { mutableStateOf(false) } // Для подтверждения
    var showOldPinScreen by remember { mutableStateOf(false) } // Ввод старого при смене
    var tempPin by remember { mutableStateOf("") }

    var showResetDialog by remember { mutableStateOf(false) } // Сброс

    // Проверяем, есть ли уже пароль
    val isPinSet = viewModel.isPinSet()

    // Логика цвета iOS
    val backgroundColor = if (themeState.isDarkTheme) Color.Black else Color(0xFFF2F2F7)
    val sectionColor = if (themeState.isDarkTheme) Color(0xFF1C1C1E) else Color.White

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = backgroundColor),
                title = {
                    Text(
                        stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { navController.navigateUp() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stringResource(R.string.back_button),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(top = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- СЕКЦИЯ 1: ВНЕШНИЙ ВИД ---
            IOSSettingsGroup(backgroundColor = sectionColor) {
                IOSSettingsItem(
                    icon = Icons.Default.DarkMode,
                    iconColor = Color(0xFF5E5CE6),
                    title = stringResource(R.string.dark_theme),
                    showDivider = false,
                    trailingContent = {
                        IOSSwitch(
                            checked = themeState.isDarkTheme,
                            onCheckedChange = { checked ->
                                themeState.isDarkTheme = checked
                                saveDarkThemePreference(checked, context)
                                if (checked) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            }
                        )
                    }
                )
            }
            // Подпись темы
            Text(
                text = "Измените оформление приложения на темное или светлое.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 24.dp, end = 16.dp)
            )

            // --- СЕКЦИЯ 2: КОНФИДЕНЦИАЛЬНОСТЬ (БЕЗОПАСНОСТЬ) ---
            IOSSettingsGroup(backgroundColor = sectionColor) {
                // Установить / Сменить пароль
                IOSSettingsItem(
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFFFF9500), // iOS Orange
                    title = if (isPinSet) "Сменить код-пароль" else "Включить код-пароль",
                    showDivider = true,
                    onClick = {
                        if (isPinSet) {
                            showOldPinScreen = true // Сначала старый
                        } else {
                            showPinCreateScreen = true // Сразу новый
                        }
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )

                // Восстановить (отправить email)
                /* // Этот функционал лучше реализовать через нативный почтовик
                IOSSettingsItem(
                    icon = Icons.Default.Email,
                    iconColor = Color(0xFF32ADE6), // iOS Teal
                    title = "Забыли код?",
                    showDivider = isPinSet,
                    onClick = {
                        sendRecoveryEmail(context) // Функция ниже
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
                    }
                )
                */

                // Сброс (Показывать, только если есть пароль)
                if (isPinSet) {
                    IOSSettingsItem(
                        icon = Icons.Default.DeleteForever,
                        iconColor = Color(0xFFFF3B30), // iOS Red
                        title = "Сбросить код-пароль",
                        showDivider = false,
                        onClick = { showResetDialog = true },
                        trailingContent = {}
                    )
                }
            }
            Text(
                text = "Код-пароль используется для доступа к папке «Скрытые». Если вы забудете код, данные будут утеряны, если вы не настроили восстановление.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 24.dp, end = 16.dp)
            )

            // --- СЕКЦИЯ 3: СИСТЕМА ---
            IOSSettingsGroup(backgroundColor = sectionColor) {
                // Уведомления
                IOSSettingsItem(
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFFFF2D55), // iOS Pink
                    title = "Уведомления",
                    showDivider = true,
                    onClick = { openNotificationSettings(context) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (areNotificationsEnabled) "Вкл" else "Выкл",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                )

                // Оптимизация батареи
                IOSSettingsItem(
                    icon = Icons.Default.BatteryStd,
                    iconColor = Color(0xFF34C759), // iOS Green
                    title = "Фоновая работа",
                    showDivider = true,
                    onClick = { requestIgnoreBatteryOptimization(context) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBatteryUnrestricted) "Разрешено" else "Ограничено", // Для краткости
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                )

                // О приложении
                IOSSettingsItem(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF007AFF), // iOS Blue
                    title = stringResource(R.string.about_app),
                    showDivider = false,
                    onClick = { navController.navigate(Screen.AboutApp.route) },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }
        }
    }

    // --- ЛОГИКА ЭКРАНОВ ПАРОЛЯ (Переиспользование компонента IOSPinInput) ---

    // 1. Создание нового пароля
    if (showPinCreateScreen) {
        IOSPinInputScreen(
            title = "Придумайте новый код",
            onPinEntered = { pin ->
                tempPin = pin
                showPinCreateScreen = false
                showPinConfirmScreen = true // Переход к подтверждению
            },
            onCancel = { showPinCreateScreen = false }
        )
    }

    // 2. Подтверждение пароля
    if (showPinConfirmScreen) {
        var isError by remember { mutableStateOf(false) }
        IOSPinInputScreen(
            title = "Повторите новый код",
            onPinEntered = { pin ->
                if (pin == tempPin) {
                    viewModel.savePin(pin) // Сохраняем в VM
                    showPinConfirmScreen = false
                } else {
                    isError = true // Тряска
                }
            },
            onCancel = { showPinConfirmScreen = false; showPinCreateScreen = true },
            isError = isError
        )
    }

    // 3. Ввод СТАРОГО пароля (для смены)
    if (showOldPinScreen) {
        var isError by remember { mutableStateOf(false) }
        IOSPinInputScreen(
            title = "Введите старый код",
            onPinEntered = { pin ->
                if (viewModel.checkPin(pin)) {
                    showOldPinScreen = false
                    showPinCreateScreen = true // Старый верный -> создаем новый
                } else {
                    isError = true
                }
            },
            onCancel = { showOldPinScreen = false },
            isError = isError
        )
    }

    // 4. Диалог СБРОСА
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = sectionColor,
            title = { Text("Сброс код-пароля") },
            text = { Text("Вы уверены? Защита с папки «Скрытые» будет снята, и пароль будет удален.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPin()
                    showResetDialog = false
                }) {
                    Text("Сбросить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Отмена") }
            }
        )
    }
}

// Вспомогательная функция: Открыть настройки уведомлений приложения
fun openNotificationSettings(context: Context) {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = "package:${context.packageName}".toUri()
    }
    context.startActivity(intent)
}

// Вспомогательная: Отправить письмо для восстановления (Опционально)
fun sendRecoveryEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com")) // Твой email
        putExtra(Intent.EXTRA_SUBJECT, "Восстановление пароля Notepad")
        putExtra(Intent.EXTRA_TEXT, "Здравствуйте, я забыл код-пароль от скрытой папки...")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Нет почтового клиента
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