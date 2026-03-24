package com.example.noteai.domain.model

import java.util.Date

data class Course(
    val id: String,
    val title: String,
    val content: String,
    val originalUri: String?,
    val timestamp: Long = System.currentTimeMillis()
)

data class CourseSummary(
    val id: String,
    val courseId: String,
    val briefSummary: String,
    val revisionSheet: String,
    val keyPoints: List<String>,
    val definitions: List<Definition>,
    val formulas: List<String>
)

data class Definition(
    val term: String,
    val explanation: String
)

data class Quiz(
    val id: String,
    val courseId: String,
    val questions: List<Question>
)

data class Question(
    val id: String,
    val text: String,
    val type: QuestionType,
    val options: List<String>? = null, // For QCM
    val correctAnswer: String,
    val explanation: String? = null
)

enum class QuestionType {
    QCM, OPEN_ENDED, FLASHCARD
}

data class Conversation(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val content: String,
    val role: MessageRole,
    val attachmentUri: String? = null,
    val attachmentType: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER, AI
}
