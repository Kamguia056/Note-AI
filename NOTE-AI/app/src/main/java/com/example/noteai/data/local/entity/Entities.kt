package com.example.noteai.data.local.entity

import androidx.room.*
import com.example.noteai.domain.model.*

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val originalUri: String?,
    val timestamp: Long
)

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val briefSummary: String,
    val revisionSheet: String,
    val keyPoints: String, // Stored as comma separated or JSON string
    val definitions: String, // Stored as JSON string
    val formulas: String // Stored as JSON string
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val questions: String // Stored as JSON string
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val role: String,
    val attachmentUri: String?,
    val attachmentType: String?,
    val timestamp: Long
)
