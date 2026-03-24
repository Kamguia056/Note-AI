import 'package:flutter_test/flutter_test.dart';
import 'package:greadcalculator/main.dart';

void main() {
  testWidgets('GradeCalculatorApp smoke test', (WidgetTester tester) async {
    // Construire l'application et déclencher une frame
    await tester.pumpWidget(const GradeCalculatorApp());

    // Vérifier que le titre de l'app est affiché
    expect(find.text('Grade Calculator'), findsOneWidget);

    // Vérifier que le bouton d'import est présent
    expect(find.text('Importer un fichier Excel'), findsOneWidget);

    // Vérifier que l'état vide est affiché
    expect(find.text('Aucun fichier importé'), findsOneWidget);
  });
}
