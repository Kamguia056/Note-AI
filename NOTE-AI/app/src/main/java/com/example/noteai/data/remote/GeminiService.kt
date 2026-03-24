package com.example.noteai.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

import com.google.gson.annotations.SerializedName

// Modèles pour Gemini
data class GeminiRequest(
    @SerializedName("system_instruction") val systemInstruction: SystemInstruction? = null,
    val contents: List<Content>
)
data class SystemInstruction(val parts: List<Part>)
data class Content(
    val parts: List<Part>,
    val role: String? = null
)
data class Part(
    val text: String? = null,
    @SerializedName("inline_data") val inlineData: InlineData? = null
)
data class InlineData(
    @SerializedName("mime_type") val mimeType: String,
    val data: String
)

data class GeminiResponse(val candidates: List<Candidate>)
data class Candidate(val content: Content)

interface GeminiService {
    @POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}