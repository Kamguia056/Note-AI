import 'dart:typed_data';

/// Stub pour la sauvegarde de fichiers (ne devrait jamais être utilisé).
Future<void> saveFile(Uint8List bytes, String fileName) async {
  throw UnsupportedError('Plateforme non supportée');
}
