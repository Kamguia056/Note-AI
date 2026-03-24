package com.example.noteai.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteai.domain.model.*
import com.example.noteai.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AnalysisState(
    val course: Course? = null,
    val summary: CourseSummary? = null,
    val quiz: Quiz? = null,
    val isGenerating: Boolean = false,
    val error: String? = null
)

class AnalysisViewModel(
    private val courseId: String,
    private val courseRepository: CourseRepository,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val course = courseRepository.getCourseById(courseId)
            if (course == null) {
                _state.update { it.copy(error = "Cours non trouvé") }
                return@launch
            }
            _state.update { it.copy(course = course) }

            val summary = courseRepository.getSummaryForCourse(courseId)
            val quiz = courseRepository.getQuizForCourse(courseId)

            if (summary == null || quiz == null) {
                generateContent(course.content)
            } else {
                _state.update { it.copy(summary = summary, quiz = quiz) }
            }
        }
    }

    private fun generateContent(content: String) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true) }
            try {
                val summary = aiRepository.generateSummary(content).copy(courseId = courseId)
                val quiz = aiRepository.generateQuiz(content).copy(courseId = courseId)
                
                courseRepository.saveSummary(summary)
                courseRepository.saveQuiz(quiz)
                
                _state.update { it.copy(summary = summary, quiz = quiz, isGenerating = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Erreur lors de la génération : ${e.message}", isGenerating = false) }
            }
        }
    }
}
