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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
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
import ru.plumsoftware.notepad.ui.elements.ScreenHeaderTitle
import androidx.compose.foundation.layout.statusBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navController: NavController,
    themeState: ThemeState,
    viewModel: NoteViewModel
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ScreenHeaderTitle(
            title = stringResource(R.string.settings),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 8.dp)
        )
        SettingsContent(
            navController = navController,
            themeState = themeState,
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    navController: NavController,
    themeState: ThemeState,
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- STATES: РАЗРЕШЕНИЯ И СИСТЕМА ---
    var isBatteryUnrestricted by remember { mutableStateOf(checkBatteryOptimization(context)) }

    var areNotificationsEnabled by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    var hasStoragePermission by remember {
        mutableStateOf(checkStoragePermission(context))
    }

    // Обновление статусов при возврате на экран (например, из системных настроек)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryUnrestricted = checkBatteryOptimization(context)
                areNotificationsEnabled = checkNotificationPermission(context)
                hasMicPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                hasCameraPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                hasStoragePermission = checkStoragePermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- LAUNCHERS: ЗАПРОС РАЗРЕШЕНИЙ В ПРИЛОЖЕНИИ ---
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    val storageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        hasStoragePermission = checkStoragePermission(context)
        if (!hasStoragePermission) {
            // Если после запроса доступ всё ещё не выдан (например, окно было заблокировано системой)
            Toast.makeText(context, "Дайте разрешение на фото вручную в настройках", Toast.LENGTH_LONG).show()
            openAppSettings(context) // Перекидываем в настройки
        }
    }

    // Состояния для диалогов безопасности
    var showPinCreateScreen by remember { mutableStateOf(false) }
    var showPinConfirmScreen by remember { mutableStateOf(false) }
    var showOldPinScreen by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    val isPinSet = viewModel.isPinSet()
    val sectionColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // --- СЕКЦИЯ 1: ВНЕШНИЙ ВИД ---
        SettingsSectionHeader("ОФОРМЛЕНИЕ")
        IOSSettingsGroup(backgroundColor = sectionColor) {
            IOSSettingsItem(
                icon = Icons.Default.DarkMode,
                iconColor = Color(0xFF5E5CE6), // iOS Indigo
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
        Text(
            text = "Измените оформление приложения на темное или светлое.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 24.dp, end = 16.dp)
        )

        // --- СЕКЦИЯ 2: КОНФИДЕНЦИАЛЬНОСТЬ (БЕЗОПАСНОСТЬ) ---
        SettingsSectionHeader("ПРИВАТНОСТЬ")
        IOSSettingsGroup(backgroundColor = sectionColor) {
            IOSSettingsItem(
                icon = Icons.Default.Lock,
                iconColor = Color(0xFFFF9500), // iOS Orange
                title = if (isPinSet) "Сменить код-пароль" else "Включить код-пароль",
                showDivider = isPinSet,
                onClick = {
                    if (isPinSet) showOldPinScreen = true else showPinCreateScreen = true
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            )

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
            text = "Код-пароль используется для доступа к папке «Скрытые». Если вы забудете код, данные будут утеряны.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 24.dp, end = 16.dp)
        )

        // --- СЕКЦИЯ 3: РАЗРЕШЕНИЯ ДОСТУПА ---
        SettingsSectionHeader("ДОСТУП")
        IOSSettingsGroup(backgroundColor = sectionColor) {
            // Уведомления
            IOSSettingsItem(
                icon = Icons.Default.Notifications,
                iconColor = Color(0xFFFF2D55), // iOS Pink
                title = "Уведомления",
                showDivider = true,
                onClick = { openNotificationSettings(context) },
                trailingContent = { PermissionStatusText(areNotificationsEnabled) }
            )

            // Микрофон
            IOSSettingsItem(
                icon = Icons.Default.Mic,
                iconColor = Color(0xFFFF3B30), // iOS Red
                title = "Микрофон",
                showDivider = true,
                onClick = {
                    if (!hasMicPermission) micLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    else openAppSettings(context)
                },
                trailingContent = { PermissionStatusText(hasMicPermission) }
            )

//            // Камера
//            IOSSettingsItem(
//                icon = Icons.Default.CameraAlt,
//                iconColor = Color(0xFF5AC8FA), // iOS Light Blue
//                title = "Камера",
//                showDivider = true,
//                onClick = {
//                    if (!hasCameraPermission) cameraLauncher.launch(android.Manifest.permission.CAMERA)
//                    else openAppSettings(context)
//                },
//                trailingContent = { PermissionStatusText(hasCameraPermission) }
//            )

            // Память / Галерея
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                IOSSettingsItem(
                    icon = Icons.Default.Folder,
                    iconColor = Color(0xFF007AFF), // iOS Blue
                    title = "Память (Фото)",
                    showDivider = true,
                    onClick = {
                        if (!hasStoragePermission) {
                            // Формируем правильный список разрешений в зависимости от версии Android
                            val perms =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    arrayOf(
                                        android.Manifest.permission.READ_MEDIA_IMAGES,
                                        android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                                    )
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
                                } else {
                                    arrayOf(
                                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )
                                }
                            storageLauncher.launch(perms)
                        } else {
                            // Если уже разрешено — просто открываем настройки приложения
                            openAppSettings(context)
                        }
                    },
                    trailingContent = { PermissionStatusText(hasStoragePermission) }
                )
            }

            // Фоновая работа (Батарея)
            IOSSettingsItem(
                icon = Icons.Default.BatteryStd,
                iconColor = Color(0xFF34C759), // iOS Green
                title = "Фоновая работа",
                showDivider = false,
                onClick = { requestIgnoreBatteryOptimization(context) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isBatteryUnrestricted) "Разрешено" else "Ограничено",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            )
        }
        Text(
            text = "Разрешения нужны для создания голосовых заметок, прикрепления фото и стабильной работы напоминаний.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 24.dp, end = 16.dp)
        )

        // --- СЕКЦИЯ 4: СИСТЕМА И ИНФОРМАЦИЯ ---
        SettingsSectionHeader("О ПРИЛОЖЕНИИ")
        IOSSettingsGroup(backgroundColor = sectionColor) {
            IOSSettingsItem(
                icon = Icons.Default.Info,
                iconColor = Color(0xFF8E8E93), // iOS Gray
                title = stringResource(R.string.about_app),
                showDivider = true,
                onClick = { navController.navigate(Screen.AboutApp.route) },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            )

            IOSSettingsItem(
                icon = Icons.Default.School,
                iconColor = Color(0xFFAC8E68), // Brown/Gold
                title = stringResource(R.string.settings_tutorial),
                showDivider = false,
                onClick = { navController.navigate("onboarding") },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // --- ЛОГИКА ЭКРАНОВ ПАРОЛЯ ---
    if (showPinCreateScreen) {
        IOSPinInputScreen(
            title = "Придумайте новый код",
            onPinEntered = { pin ->
                tempPin = pin
                showPinCreateScreen = false
                showPinConfirmScreen = true
            },
            onCancel = { showPinCreateScreen = false }
        )
    }

    if (showPinConfirmScreen) {
        var isError by remember { mutableStateOf(false) }
        IOSPinInputScreen(
            title = "Повторите новый код",
            onPinEntered = { pin ->
                if (pin == tempPin) {
                    viewModel.savePin(pin)
                    showPinConfirmScreen = false
                } else {
                    isError = true
                }
            },
            onCancel = { showPinConfirmScreen = false; showPinCreateScreen = true },
            isError = isError
        )
    }

    if (showOldPinScreen) {
        var isError by remember { mutableStateOf(false) }
        IOSPinInputScreen(
            title = "Введите старый код",
            onPinEntered = { pin ->
                if (viewModel.checkPin(pin)) {
                    showOldPinScreen = false
                    showPinCreateScreen = true
                } else {
                    isError = true
                }
            },
            onCancel = { showOldPinScreen = false },
            isError = isError
        )
    }

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

// --- ВСПОМОГАТЕЛЬНЫЕ UI КОМПОНЕНТЫ ---

@Composable
fun PermissionStatusText(isGranted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (isGranted) "Разрешено" else "Запрещено",
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

@Composable
fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 32.dp, end = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun IOSSettingsGroup(backgroundColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
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

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
                    .padding(vertical = 12.dp),
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

            if (showDivider) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ЛОГИКИ ---

fun checkStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        // Android 14 и 15: Проверяем полный доступ ИЛИ частичный доступ
        val fullAccess = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        val partialAccess = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
        fullAccess || partialAccess
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    } else {
        // Android 12 и ниже
        val read = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val write = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        read && write
    }
}

fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

// Открыть системные настройки приложения
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

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

fun checkBatteryOptimization(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

@SuppressLint("BatteryLife")
fun requestIgnoreBatteryOptimization(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    }
}

@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val animationDuration = 200
    val thumbSize = 28.dp
    val trackHeight = 32.dp
    val trackWidth = 52.dp

    val isDark = isSystemInDarkTheme()

    val uncheckedTrackColor = if (isDark) Color(0xFF363636) else Color(0xFFE9E9EA)
    val checkedTrackColor = Color(0xFF34C759)

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - 2.dp else 2.dp,
        animationSpec = tween(durationMillis = animationDuration),
        label = "thumb_animation"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) checkedTrackColor else uncheckedTrackColor,
        animationSpec = tween(durationMillis = animationDuration),
        label = "track_color_animation"
    )

    val thumbColor by animateColorAsState(
        targetValue = Color.White,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = trackColor,
                    shape = RoundedCornerShape(50)
                )
        )

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