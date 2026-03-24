/// Modèle représentant un étudiant avec ses notes et calculs associés.
///
/// Chaque étudiant a un nom, deux notes d'évaluation, et des propriétés
/// calculées automatiquement : moyenne, note sur 5, grade et statut.
class Student {
  /// Nom de l'étudiant
  final String name;

  /// Note de la première évaluation (/20)
  final double note1;

  /// Note de la deuxième évaluation (/20)
  final double note2;

  Student({required this.name, required this.note1, required this.note2});

  /// Calcule la moyenne des deux notes (/20)
  /// moyenne = (note1 + note2) / 2
  double get moyenne => (note1 + note2) / 2;

  /// Ramène la moyenne sur 5
  /// note_sur_5 = (moyenne / 20) * 5
  double get noteSur5 => (moyenne / 20) * 5;

  /// Attribue un grade selon la note sur 5 :
  /// - A : [4.5 – 5]
  /// - B : [4 – 4.49]
  /// - C : [3 – 3.99]
  /// - D : [2 – 2.99]
  /// - F : [0 – 1.99]
  String get grade {
    final n = noteSur5;
    if (n >= 4.5) return 'A';
    if (n >= 4.0) return 'B';
    if (n >= 3.0) return 'C';
    if (n >= 2.0) return 'D';
    return 'F';
  }

  /// Détermine le statut de l'étudiant :
  /// - Si grade = D ou F → "Rattrapage"
  /// - Sinon → "Validé"
  String get statut {
    return (grade == 'D' || grade == 'F') ? 'Rattrapage' : 'Validé';
  }

  /// Vérifie si l'étudiant a validé
  bool get isValidated => statut == 'Validé';

  @override
  String toString() {
    return 'Student(name: $name, note1: $note1, note2: $note2, '
        'moyenne: ${moyenne.toStringAsFixed(2)}, '
        'noteSur5: ${noteSur5.toStringAsFixed(2)}, '
        'grade: $grade, statut: $statut)';
  }
}
