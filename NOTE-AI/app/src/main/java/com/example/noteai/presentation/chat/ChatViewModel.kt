package com.example.noteai.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteai.domain.model.*
import com.example.noteai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatState(
    val conversations: List<Conversation> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val currentConversationId: String? = null,
    val isTyping: Boolean = false,
    val error: String? = null
)

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            repository.getAllConversations().collect { list ->
                _state.update { it.copy(conversations = list) }
            }
        }
    }

    fun selectConversation(id: String) {
        _state.update { it.copy(currentConversationId = id) }
        viewModelScope.launch {
            repository.getMessagesForConversation(id).collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val id = repository.createConversation("Nouvelle discussion")
            selectConversation(id)
        }
    }

    fun sendMessage(content: String, attachmentUri: String? = null) {
        viewModelScope.launch {
            var convId = _state.value.currentConversationId
            if (convId == null) {
                val title = if (content.length > 25) content.take(22) + "..." else content
                convId = repository.createConversation(title)
                selectConversation(convId)
            }
            
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                content = content,
                role = MessageRole.USER,
                attachmentUri = attachmentUri
            )
            repository.saveMessage(userMsg)
            getAIResponse(convId, content, attachmentUri)
        }
    }

    fun regenerateLastResponse() {
        val convId = _state.value.currentConversationId ?: return
        val messages = _state.value.messages
        if (messages.isEmpty()) return

        val lastMsg = messages.last()
        viewModelScope.launch {
            if (lastMsg.role == MessageRole.AI) {
                repository.deleteMessage(lastMsg.id)
                val lastUserMsg = messages.filter { it.role == MessageRole.USER }.lastOrNull()
                if (lastUserMsg != null) {
                    getAIResponse(convId, lastUserMsg.content, lastUserMsg.attachmentUri)
                }
            } else {
                getAIResponse(convId, lastMsg.content, lastMsg.attachmentUri)
            }
        }
    }

    private suspend fun getAIResponse(convId: String, content: String, attachmentUri: String?) {
        _state.update { it.copy(isTyping = true) }
        val response = repository.getChatResponse(content, convId, attachmentUri)
        
        val aiMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = convId,
            content = response,
            role = MessageRole.AI
        )
        repository.saveMessage(aiMsg)
        _state.update { it.copy(isTyping = false) }
    }
}
