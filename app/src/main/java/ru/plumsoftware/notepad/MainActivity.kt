package ru.plumsoftware.notepad

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.NoteViewModelFactory
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.addnote.AddNoteScreen
import ru.plumsoftware.notepad.ui.notes.NoteListScreen
import ru.plumsoftware.notepad.ui.theme.NotepadTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                        AddNoteScreen(navController, viewModel)
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
                            AddNoteScreen(navController, viewModel, note)
                        }
                    }
                }
            }
        }
    }
}
