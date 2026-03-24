import 'dart:typed_data';
import 'package:excel/excel.dart';
import '../models/student.dart';

/// Service responsable de la lecture et l'écriture de fichiers Excel.
///
/// Ce service gère l'importation de fichiers Excel contenant les notes
/// des étudiants ainsi que l'exportation des résultats calculés.
/// Compatible web et mobile (utilise des bytes, pas des chemins de fichiers).
class ExcelService {
  /// Importe un fichier Excel depuis ses bytes et retourne une liste d'étudiants.
  ///
  /// Le fichier doit contenir au minimum 3 colonnes :
  /// - Colonne 0 : Nom de l'étudiant
  /// - Colonne 1 : Note Évaluation 1
  /// - Colonne 2 : Note Évaluation 2
  ///
  /// [fileBytes] : Bytes du fichier Excel à importer.
  /// Retourne une liste de [Student] avec les données lues.
  /// Lève une [Exception] si le fichier est invalide ou contient des erreurs.
  static Future<List<Student>> importExcel(Uint8List fileBytes) async {
    try {
      // Décoder le fichier Excel depuis les bytes
      final excel = Excel.decodeBytes(fileBytes);

      // Récupérer la première feuille
      final sheetName = excel.tables.keys.first;
      final sheet = excel.tables[sheetName];

      if (sheet == null || sheet.rows.isEmpty) {
        throw Exception('Le fichier Excel est vide ou invalide.');
      }

      final List<Student> students = [];

      // Parcourir les lignes (ignorer l'en-tête - ligne 0)
      for (int i = 1; i < sheet.rows.length; i++) {
        final row = sheet.rows[i];

        // Vérifier que la ligne a assez de colonnes
        if (row.length < 3) {
          throw Exception(
            'Ligne ${i + 1} : données insuffisantes. '
            '3 colonnes requises (Nom, Note 1, Note 2).',
          );
        }

        // Extraire le nom
        final nameCell = row[0];
        if (nameCell == null || nameCell.value == null) {
          throw Exception(
            'Ligne ${i + 1} : le nom de l\'étudiant est manquant.',
          );
        }
        final name = nameCell.value.toString().trim();

        if (name.isEmpty) {
          continue; // Ignorer les lignes vides
        }

        // Extraire et valider les notes
        final note1 = _parseNote(row[1], i + 1, 'Note 1');
        final note2 = _parseNote(row[2], i + 1, 'Note 2');

        // Valider la plage des notes (0-20)
        if (note1 < 0 || note1 > 20) {
          throw Exception(
            'Ligne ${i + 1} : Note 1 ($note1) hors limites. '
            'Les notes doivent être entre 0 et 20.',
          );
        }
        if (note2 < 0 || note2 > 20) {
          throw Exception(
            'Ligne ${i + 1} : Note 2 ($note2) hors limites. '
            'Les notes doivent être entre 0 et 20.',
          );
        }

        students.add(Student(name: name, note1: note1, note2: note2));
      }

      if (students.isEmpty) {
        throw Exception('Aucun étudiant trouvé dans le fichier.');
      }

      return students;
    } catch (e) {
      if (e is Exception) rethrow;
      throw Exception('Erreur lors de la lecture du fichier : $e');
    }
  }

  /// Parse une cellule de note et retourne sa valeur en double.
  ///
  /// [cell] : La cellule contenant la note.
  /// [rowNum] : Numéro de la ligne (pour les messages d'erreur).
  /// [columnName] : Nom de la colonne (pour les messages d'erreur).
  static double _parseNote(Data? cell, int rowNum, String columnName) {
    if (cell == null || cell.value == null) {
      throw Exception('Ligne $rowNum : $columnName est manquante.');
    }

    final value = cell.value;

    // Gérer les différents types de valeurs Excel
    if (value is IntCellValue) {
      return value.value.toDouble();
    } else if (value is DoubleCellValue) {
      return value.value;
    } else {
      // Essayer de parser comme string
      final parsed = double.tryParse(value.toString());
      if (parsed == null) {
        throw Exception(
          'Ligne $rowNum : $columnName ("${value.toString()}") '
          'n\'est pas un nombre valide.',
        );
      }
      return parsed;
    }
  }

  /// Exporte la liste des étudiants avec leurs résultats en bytes Excel.
  ///
  /// Crée un fichier Excel en mémoire contenant les colonnes :
  /// Nom, Note 1, Note 2, Moyenne (/20), Note sur 5, Grade, Statut
  ///
  /// [students] : Liste des étudiants à exporter.
  /// Retourne les bytes du fichier Excel généré.
  static Uint8List exportExcel(List<Student> students) {
    final excel = Excel.createExcel();
    final sheetName = 'Résultats';

    // Renommer la feuille par défaut
    excel.rename(excel.getDefaultSheet()!, sheetName);
    final sheet = excel[sheetName];

    // Style pour l'en-tête
    final headerStyle = CellStyle(
      bold: true,
      horizontalAlign: HorizontalAlign.Center,
      backgroundColorHex: ExcelColor.fromHexString('#4A90D9'),
      fontColorHex: ExcelColor.fromHexString('#FFFFFF'),
    );

    // En-têtes
    final headers = [
      'Nom',
      'Note Évaluation 1',
      'Note Évaluation 2',
      'Moyenne (/20)',
      'Note sur 5',
      'Grade',
      'Statut',
    ];

    for (int i = 0; i < headers.length; i++) {
      final cell = sheet.cell(
        CellIndex.indexByColumnRow(columnIndex: i, rowIndex: 0),
      );
      cell.value = TextCellValue(headers[i]);
      cell.cellStyle = headerStyle;
    }

    // Style pour Validé (vert)
    final validatedStyle = CellStyle(
      fontColorHex: ExcelColor.fromHexString('#2E7D32'),
      bold: true,
    );

    // Style pour Rattrapage (rouge)
    final rattrapageStyle = CellStyle(
      fontColorHex: ExcelColor.fromHexString('#C62828'),
      bold: true,
    );

    // Données des étudiants
    for (int i = 0; i < students.length; i++) {
      final student = students[i];
      final rowIndex = i + 1;

      sheet
          .cell(CellIndex.indexByColumnRow(columnIndex: 0, rowIndex: rowIndex))
          .value = TextCellValue(
        student.name,
      );

      sheet
          .cell(CellIndex.indexByColumnRow(columnIndex: 1, rowIndex: rowIndex))
          .value = DoubleCellValue(
        student.note1,
      );

      sheet
          .cell(CellIndex.indexByColumnRow(columnIndex: 2, rowIndex: rowIndex))
          .value = DoubleCellValue(
        student.note2,
      );

      sheet
          .cell(CellIndex.indexByColumnRow(columnIndex: 3, rowIndex: rowIndex))
          .value = DoubleCellValue(
        double.parse(student.moyenne.toStringAsFixed(2)),
      );

      sheet
          .cell(CellIndex.indexByColumnRow(columnIndex: 4, rowIndex: rowIndex))
          .value = DoubleCellValue(
        double.parse(student.noteSur5.toStringAsFixed(2)),
      );

      sheet
          .cell(CellIndex.indexByColumnRow(columnIndex: 5, rowIndex: rowIndex))
          .value = TextCellValue(
        student.grade,
      );

      final statusCell = sheet.cell(
        CellIndex.indexByColumnRow(columnIndex: 6, rowIndex: rowIndex),
      );
      statusCell.value = TextCellValue(student.statut);
      statusCell.cellStyle = student.isValidated
          ? validatedStyle
          : rattrapageStyle;
    }

    // Générer les bytes
    final fileBytes = excel.save();
    if (fileBytes == null) {
      throw Exception('Erreur lors de la génération du fichier Excel.');
    }

    return Uint8List.fromList(fileBytes);
  }
}
