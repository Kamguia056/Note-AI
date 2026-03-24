# Documentation Technique - NoteAI

## 1. Présentation du projet

NoteAI est un assistant pédagogique intelligent centré sur la **discussion**. L'application s'ouvre sur une interface de chat permettant d'interagir directement avec une intelligence artificielle (Gemini) pour analyser des cours, poser des questions ou générer des outils de révision.

## 2. Architecture logicielle

L'application utilise désormais un moteur de chat en temps réel :

- **Chat Layer** : Gestion des messages et des conversations avec persistance locale.
- **IA Integration** : Utilisation du SDK Google Gemini AI pour des réponses réelles et contextuelles.
- **Sidebar Navigation** : Accès rapide à l'historique des discussions via un tiroir de navigation (swipe gauche).

## 3. Flux de fonctionnement

1. L'utilisateur pose une question ou importe un fichier via le bouton "+" de la barre de saisie.
2. L'IA Gemini traite la demande et répond instantanément.
3. Toutes les discussions sont sauvegardées dans Room et accessibles via la barre latérale.
4. Les fonctionnalités de résumé et de quiz restent intégrées en tant que capacités de l'assistant.

## 4. Choix technologiques

- **Langage** : Kotlin.
- **UI** : Jetpack Compose (Material 3).
- **Persistance** : Room Database.
- **Réseau/IA** : Retrofit & Gson (prêt pour l'intégration Gemini).
- **Navigation** : Navigation Compose.
- **DI** : Injection manuelle via `DependencyContainer` (extensible vers Hilt).

## 5. Besoins fonctionnels

- Importation de texte libre.
- Automatisation de l'analyse via IA.
- Affichage de fiches de révision (Résumé, Points clés, Définitions, Formules).
- Génération de quiz QCM interactifs.
- Liste de cours sauvegardés.

## 6. Besoins non fonctionnels

- Performance : Analyse asynchrone pour ne pas bloquer l'UI.
- Scalabilité : Architecture modulaire permettant d'ajouter facilement l'OCR ou de nouveaux moteurs d'IA.
- UX : Interface "Wow" avec Material 3 et animations fluides.

## 7. Structure des dossiers

```text
com.example.noteai
├── data
│   ├── local (Room DB, DAO, Entities)
│   ├── mapper (Mappers Data <-> Domain)
│   └── repository (Implémentations)
├── domain
│   ├── model (Modèles métier)
│   └── repository (Interfaces)
├── ui (Thème et Navigation)
└── presentation
    ├── home (Écran d'accueil)
    ├── analysis (Génération IA)
    ├── import_doc (Saisie de cours)
    ├── summary (Fiche de révision)
    └── quiz (Entraînement)
```

## 9. Flux de fonctionnement

1. L'utilisateur saisit son cours dans `ImportScreen`.
2. L'application enregistre le cours et lance l'analyse dans `AnalysisScreen`.
3. L'IA (via repository) génère la fiche et le quiz.
4. Les résultats sont stockés localement.
5. L'utilisateur navigue entre la fiche de révision et le quiz.

## 10. Améliorations futures

- Intégration réelle de l'API Google Gemini.
- Module OCR (ML Kit) pour scanner des photos de cahiers.
- Exportation des fiches en PDF.
- Statistiques de progression par matière.
