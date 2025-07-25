package ru.plumsoftware.notepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.plumsoftware.notepad.ui.NoteViewModelFactory
import ru.plumsoftware.notepad.ui.Screen
import ru.plumsoftware.notepad.ui.addnote.AddNoteScreen
import ru.plumsoftware.notepad.ui.notes.NoteListScreen
import ru.plumsoftware.notepad.ui.theme.NotepadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotepadTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.NoteList.route) {
                    composable(Screen.NoteList.route) {
                        NoteListScreen(
                            navController = navController,
                            viewModel = viewModel(
                                factory = NoteViewModelFactory(application)
                            )
                        )
                    }
                    composable(Screen.AddNote.route) {
                        AddNoteScreen(
                            navController = navController,
                            viewModel = viewModel(
                                factory = NoteViewModelFactory(application)
                            )
                        )
                    }
                }
            }
        }
    }
}
