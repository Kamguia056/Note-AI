package com.example.noteai.data.local

import androidx.room.*
import com.example.noteai.data.local.dao.CourseDao
import com.example.noteai.data.local.entity.*

@Database(
    entities = [
        CourseEntity::class, 
        SummaryEntity::class, 
        QuizEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ], 
    version = 2
)
abstract class NoteDatabase : RoomDatabase() {
    abstract val courseDao: CourseDao
    abstract val chatDao: com.example.noteai.data.local.dao.ChatDao
}
