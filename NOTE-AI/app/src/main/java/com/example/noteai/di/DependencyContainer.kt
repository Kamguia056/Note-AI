package com.example.noteai.di

import android.content.Context
import androidx.room.*
import com.example.noteai.data.local.NoteDatabase
import com.example.noteai.data.local.dao.CourseDao
import com.example.noteai.data.mapper.Mapper
import com.example.noteai.data.repository.*
import com.example.noteai.domain.repository.*
import com.google.gson.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DependencyContainer(context: Context) {
    
    private val database: NoteDatabase by lazy {
        Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            "note_ai_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    private val gson = Gson()
    private val mapper = Mapper(gson)
    
    private val okHttpClient by lazy {
        okhttp3.OkHttpClient.Builder()
            .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val geminiService = retrofit.create(com.example.noteai.data.remote.GeminiService::class.java)
    
    // REMPLACEZ PAR VOTRE CLÉ API GEMINI REELLE
    private val geminiApiKey = "AIzaSyB8-RPPwVfHE0t1h8w0WyqJeBV6TC_zQDI"

    val courseRepository: CourseRepository by lazy {
        CourseRepositoryImpl(database.courseDao, mapper)
    }

    val aiRepository: AIRepository by lazy {
        MockAIRepositoryImpl()
    }
    
    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(context, database.chatDao, mapper, geminiService, geminiApiKey)
    }
}
