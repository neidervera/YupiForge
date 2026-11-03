package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.repository.ProjectRepository
import com.example.ui.EditorScreen
import com.example.ui.EditorViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    MAIN,
    EDITOR
}

class MainActivity : ComponentActivity() {

    private val repository by lazy { ProjectRepository(applicationContext) }
    private val editorViewModel by lazy { EditorViewModel(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.MAIN) }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        ScreenState.MAIN -> {
                            MainScreen(
                                repository = repository,
                                onOpenProject = { project ->
                                    editorViewModel.loadProject(project)
                                    currentScreen = ScreenState.EDITOR
                                },
                                onCreateNewProject = { name, width, height, initialBitmap ->
                                    editorViewModel.createNewProject(name, width, height, initialBitmap)
                                    currentScreen = ScreenState.EDITOR
                                }
                            )
                        }
                        ScreenState.EDITOR -> {
                            EditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = {
                                    repository.refreshProjects()
                                    repository.refreshExportedGallery()
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
