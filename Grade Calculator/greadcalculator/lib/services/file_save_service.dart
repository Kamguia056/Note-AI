import 'dart:typed_data';
import 'package:flutter/foundation.dart' show kIsWeb;

// Imports conditionnels pour le web et le mobile
import 'file_save_service_stub.dart'
    if (dart.library.html) 'file_save_service_web.dart'
    if (dart.library.io) 'file_save_service_mobile.dart'
    as platform;

/// Service de sauvegarde de fichiers compatible web et mobile.
///
/// Utilise des imports conditionnels pour charger l'implémentation
/// appropriée selon la plateforme.
class FileSaveService {
  /// Sauvegarde/télécharge un fichier.
  ///
  /// Sur web : déclenche un téléchargement dans le navigateur.
  /// Sur mobile : sauvegarde dans le dossier documents + propose le partage.
  static Future<void> saveFile(Uint8List bytes, String fileName) async {
    await platform.saveFile(bytes, fileName);
  }
}
