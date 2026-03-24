import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

/// Point d'entrée de l'application Grade Calculator.
///
/// Cette application permet de traiter un fichier Excel contenant
/// des notes d'étudiants, calculer les moyennes, attribuer des grades
/// et déterminer les statuts (Validé / Rattrapage).
void main() {
  runApp(const GradeCalculatorApp());
}

/// Widget racine de l'application.
class GradeCalculatorApp extends StatelessWidget {
  const GradeCalculatorApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Grade Calculator',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        // Thème moderne avec des couleurs profondes
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF7C4DFF),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
        // Typographie élégante
        textTheme: const TextTheme(
          headlineLarge: TextStyle(
            fontWeight: FontWeight.bold,
            letterSpacing: -0.5,
          ),
          bodyLarge: TextStyle(fontSize: 16, height: 1.5),
        ),
        // Composants Material 3
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            elevation: 4,
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(14),
            ),
          ),
        ),
        cardTheme: CardThemeData(
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
        ),
      ),
      home: const HomeScreen(),
    );
  }
}
