package com.example.noteai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.noteai.ui.theme.NoteAITheme
import com.example.noteai.ui.navigation.Screen
import com.example.noteai.di.DependencyContainer
import com.example.noteai.presentation.home.*
import com.example.noteai.presentation.analysis.*
import com.example.noteai.presentation.import_doc.ImportScreen
import com.example.noteai.presentation.summary.SummaryScreen
import com.example.noteai.presentation.quiz.QuizScreen

import com.example.noteai.presentation.chat.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FabPosition
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
class MainActivity : ComponentActivity() {

    private lateinit var dependencyContainer: DependencyContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dependencyContainer = DependencyContainer(applicationContext)
        enableEdgeToEdge()
        setContent {
            NoteAITheme {
                val navController = rememberNavController()
                val chatViewModel: ChatViewModel = viewModel(
                    factory = ChatViewModelFactory(dependencyContainer.chatRepository)
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "chat") {
                            composable("home") {
                                val homeViewModel: HomeViewModel = viewModel(
                                    factory = HomeViewModelFactory(dependencyContainer.courseRepository)
                                )
                                val homeState by homeViewModel.state.collectAsState()
                                HomeScreen(
                                    state = homeState,
                                    onAddCourse = { navController.navigate(Screen.ImportDoc.route) },
                                    onCourseClick = { course -> navController.navigate(Screen.Summary.createRoute(course.id)) },
                                    onDeleteCourse = { course -> homeViewModel.deleteCourse(course) }
                                )
                            }
                            composable(Screen.ImportDoc.route) {
                                ImportScreen(
                                    onCourseCreated = { courseId ->
                                        navController.navigate(Screen.Summary.createRoute(courseId)) {
                                            popUpTo("home")
                                        }
                                    },
                                    onBack = { navController.popBackStack() },
                                    saveCourse = { course ->
                                        dependencyContainer.courseRepository.saveCourse(course)
                                    }
                                )
                            }
                            composable("chat") {
                                val state by chatViewModel.state.collectAsState()
                                ChatScreen(
                                    state = state,
                                    onSendMessage = { content, attachment -> chatViewModel.sendMessage(content, attachment) },
                                    onRegenerate = { chatViewModel.regenerateLastResponse() },
                                    onNewConversation = { chatViewModel.startNewConversation() },
                                    onSelectConversation = { chatViewModel.selectConversation(it) }
                                )
                            }

                            composable(Screen.Summary.route) { backStackEntry ->
                                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                                var summary by remember { mutableStateOf<com.example.noteai.domain.model.CourseSummary?>(null) }
                                LaunchedEffect(courseId) {
                                    summary = dependencyContainer.courseRepository.getSummaryForCourse(courseId)
                                }
                                SummaryScreen(summary = summary, onBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}

class ChatViewModelFactory(private val repository: com.example.noteai.domain.repository.ChatRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ChatViewModel(repository) as T
}

class HomeViewModelFactory(private val repository: com.example.noteai.domain.repository.CourseRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository) as T
}

class AnalysisViewModelFactory(
    private val courseId: String,
    private val courseRepository: com.example.noteai.domain.repository.CourseRepository,
    private val aiRepository: com.example.noteai.domain.repository.AIRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AnalysisViewModel(courseId, courseRepository, aiRepository) as T
}
