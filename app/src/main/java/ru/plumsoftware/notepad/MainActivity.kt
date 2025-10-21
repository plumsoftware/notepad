package ru.plumsoftware.notepad

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import ru.plumsoftware.notepad.data.model.AdsConfig
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.NoteViewModelFactory
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.addnote.AddNoteScreen
import ru.plumsoftware.notepad.ui.dialog.PermissionRationaleDialog
import ru.plumsoftware.notepad.ui.notes.NoteListScreen
import ru.plumsoftware.notepad.ui.theme.NotepadTheme
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import ru.plumsoftware.notepad.data.theme_saver.ThemeState
import ru.plumsoftware.notepad.data.theme_saver.getDarkThemePreference
import ru.plumsoftware.notepad.ui.about_app.AboutAppScreen
import ru.plumsoftware.notepad.ui.settings.Settings

class MainActivity : ComponentActivity() {
    private var showOpenAdsCounter = 0

    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Загружаем настройку темы при запуске
        val isDarkTheme = getDarkThemePreference(this)

        MobileAds.initialize(baseContext) {}
        val analytics: FirebaseAnalytics = Firebase.analytics

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "note_reminder_channel",
                "Note Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for note reminder notifications"
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        setContent {
            val window = LocalActivity.current?.window
            val resources = LocalActivity.current?.resources
            val view = LocalView.current

            // Создаем состояние темы, которое можно передавать между компонентами
            val themeState = remember { ThemeState(isDarkTheme) }

            SideEffect {
                setupEdgeToEdge(window = window, resources = resources)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.windowInsetsController?.hide(android.view.WindowInsets.Type.navigationBars())
                } else {
                    @Suppress("DEPRECATION")
                    view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                }
            }

            NotepadTheme(
                darkTheme = themeState.isDarkTheme
            ) {
                val navController = rememberNavController()
                val noteId = intent.getStringExtra("noteId")
                var showPermissionRationale by remember { mutableStateOf<String?>(null) }

                // Request initial permissions
                val requestPermissions =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                        permissions.entries.forEach { (permission, granted) ->
                            if (!granted && shouldShowRequestPermissionRationale(permission)) {
                                showPermissionRationale = permission
                                showOpenAdsCounter++
                            }
                        }
                    }

                // Define permissions to request
                val permissionsToRequest = remember {
                    mutableListOf<String>().apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                            add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }.toTypedArray()
                }

                // Request permissions on start
                LaunchedEffect(Unit) {
                    requestPermissions.launch(permissionsToRequest)

                    if (showOpenAdsCounter == 0) {
                        showOpenAds()
                    }
                }

                // Permission Rationale Dialog
                showPermissionRationale?.let { permission ->
                    PermissionRationaleDialog(
                        permission = permission,
                        onConfirm = { requestPermissions.launch(arrayOf(permission)) },
                        onDismiss = { showPermissionRationale = null }
                    )
                }
                NavHost(
                    navController = navController,
                    startDestination = Screen.NoteList.route
                ) {
                    composable(Screen.NoteList.route) {
                        val viewModel: NoteViewModel = viewModel(
                            factory = NoteViewModelFactory(application)
                        )
                        NoteListScreen(navController, viewModel, scrollToNoteId = noteId)
                    }
                    composable(Screen.AddNote.route) {
                        val viewModel: NoteViewModel = viewModel(
                            factory = NoteViewModelFactory(application)
                        )
                        AddNoteScreen(this@MainActivity, navController, viewModel)
                    }
                    composable(Screen.Settings.route) {
                        Settings(
                            activity = this@MainActivity,
                            navController = navController,
                            themeState = themeState
                        )
                    }
                    composable(Screen.AboutApp.route) {
                        AboutAppScreen(navController)
                    }
                    composable(
                        route = Screen.EditNote.route,
                        arguments = listOf(navArgument("noteId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val viewModel: NoteViewModel = viewModel(
                            factory = NoteViewModelFactory(application)
                        )
                        val noteId = backStackEntry.arguments?.getString("noteId")
                        val note = viewModel.notes.value.find { it.id == noteId }
                        if (note != null) {
                            AddNoteScreen(this@MainActivity, navController, viewModel, note)
                        }
                    }
                }
            }
        }
    }

    private fun showOpenAds() {
        val appOpenLoader = AppOpenAdLoader(baseContext)
        val adRequestConfiguration =
            AdRequestConfiguration.Builder(App.adsConfig.openAdsId).build()

        val appOpenAdEventListener: AppOpenAdEventListener = object : AppOpenAdEventListener {
            override fun onAdShown() {
            }

            override fun onAdDismissed() {

            }

            override fun onAdFailedToShow(adError: AdError) {

            }

            override fun onAdClicked() {

            }

            override fun onAdImpression(impressionData: ImpressionData?) {
            }
        }

        val appOpenAdLoadListener: AppOpenAdLoadListener = object : AppOpenAdLoadListener {
            override fun onAdFailedToLoad(error: AdRequestError) {
            }

            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                appOpenAd.setAdEventListener(appOpenAdEventListener)
                appOpenAd.show(this@MainActivity)
            }
        }

        appOpenLoader.setAdLoadListener(appOpenAdLoadListener)
        appOpenLoader.loadAd(adRequestConfiguration)
    }


    private fun setupEdgeToEdge(window: Window?, resources: Resources?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val window = window

        // Определяем текущую тему (светлая/темная)
        val nightModeFlags = resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        val isDarkTheme =
            (nightModeFlags?.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES

        var systemUiVisibilityFlags = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        // Делаем статус бар и нав бар прозрачными
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Настройка цвета иконок для Android 5–10
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isDarkTheme) {
                    // СВЕТЛАЯ ТЕМА — ТЁМНЫЕ ИКОНКИ
                    systemUiVisibilityFlags =
                        systemUiVisibilityFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                // Для тёмной темы оставляем светлые иконки (по умолчанию)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!isDarkTheme) {
                    // СВЕТЛАЯ ТЕМА — ТЁМНЫЕ ИКОНКИ НАВИГАЦИИ
                    systemUiVisibilityFlags =
                        systemUiVisibilityFlags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
                // Для тёмной темы оставляем светлые иконки (по умолчанию)
            }

            @Suppress("DEPRECATION")
            window?.decorView?.systemUiVisibility = systemUiVisibilityFlags
            @Suppress("DEPRECATION")
            window?.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window?.navigationBarColor = android.graphics.Color.TRANSPARENT
        }

        // Для Android 10+ убираем затемнение под нав баром
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            window?.isNavigationBarContrastEnforced = false
        }

        // Для Android 11+ используем новый API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)

            val controller = window?.insetsController
            controller?.let {
                // Убеждаемся, что нав бар остаётся видимым
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                // Настройка цвета иконок для Android 11+
                if (!isDarkTheme) {
                    // СВЕТЛАЯ ТЕМА — ТЁМНЫЕ ИКОНКИ
                    it.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                } else {
                    // ТЁМНАЯ ТЕМА — СВЕТЛЫЕ ИКОНКИ (убираем флаги светлых иконок)
                    it.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                }
            }
        }
    }
}
