import 'dart:io';
import 'dart:typed_data';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

/// Sauvegarde de fichiers pour les plateformes mobiles (Android/iOS).
/// Sauvegarde le fichier dans le dossier documents et propose le partage.
Future<void> saveFile(Uint8List bytes, String fileName) async {
  final directory = await getApplicationDocumentsDirectory();
  final filePath = '${directory.path}/$fileName';
  final file = File(filePath);
  await file.writeAsBytes(bytes);

  // Partager le fichier
  await Share.shareXFiles([XFile(filePath)], text: 'Résultats des étudiants');
}
