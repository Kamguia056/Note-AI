package com.example.noteai.data.repository

import com.example.noteai.data.local.dao.ChatDao
import com.example.noteai.data.local.entity.ConversationEntity
import com.example.noteai.data.mapper.Mapper
import com.example.noteai.data.remote.*
import com.example.noteai.domain.model.*
import com.example.noteai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.*
import java.util.UUID
import android.content.Context
import retrofit2.HttpException

class ChatRepositoryImpl(
    private val context: Context,
    private val chatDao: ChatDao,
    private val mapper: Mapper,
    private val geminiService: GeminiService,
    private val apiKey: String
) : ChatRepository {

    override fun getAllConversations(): Flow<List<Conversation>> {
        return chatDao.getAllConversations().map { entities ->
            entities.map { mapper.toConversation(it) }
        }
    }

    override fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversation(conversationId).map { entities ->
            entities.map { mapper.toChatMessage(it) }
        }
    }

    override suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(mapper.toMessageEntity(message))
        
        // Update last message in conversation
        val conversationList = chatDao.getAllConversations().first()
        val conversation = conversationList.find { it.id == message.conversationId }
        conversation?.let {
            chatDao.updateConversation(it.copy(lastMessage = message.content, timestamp = System.currentTimeMillis()))
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }
    
    override suspend fun createConversation(title: String): String {
        val id = UUID.randomUUID().toString()
        val entity = ConversationEntity(
            id = id,
            title = title,
            lastMessage = "",
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertConversation(entity)
        return id
    }

    private fun getInlineData(uriString: String?): InlineData? {
        if (uriString == null) return null
        return try {
            val uri = android.net.Uri.parse(uriString)
            val contentResolver = context.contentResolver
            var mimeType = contentResolver.getType(uri)
            if (mimeType == null && uriString.lowercase().endsWith(".m4a")) {
                mimeType = "audio/mp4"
            }
            if (mimeType == null) mimeType = "application/octet-stream"
            val bytes = contentResolver.openInputStream(uri)?.readBytes()
            if (bytes != null) {
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                InlineData(mimeType, base64)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getChatResponse(prompt: String, conversationId: String, attachmentUri: String?): String {
        return try {
            val historyEntities = chatDao.getMessagesForConversation(conversationId).first()
            val messages = historyEntities.map { mapper.toChatMessage(it) }.sortedBy { it.timestamp }

            val contentsList = messages.map { msg ->
                val inlineData = getInlineData(msg.attachmentUri)
                val parts = mutableListOf<Part>(Part(text = msg.content))
                inlineData?.let { parts.add(Part(inlineData = it)) }
                val roleStr = if (msg.role == MessageRole.USER) "user" else "model"
                Content(parts = parts, role = roleStr)
            }

            // Note: Le message actuel (prompt + attachment) est déjà dans historyEntities grâce au saveMessage appelé juste avant dans le ViewModel.

            val response = geminiService.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    systemInstruction = SystemInstruction(
                        parts = listOf(Part(text = "Tu es NoteAI, une intelligence artificielle experte en soutien scolaire et pédagogique. Ton but est de faciliter les études de l'utilisateur. Règle absolue : NE TE PRÉSENTE PAS à chaque début de réponse. Rentre IMMÉDIATEMENT dans le vif du sujet et réponds directement à la question posée sans formule d'introduction ('Bonjour je suis NoteAI', etc est interdit). Tu ne dois décliner ton identité (en tant que NoteAI et non Gemini) QUE si l'utilisateur te demande explicitement 'qui es-tu ?'. Sois toujours précis, pédagogue et structuré."))
                    ),
                    contents = contentsList
                )
            )
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Désolé, je n'ai pas pu générer de réponse."
        } catch (e: java.io.IOException) {
            "Problème de connexion 📶"
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> "Erreur 🔑 : Votre clé API est invalide."
                403 -> "Erreur 🚫 : Accès refusé (Vérifiez les restrictions de votre clé)."
                429 -> "Erreur ⏳ : Trop de requêtes (Quota atteint). Réessayez dans un instant."
                500, 503 -> "Erreur ☁️ : Le serveur Gemini est temporairement indisponible."
                else -> "Erreur API (${e.code()}) : ${e.message()}"
            }
        } catch (e: Exception) {
            "Erreur : ${e.localizedMessage}"
        }
    }
}
