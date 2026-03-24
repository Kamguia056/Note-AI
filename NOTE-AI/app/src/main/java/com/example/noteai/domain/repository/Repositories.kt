package com.example.noteai.domain.repository

import com.example.noteai.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun getAllCourses(): Flow<List<Course>>
    suspend fun getCourseById(id: String): Course?
    suspend fun saveCourse(course: Course)
    suspend fun getSummaryForCourse(courseId: String): CourseSummary?
    suspend fun saveSummary(summary: CourseSummary)
    suspend fun getQuizForCourse(courseId: String): Quiz?
    suspend fun saveQuiz(quiz: Quiz)
    suspend fun deleteCourse(course: Course)
}

interface AIRepository {
    suspend fun generateSummary(content: String): CourseSummary
    suspend fun generateQuiz(content: String): Quiz
    suspend fun extractTextFromImage(imageUri: String): String
}

interface ChatRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun deleteMessage(messageId: String)
    suspend fun createConversation(title: String): String
    suspend fun getChatResponse(prompt: String, conversationId: String, attachmentUri: String? = null): String
}
