package com.example.noteai.presentation.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteai.domain.model.ChatMessage
import com.example.noteai.domain.model.MessageRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatState,
    onSendMessage: (String, String?) -> Unit,
    onRegenerate: () -> Unit,
    onNewConversation: () -> Unit,
    onSelectConversation: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState() // Pour le scroll auto
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    var attachedFileUri by remember { mutableStateOf<Uri?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }
    
    var isRecording by remember { mutableStateOf(false) }
    val voiceRecorder = remember { com.example.noteai.util.VoiceRecorder(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
            voiceRecorder.startRecording()
        }
    }

    // Effet pour scroller vers le bas automatiquement à chaque nouveau message
    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            attachedFileUri = it
            attachedFileName = it.lastPathSegment ?: "Document"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("Historique", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                NavigationDrawerItem(
                    label = { Text("Nouvelle discussion") },
                    selected = false,
                    onClick = {
                        onNewConversation()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                LazyColumn {
                    items(state.conversations) { conversation ->
                        NavigationDrawerItem(
                            label = { Text(conversation.title) },
                            selected = conversation.id == state.currentConversationId,
                            onClick = {
                                onSelectConversation(conversation.id)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "NoteAI Chat", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold 
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Messages list
                Box(modifier = Modifier.weight(1f)) {
                    if (state.currentConversationId == null && state.messages.isEmpty()) {
                        EmptyChatPlaceholder()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(state.messages, key = { it.id }) { message ->
                                Column {
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                                    ) {
                                        ChatBubble(message)
                                    }
                                }
                            }
                            if (state.isTyping) {
                                item { TypingIndicator() }
                            } else if (state.messages.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        TextButton(
                                            onClick = onRegenerate,
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Régénérer la solution")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Input bar
                ChatInputBar(
                    onSendMessage = { text ->
                        onSendMessage(text, attachedFileUri?.toString())
                        attachedFileUri = null
                        attachedFileName = null
                        keyboardController?.hide() // Cache le clavier après envoi
                    },
                    onAttachFile = { filePickerLauncher.launch("*/*") },
                    onAttachImage = { filePickerLauncher.launch("image/*") },
                    onTakePhoto = { /* TODO: Implémenter Caméra */ },
                    onRecordVoice = { 
                        if (isRecording) {
                            val file = voiceRecorder.stopRecording()
                            isRecording = false
                            file?.let {
                                onSendMessage("Message vocal envoyé 🎙️", Uri.fromFile(it).toString())
                            }
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    attachedFileName = attachedFileName,
                    onRemoveAttachment = {
                        attachedFileUri = null
                        attachedFileName = null
                    },
                    isLoading = state.isTyping,
                    isRecording = isRecording
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.attachmentUri != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        val uri = android.net.Uri.parse(message.attachmentUri)
                        Text(
                            text = uri.lastPathSegment ?: "Fichier joint",
                            color = textColor,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
        Text(
            text = if (isUser) "Vous" else "NoteAI",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
        )
    }
}

@Composable
fun ChatInputBar(
    onSendMessage: (String) -> Unit,
    onAttachFile: () -> Unit,
    onAttachImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordVoice: () -> Unit,
    attachedFileName: String?,
    onRemoveAttachment: () -> Unit,
    isLoading: Boolean,
    isRecording: Boolean
) {
    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Preview de la pièce jointe
            if (attachedFileName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = attachedFileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    IconButton(onClick = onRemoveAttachment, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Retirer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    IconButton(onClick = { showMenu = true }, enabled = !isLoading && !isRecording) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Plus", tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Importer Fichier") },
                            onClick = { showMenu = false; onAttachFile() },
                            leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Importer Image") },
                            onClick = { showMenu = false; onAttachImage() },
                            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Prendre une Photo") },
                            onClick = { showMenu = false; onTakePhoto() },
                            leadingIcon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) }
                        )
                    }
                }

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(if (isRecording) "Enregistrement..." else "Message NoteAI...") },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4,
                    enabled = !isLoading && !isRecording
                )

                Spacer(Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        val hasText = text.isNotBlank()
                        val hasAttachment = attachedFileName != null
                        
                        if (hasText || hasAttachment) {
                            val finalMsg = if (hasText) text else "Fichier joint : $attachedFileName"
                            onSendMessage(finalMsg)
                            text = ""
                        } else {
                            onRecordVoice()
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = if ((text.isNotBlank() || attachedFileName != null || isRecording) && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                    } else if (text.isNotBlank() || attachedFileName != null) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Envoyer",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else if (isRecording) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Arrêter",
                            tint = Color.Red
                        )
                    } else {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Enregistrer",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "NoteAI à votre écoute",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Analysez vos cours ou posez des questions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer(scaleX = pulse, scaleY = pulse, alpha = pulse)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "NoteAI réfléchit...",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary.copy(alpha = pulse)
        )
    }
}