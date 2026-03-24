package com.example.noteai.data.local.dao

import androidx.room.*
import com.example.noteai.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY timestamp DESC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Query("SELECT * FROM summaries WHERE courseId = :courseId")
    suspend fun getSummaryForCourse(courseId: String): SummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: SummaryEntity)

    @Query("SELECT * FROM quizzes WHERE courseId = :courseId")
    suspend fun getQuizForCourse(courseId: String): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)
    
    @Delete
    suspend fun deleteCourse(course: CourseEntity)
}
