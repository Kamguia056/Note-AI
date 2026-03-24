package com.example.noteai.data.repository

import com.example.noteai.data.local.dao.CourseDao
import com.example.noteai.data.mapper.Mapper
import com.example.noteai.domain.model.*
import com.example.noteai.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepositoryImpl(
    private val dao: CourseDao,
    private val mapper: Mapper
) : CourseRepository {

    override fun getAllCourses(): Flow<List<Course>> {
        return dao.getAllCourses().map { entities ->
            entities.map { mapper.toCourse(it) }
        }
    }

    override suspend fun getCourseById(id: String): Course? {
        return dao.getCourseById(id)?.let { mapper.toCourse(it) }
    }

    override suspend fun saveCourse(course: Course) {
        dao.insertCourse(mapper.toCourseEntity(course))
    }

    override suspend fun getSummaryForCourse(courseId: String): CourseSummary? {
        return dao.getSummaryForCourse(courseId)?.let { mapper.toCourseSummary(it) }
    }

    override suspend fun saveSummary(summary: CourseSummary) {
        dao.insertSummary(mapper.toSummaryEntity(summary))
    }

    override suspend fun getQuizForCourse(courseId: String): Quiz? {
        return dao.getQuizForCourse(courseId)?.let { mapper.toQuiz(it) }
    }

    override suspend fun saveQuiz(quiz: Quiz) {
        dao.insertQuiz(mapper.toQuizEntity(quiz))
    }

    override suspend fun deleteCourse(course: Course) {
        dao.deleteCourse(mapper.toCourseEntity(course))
    }
}
