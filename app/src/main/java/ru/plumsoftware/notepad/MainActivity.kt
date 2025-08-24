package ru.plumsoftware.notepad

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
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

class MainActivity : ComponentActivity() {
    private var showOpenAdsCounter = 0
    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MobileAds.initialize(baseContext) {}

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
            NotepadTheme {
                val navController = rememberNavController()
                val noteId = intent.getStringExtra("noteId")
                var showPermissionRationale by remember { mutableStateOf<String?>(null) }

                // Request initial permissions
                val requestPermissions =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                        permissions.entries.forEach { (permission, granted) ->
                            if (!granted && shouldShowRequestPermissionRationale(permission)) {
                                showPermissionRationale = permission
                                showOpenAdsCounter ++
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
        val adRequestConfiguration = AdRequestConfiguration.Builder(AdsConfig.HuaweiAppGalleryAds().openAdsId).build()

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
}
