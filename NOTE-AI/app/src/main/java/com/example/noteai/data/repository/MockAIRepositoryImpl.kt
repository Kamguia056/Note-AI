package com.example.noteai.data.repository

import com.example.noteai.domain.model.*
import com.example.noteai.domain.repository.AIRepository
import kotlinx.coroutines.delay
import java.util.UUID

class MockAIRepositoryImpl : AIRepository {

    override suspend fun generateSummary(content: String): CourseSummary {
        delay(2000) // Simulate network delay
        return CourseSummary(
            id = UUID.randomUUID().toString(),
            courseId = "", // To be filled by caller
            briefSummary = "Ce cours traite des principes fondamentaux de l'intelligence artificielle, incluant le machine learning et les réseaux de neurones.",
            revisionSheet = "Exploration de l'histoire de l'IA, des différents types d'apprentissage (supervisé, non-supervisé) et de l'importance des données.",
            keyPoints = listOf(
                "L'IA est la simulation de l'intelligence humaine par des machines.",
                "Le Deep Learning est une sous-catégorie du Machine Learning.",
                "Les réseaux de neurones s'inspirent du cerveau humain."
            ),
            definitions = listOf(
                Definition("Algorithme", "Une suite d'instructions permettant de résoudre un problème."),
                Definition("Modèle", "Une représentation mathématique d'un processus réel.")
            ),
            formulas = listOf("y = wx + b", "Loss = (y_pred - y_true)^2")
        )
    }

    override suspend fun generateQuiz(content: String): Quiz {
        delay(2000)
        return Quiz(
            id = UUID.randomUUID().toString(),
            courseId = "",
            questions = listOf(
                Question(
                    id = "1",
                    text = "Qu'est-ce que le Machine Learning ?",
                    type = QuestionType.QCM,
                    options = listOf("Apprentissage automatique", "Un robot ménager", "Un langage de programmation"),
                    correctAnswer = "Apprentissage automatique",
                    explanation = "Le Machine Learning permet aux systèmes d'apprendre à partir de données."
                ),
                Question(
                    id = "2",
                    text = "L'IA peut-elle remplacer totalement l'humain ?",
                    type = QuestionType.OPEN_ENDED,
                    correctAnswer = "Dépend du contexte et de la créativité.",
                    explanation = "L'IA excelle dans les tâches répétitives mais manque de sens commun."
                )
            )
        )
    }

    override suspend fun extractTextFromImage(imageUri: String): String {
        delay(1500)
        return "Ceci est un exemple de texte extrait d'un document sur l'intelligence artificielle."
    }
}
