package ru.plumsoftware.notepad

import android.annotation.SuppressLint
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
        setContent {
            NotepadTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.NoteList.route) {
                    composable(Screen.NoteList.route) {
                        val viewModel: NoteViewModel = viewModel(
                            factory = NoteViewModelFactory(application)
                        )
                        NoteListScreen(navController, viewModel)
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
