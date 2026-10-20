package ru.plumsoftware.notepad

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yandex.mobile.ads.common.YandexAds
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.NoteViewModelFactory
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.addnote.AddNoteScreen
import ru.plumsoftware.notepad.ui.dialog.PermissionRationaleDialog
import ru.plumsoftware.notepad.ui.notes.NoteListScreen
import ru.plumsoftware.notepad.ui.theme.NotepadTheme
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import ru.plumsoftware.notepad.data.theme_saver.ThemeState
import ru.plumsoftware.notepad.data.theme_saver.getDarkThemePreference
import ru.plumsoftware.notepad.ui.about_app.AboutAppScreen
import ru.plumsoftware.notepad.ui.fadeInEnter
import ru.plumsoftware.notepad.ui.fadeOutExit
import ru.plumsoftware.notepad.ui.horizontalSlideInEnter
import ru.plumsoftware.notepad.ui.horizontalSlideInExit
import ru.plumsoftware.notepad.ui.horizontalSlideOutEnter
import ru.plumsoftware.notepad.ui.horizontalSlideOutExit
import ru.plumsoftware.notepad.ui.verticalSlideInEnter
import ru.plumsoftware.notepad.ui.verticalSlideInExit
import androidx.core.content.edit
import ru.plumsoftware.notepad.ui.onboarding.OnboardingScreen
import ru.plumsoftware.notepad.ui.habit.add_habit.AddHabitScreen

class MainActivity : ComponentActivity() {

    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Включаем режим Edge-to-Edge ДО super.onCreate (и до setContent)
        // Это делает статус бар и нав бар прозрачными и расширяет окно на весь экран
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        // События от виджета и шорткатов
        val openAddNote = intent.getBooleanExtra("OPEN_ADD_NOTE", false)
            || intent.getStringExtra("action") == "new_note"
            || intent.getStringExtra("action") == "new_task"
            || intent.getStringExtra("action") == "voice_note"
        val launchAction = intent.getStringExtra("action")

        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Загружаем настройку темы при запуске
        val isDarkTheme = getDarkThemePreference(this)

        val analytics: FirebaseAnalytics = Firebase.analytics

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "note_reminder_channel_v2",
                "Note Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for note reminder notifications"
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        setContent {
            val window = LocalActivity.current?.window
            val view = LocalView.current

            // Создаем состояние темы
            val themeState = remember { ThemeState(isDarkTheme) }

            // 2. Управление цветом иконок в статус баре и навигации
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    // Получаем контроллер для управления внешним видом системных баров
                    val insetsController = WindowCompat.getInsetsController(window, view)

                    // Логика:
                    // Если тема Светлая (!isDarkTheme), иконки должны быть ТЕМНЫМИ (isAppearanceLight... = true)
                    // Если тема Темная (isDarkTheme), иконки должны быть СВЕТЛЫМИ (isAppearanceLight... = false)

                    // Управление иконками статус бара (часы, зарядка)
                    insetsController.isAppearanceLightStatusBars = !themeState.isDarkTheme

                    // Управление иконками нижнего бара навигации (кнопки назад/домой)
                    insetsController.isAppearanceLightNavigationBars = !themeState.isDarkTheme
                }
            }

            var isFirstLaunch by remember {
                mutableStateOf(sharedPreferences.getBoolean("is_first_launch", true))
            }

            NotepadTheme(
                darkTheme = themeState.isDarkTheme
            ) {
                val navController = rememberNavController()
                val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
                val navBarColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f).toArgb()
                val noteId = intent.getStringExtra("noteId")
                var showPermissionRationale by remember { mutableStateOf<String?>(null) }

                // --- Permissions Logic ---
                val requestPermissions =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                        permissions.entries.forEach { (permission, granted) ->
                            if (!granted && shouldShowRequestPermissionRationale(permission)) {
                                showPermissionRationale = permission
                            }
                        }
                    }

                val permissionsToRequest = remember {
                    mutableListOf<String>().apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        // Для Android 12 и ниже нужны разрешения на память
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }.toTypedArray()
                }

                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        val insetsController = WindowCompat.getInsetsController(window, view)

                        // Иконки
                        insetsController.isAppearanceLightStatusBars = !themeState.isDarkTheme
                        insetsController.isAppearanceLightNavigationBars = !themeState.isDarkTheme

                        // Цвет статус бара (обычно прозрачный для Edge-to-Edge)
                        window.statusBarColor = android.graphics.Color.TRANSPARENT

                        // 🔥 ЦВЕТ НАВИГАЦИИ 🔥
                        // Если ты хочешь цвет Surface:
                        window.navigationBarColor = navBarColor

                        // НО! Если ты включил enableEdgeToEdge(), Android 10+ (Q) и выше
                        // могут игнорировать этот цвет и делать бар прозрачным/полупрозрачным.
                        // Для Android 15 (V) это вообще дефолт.
                        // Чтобы вернуть непрозрачность, нужно отключить enforce contrast (для API 29+).
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            window.isNavigationBarContrastEnforced = false
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    if (permissionsToRequest.isNotEmpty()) {
                        requestPermissions.launch(permissionsToRequest)
                    }

                    // SDK нужен для нативной и межстраничной рекламы; реклама при входе отключена
                    YandexAds.initialize(baseContext) {}
                }

                showPermissionRationale?.let { permission ->
                    PermissionRationaleDialog(
                        permission = permission,
                        onConfirm = { requestPermissions.launch(arrayOf(permission)) },
                        onDismiss = { showPermissionRationale = null }
                    )
                }

                // --- Navigation Host ---
                if (isFirstLaunch) {
                    // ПОКАЗЫВАЕМ ОБУЧЕНИЕ
                    OnboardingScreen(
                        onFinished = {
                            // Сохраняем, что обучение пройдено
                            sharedPreferences.edit {putBoolean("is_first_launch", false)}
                            isFirstLaunch = false // Переключаем стейт -> покажется NavHost
                        }
                    )
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.NoteList.route,
                        enterTransition = { horizontalSlideInEnter() },
                        exitTransition = { horizontalSlideInExit() },
                        popEnterTransition = { horizontalSlideOutEnter() },
                        popExitTransition = { horizontalSlideOutExit() }
                    ) {
                        composable(Screen.NoteList.route) {
                            val viewModel: NoteViewModel = viewModel(
                                factory = NoteViewModelFactory(application, openAddNote)
                            )
                            LaunchedEffect(launchAction) {
                                when (launchAction) {
                                    "new_note", "new_task", "voice_note" ->
                                        navController.navigate(Screen.AddNote.route)
                                    else -> Unit
                                }
                            }
                            NoteListScreen(
                                navController,
                                viewModel,
                                scrollToNoteId = noteId,
                                themeState = themeState,
                                focusSearchOnStart = launchAction == "search"
                            )
                        }

                        composable(
                            Screen.AddNote.route,
                            enterTransition = { verticalSlideInEnter() },
                            exitTransition = { fadeOutExit() },
                            popEnterTransition = { fadeInEnter() },
                            popExitTransition = { verticalSlideInExit() }
                        ) {
                            val viewModel: NoteViewModel = viewModel(
                                factory = NoteViewModelFactory(application, openAddNote)
                            )
                            AddNoteScreen(this@MainActivity, navController, viewModel)
                        }

                        composable(Screen.AboutApp.route) {
                            AboutAppScreen(navController)
                        }

                        composable(Screen.Trash.route) {
                            val viewModel: NoteViewModel = viewModel(
                                factory = NoteViewModelFactory(application, false)
                            )
                            ru.plumsoftware.notepad.ui.trash.TrashScreen(navController, viewModel)
                        }

                        composable(Screen.Settings.route) {
                            LaunchedEffect(Unit) {
                                navController.popBackStack(Screen.NoteList.route, inclusive = false)
                            }
                        }

                        composable(
                            route = Screen.EditNote.route,
                            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val viewModel: NoteViewModel = viewModel(
                                factory = NoteViewModelFactory(application, openAddNote)
                            )
                            val noteId = backStackEntry.arguments?.getString("noteId")

                            if (noteId != null) {
                                val note by viewModel.getNoteById(noteId)
                                    .collectAsState(initial = null)
                                if (note != null) {
                                    AddNoteScreen(this@MainActivity, navController, viewModel, note)
                                } else {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }

                        composable(
                            route = Screen.AddHabit.route,
                            enterTransition = { verticalSlideInEnter() },
                            exitTransition = { fadeOutExit() },
                            popEnterTransition = { fadeInEnter() },
                            popExitTransition = { verticalSlideInExit() }
                        ) {
                            val viewModel: NoteViewModel = viewModel(
                                factory = NoteViewModelFactory(
                                    application,
                                    false
                                ) // openAddNote false тут
                            )

                            // Вызываем экран создания привычки (код ниже)
                            AddHabitScreen(
                                activity = this@MainActivity,
                                navController = navController,
                                themeState = themeState,
                                viewModel = viewModel
                            )
                        }

                        composable(
                            route = "edit_habit/{habitId}", // Или Screen.EditHabit.route
                            arguments = listOf(navArgument("habitId") {
                                type = NavType.StringType
                            }),
                            enterTransition = { verticalSlideInEnter() }, // Тоже снизу вверх
                            exitTransition = { fadeOutExit() },
                            popEnterTransition = { fadeInEnter() },
                            popExitTransition = { verticalSlideInExit() }
                        ) { backStackEntry ->
                            val viewModel: NoteViewModel =
                                viewModel(factory = NoteViewModelFactory(application, false))
                            val habitId = backStackEntry.arguments?.getString("habitId")

                            AddHabitScreen(
                                activity = this@MainActivity,
                                navController = navController,
                                viewModel = viewModel,
                                themeState = themeState,
                                habitId = habitId
                            )
                        }

                        composable("onboarding") {
                            OnboardingScreen(onFinished = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
