package com.example.noteai.presentation.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnalysisScreen(
    state: AnalysisState,
    onNavigateToSummary: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isGenerating) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("L'IA analyse votre cours...", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text("Génération du résumé et des quiz...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (state.error != null) {
            Text("Erreur: ${state.error}", color = MaterialTheme.colorScheme.error)
            Button(onClick = onBack) { Text("Retour") }
        } else if (state.summary != null) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Analyse terminée !", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = onNavigateToSummary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voir la fiche de révision")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNavigateToQuiz,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("S'entraîner avec le Quiz")
            }
        }
    }
}
