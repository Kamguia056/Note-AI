package com.example.noteai.presentation.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.example.noteai.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quiz: Quiz?,
    onBack: () -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz de Révision") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (quiz == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (showResult) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Terminé !", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("Votre score : $score / ${quiz.questions.size}", fontSize = 24.sp)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = onBack) {
                        Text("Retour aux cours")
                    }
                }
            } else {
                val question = quiz.questions[currentQuestionIndex]
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    LinearProgressIndicator(
                        progress = (currentQuestionIndex + 1).toFloat() / quiz.questions.size,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))
                    Text("Question ${currentQuestionIndex + 1} / ${quiz.questions.size}", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(question.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    
                    Spacer(Modifier.height(32.dp))
                    
                    question.options?.forEach { option ->
                        OutlinedButton(
                            onClick = {
                                if (option == question.correctAnswer) {
                                    score++
                                }
                                if (currentQuestionIndex < quiz.questions.size - 1) {
                                    currentQuestionIndex++
                                } else {
                                    showResult = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(option)
                        }
                    }
                    
                    if (question.type == QuestionType.OPEN_ENDED || question.options == null) {
                        Text("Ceci est une question ouverte. Préparez votre réponse mentalement.", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (currentQuestionIndex < quiz.questions.size - 1) {
                                    currentQuestionIndex++
                                } else {
                                    showResult = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Question suivante")
                        }
                    }
                }
            }
        }
    }
}
