/// Service de calcul des grades et statuts.
///
/// Fournit des méthodes utilitaires statiques pour calculer
/// les grades et les statuts à partir des notes.
class GradeService {
  /// Calcule le grade à partir de la note sur 5.
  ///
  /// Barème :
  /// - A : [4.5 – 5]
  /// - B : [4 – 4.49]
  /// - C : [3 – 3.99]
  /// - D : [2 – 2.99]
  /// - F : [0 – 1.99]
  static String calculateGrade(double noteSur5) {
    if (noteSur5 >= 4.5) return 'A';
    if (noteSur5 >= 4.0) return 'B';
    if (noteSur5 >= 3.0) return 'C';
    if (noteSur5 >= 2.0) return 'D';
    return 'F';
  }

  /// Détermine le statut en fonction du grade.
  ///
  /// - D ou F → "Rattrapage"
  /// - A, B, C → "Validé"
  static String calculateStatus(String grade) {
    return (grade == 'D' || grade == 'F') ? 'Rattrapage' : 'Validé';
  }

  /// Calcule la moyenne de deux notes.
  static double calculateMoyenne(double note1, double note2) {
    return (note1 + note2) / 2;
  }

  /// Convertit une moyenne sur 20 en note sur 5.
  static double convertToNoteSur5(double moyenne) {
    return (moyenne / 20) * 5;
  }
}
