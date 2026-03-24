package com.example.noteai.data.mapper

import com.example.noteai.data.local.entity.*
import com.example.noteai.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Mapper(private val gson: Gson) {

    fun toCourse(entity: CourseEntity): Course {
        return Course(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            originalUri = entity.originalUri,
            timestamp = entity.timestamp
        )
    }

    fun toCourseEntity(domain: Course): CourseEntity {
        return CourseEntity(
            id = domain.id,
            title = domain.title,
            content = domain.content,
            originalUri = domain.originalUri,
            timestamp = domain.timestamp
        )
    }

    fun toCourseSummary(entity: SummaryEntity): CourseSummary {
        val keyPointsType = object : TypeToken<List<String>>() {}.type
        val definitionsType = object : TypeToken<List<Definition>>() {}.type
        val formulasType = object : TypeToken<List<String>>() {}.type

        return CourseSummary(
            id = entity.id,
            courseId = entity.courseId,
            briefSummary = entity.briefSummary,
            revisionSheet = entity.revisionSheet,
            keyPoints = gson.fromJson(entity.keyPoints, keyPointsType),
            definitions = gson.fromJson(entity.definitions, definitionsType),
            formulas = gson.fromJson(entity.formulas, formulasType)
        )
    }

    fun toSummaryEntity(domain: CourseSummary): SummaryEntity {
        return SummaryEntity(
            id = domain.id,
            courseId = domain.courseId,
            briefSummary = domain.briefSummary,
            revisionSheet = domain.revisionSheet,
            keyPoints = gson.toJson(domain.keyPoints),
            definitions = gson.toJson(domain.definitions),
            formulas = gson.toJson(domain.formulas)
        )
    }

    fun toQuiz(entity: QuizEntity): Quiz {
        val questionsType = object : TypeToken<List<Question>>() {}.type
        return Quiz(
            id = entity.id,
            courseId = entity.courseId,
            questions = gson.fromJson(entity.questions, questionsType)
        )
    }

    fun toQuizEntity(domain: Quiz): QuizEntity {
        return QuizEntity(
            id = domain.id,
            courseId = domain.courseId,
            questions = gson.toJson(domain.questions)
        )
    }

    fun toConversation(entity: ConversationEntity): Conversation {
        return Conversation(
            id = entity.id,
            title = entity.title,
            lastMessage = entity.lastMessage,
            timestamp = entity.timestamp
        )
    }

    fun toConversationEntity(domain: Conversation): ConversationEntity {
        return ConversationEntity(
            id = domain.id,
            title = domain.title,
            lastMessage = domain.lastMessage,
            timestamp = domain.timestamp
        )
    }

    fun toChatMessage(entity: MessageEntity): ChatMessage {
        return ChatMessage(
            id = entity.id,
            conversationId = entity.conversationId,
            content = entity.content,
            role = MessageRole.valueOf(entity.role),
            attachmentUri = entity.attachmentUri,
            attachmentType = entity.attachmentType,
            timestamp = entity.timestamp
        )
    }

    fun toMessageEntity(domain: ChatMessage): MessageEntity {
        return MessageEntity(
            id = domain.id,
            conversationId = domain.conversationId,
            content = domain.content,
            role = domain.role.name,
            attachmentUri = domain.attachmentUri,
            attachmentType = domain.attachmentType,
            timestamp = domain.timestamp
        )
    }
}
