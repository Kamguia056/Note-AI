import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:file_picker/file_picker.dart';
import 'package:fl_chart/fl_chart.dart';
import '../models/student.dart';
import '../services/excel_service.dart';
import '../services/file_save_service.dart';

/// Écran principal de l'application Grade Calculator.
///
/// Permet l'importation d'un fichier Excel, l'affichage des résultats
/// sous forme de tableau, la recherche, le tri, et l'exportation.
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen>
    with SingleTickerProviderStateMixin {
  /// Liste complète des étudiants importés
  List<Student> _students = [];

  /// Liste filtrée (après recherche)
  List<Student> _filteredStudents = [];

  /// Contrôleur pour la barre de recherche
  final TextEditingController _searchController = TextEditingController();

  /// État de chargement
  bool _isLoading = false;

  /// Message d'erreur éventuel
  String? _errorMessage;

  /// Colonne de tri actuelle
  int _sortColumnIndex = 0;

  /// Direction du tri
  bool _sortAscending = true;

  /// Contrôleur d'animation
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    );
    _fadeAnimation = CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeInOut,
    );
  }

  @override
  void dispose() {
    _searchController.dispose();
    _animationController.dispose();
    super.dispose();
  }

  /// Importe un fichier Excel sélectionné par l'utilisateur.
  Future<void> _importFile() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      // Ouvrir le sélecteur de fichiers
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['xlsx', 'xls'],
        allowMultiple: false,
      );

      if (result == null || result.files.isEmpty) {
        setState(() => _isLoading = false);
        return;
      }

      final fileBytes = result.files.single.bytes;
      if (fileBytes == null) {
        throw Exception('Impossible d\'accéder au fichier sélectionné.');
      }

      // Importer et traiter le fichier
      final students = await ExcelService.importExcel(fileBytes);

      setState(() {
        _students = students;
        _filteredStudents = List.from(students);
        _isLoading = false;
        _searchController.clear();
      });

      // Animation d'apparition des résultats
      _animationController.reset();
      _animationController.forward();

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              '${students.length} étudiant(s) importé(s) avec succès !',
            ),
            backgroundColor: const Color(0xFF2E7D32),
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        );
      }
    } catch (e) {
      setState(() {
        _isLoading = false;
        _errorMessage = e.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  /// Exporte les résultats dans un nouveau fichier Excel.
  Future<void> _exportFile() async {
    if (_students.isEmpty) return;

    setState(() => _isLoading = true);

    try {
      final bytes = ExcelService.exportExcel(_students);
      final fileName =
          'resultats_etudiants_${DateTime.now().millisecondsSinceEpoch}.xlsx';

      // Sauvegarder/télécharger le fichier
      await FileSaveService.saveFile(bytes, fileName);

      setState(() => _isLoading = false);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: const Text('Fichier exporté avec succès !'),
            backgroundColor: const Color(0xFF2E7D32),
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        );
      }
    } catch (e) {
      setState(() {
        _isLoading = false;
        _errorMessage = e.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  /// Filtre les étudiants selon le texte de recherche.
  void _filterStudents(String query) {
    setState(() {
      if (query.isEmpty) {
        _filteredStudents = List.from(_students);
      } else {
        _filteredStudents = _students
            .where((s) => s.name.toLowerCase().contains(query.toLowerCase()))
            .toList();
      }
    });
  }

  /// Trie les étudiants selon une colonne donnée.
  void _sortStudents(int columnIndex, bool ascending) {
    setState(() {
      _sortColumnIndex = columnIndex;
      _sortAscending = ascending;

      _filteredStudents.sort((a, b) {
        int result;
        switch (columnIndex) {
          case 0:
            result = a.name.compareTo(b.name);
            break;
          case 1:
            result = a.note1.compareTo(b.note1);
            break;
          case 2:
            result = a.note2.compareTo(b.note2);
            break;
          case 3:
            result = a.moyenne.compareTo(b.moyenne);
            break;
          case 4:
            result = a.noteSur5.compareTo(b.noteSur5);
            break;
          case 5:
            result = a.grade.compareTo(b.grade);
            break;
          case 6:
            result = a.statut.compareTo(b.statut);
            break;
          default:
            result = 0;
        }
        return ascending ? result : -result;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E)],
          ),
        ),
        child: SafeArea(
          child: Column(
            children: [
              _buildAppBar(),
              Expanded(
                child: _isLoading
                    ? const Center(
                        child: CircularProgressIndicator(color: Colors.white),
                      )
                    : _students.isEmpty
                    ? _buildEmptyState()
                    : _buildResultsView(),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// Construit la barre d'application personnalisée.
  Widget _buildAppBar() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: Row(
        children: [
          // Logo et titre
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(
              Icons.school_rounded,
              color: Colors.white,
              size: 28,
            ),
          ),
          const SizedBox(width: 14),
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Grade Calculator',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.5,
                  ),
                ),
                Text(
                  'Traitement des notes étudiants',
                  style: TextStyle(color: Colors.white60, fontSize: 13),
                ),
              ],
            ),
          ),
          // Boutons d'action
          if (_students.isNotEmpty) ...[
            _buildActionButton(
              icon: Icons.file_download_rounded,
              tooltip: 'Exporter Excel',
              onPressed: _exportFile,
              color: const Color(0xFF00BFA5),
            ),
            const SizedBox(width: 8),
          ],
          _buildActionButton(
            icon: Icons.upload_file_rounded,
            tooltip: 'Importer Excel',
            onPressed: _importFile,
            color: const Color(0xFF7C4DFF),
          ),
        ],
      ),
    );
  }

  /// Construit un bouton d'action stylisé.
  Widget _buildActionButton({
    required IconData icon,
    required String tooltip,
    required VoidCallback onPressed,
    required Color color,
  }) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onPressed,
        borderRadius: BorderRadius.circular(14),
        child: Tooltip(
          message: tooltip,
          child: Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.2),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: color.withValues(alpha: 0.3)),
            ),
            child: Icon(icon, color: color, size: 24),
          ),
        ),
      ),
    );
  }

  /// Construit l'état vide (aucun fichier importé).
  Widget _buildEmptyState() {
    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(vertical: 24),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Icône animée
            Container(
              width: 120,
              height: 120,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.08),
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.cloud_upload_rounded,
                size: 56,
                color: Colors.white.withValues(alpha: 0.6),
              ),
            ),
            const SizedBox(height: 28),
            Text(
              'Aucun fichier importé',
              style: TextStyle(
                color: Colors.white.withValues(alpha: 0.9),
                fontSize: 22,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              'Importez un fichier Excel (.xlsx) contenant\nles notes des étudiants pour commencer.',
              textAlign: TextAlign.center,
              style: TextStyle(
                color: Colors.white.withValues(alpha: 0.5),
                fontSize: 15,
                height: 1.5,
              ),
            ),
            const SizedBox(height: 36),
            // Bouton d'import principal
            ElevatedButton.icon(
              onPressed: _importFile,
              icon: const Icon(Icons.upload_file_rounded),
              label: const Text(
                'Importer un fichier Excel',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
              ),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF7C4DFF),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(
                  horizontal: 32,
                  vertical: 18,
                ),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
                elevation: 8,
                shadowColor: const Color(0xFF7C4DFF).withValues(alpha: 0.4),
              ),
            ),
            // Message d'erreur
            if (_errorMessage != null) ...[
              const SizedBox(height: 24),
              Container(
                margin: const EdgeInsets.symmetric(horizontal: 32),
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.red.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.red.withValues(alpha: 0.3)),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.error_outline, color: Colors.redAccent),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        _errorMessage!,
                        style: const TextStyle(
                          color: Colors.redAccent,
                          fontSize: 13,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
            const SizedBox(height: 40),
            // Instructions de format
            Container(
              margin: const EdgeInsets.symmetric(horizontal: 32),
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.05),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: Colors.white.withValues(alpha: 0.1)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(
                        Icons.info_outline,
                        color: Colors.white.withValues(alpha: 0.6),
                        size: 20,
                      ),
                      const SizedBox(width: 8),
                      Text(
                        'Format attendu du fichier',
                        style: TextStyle(
                          color: Colors.white.withValues(alpha: 0.8),
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  _buildFormatRow('Colonne 1', 'Nom de l\'étudiant'),
                  _buildFormatRow('Colonne 2', 'Note Évaluation 1 (/20)'),
                  _buildFormatRow('Colonne 3', 'Note Évaluation 2 (/20)'),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Construit une ligne d'instruction de format.
  Widget _buildFormatRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(top: 6),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
            decoration: BoxDecoration(
              color: const Color(0xFF7C4DFF).withValues(alpha: 0.2),
              borderRadius: BorderRadius.circular(6),
            ),
            child: Text(
              label,
              style: const TextStyle(
                color: Color(0xFF7C4DFF),
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          const SizedBox(width: 10),
          Text(
            value,
            style: TextStyle(
              color: Colors.white.withValues(alpha: 0.6),
              fontSize: 13,
            ),
          ),
        ],
      ),
    );
  }

  /// Construit la vue des résultats (statistiques + tableau + graphique).
  Widget _buildResultsView() {
    return FadeTransition(
      opacity: _fadeAnimation,
      child: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Barre de recherche
            _buildSearchBar(),
            const SizedBox(height: 16),

            // Cartes de statistiques
            _buildStatisticsCards(),
            const SizedBox(height: 20),

            // Message d'erreur
            if (_errorMessage != null) ...[
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.red.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    const Icon(
                      Icons.error_outline,
                      color: Colors.redAccent,
                      size: 20,
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _errorMessage!,
                        style: const TextStyle(
                          color: Colors.redAccent,
                          fontSize: 13,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
            ],

            // Tableau des résultats
            _buildResultsTable(),
            const SizedBox(height: 24),

            // Graphique de performance
            _buildPerformanceChart(),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }

  /// Construit la barre de recherche.
  Widget _buildSearchBar() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.white.withValues(alpha: 0.1)),
      ),
      child: TextField(
        controller: _searchController,
        onChanged: _filterStudents,
        style: const TextStyle(color: Colors.white),
        decoration: InputDecoration(
          hintText: 'Rechercher un étudiant...',
          hintStyle: TextStyle(color: Colors.white.withValues(alpha: 0.4)),
          prefixIcon: Icon(
            Icons.search_rounded,
            color: Colors.white.withValues(alpha: 0.5),
          ),
          suffixIcon: _searchController.text.isNotEmpty
              ? IconButton(
                  icon: Icon(
                    Icons.clear,
                    color: Colors.white.withValues(alpha: 0.5),
                  ),
                  onPressed: () {
                    _searchController.clear();
                    _filterStudents('');
                  },
                )
              : null,
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 16,
            vertical: 14,
          ),
        ),
      ),
    );
  }

  /// Construit les cartes de statistiques.
  Widget _buildStatisticsCards() {
    final totalStudents = _students.length;
    final validated = _students.where((s) => s.isValidated).length;
    final rattrapage = totalStudents - validated;
    final classAverage = totalStudents > 0
        ? _students.map((s) => s.moyenne).reduce((a, b) => a + b) /
              totalStudents
        : 0.0;

    return Row(
      children: [
        Expanded(
          child: _buildStatCard(
            'Total',
            '$totalStudents',
            Icons.people_rounded,
            const Color(0xFF7C4DFF),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _buildStatCard(
            'Validés',
            '$validated',
            Icons.check_circle_rounded,
            const Color(0xFF2E7D32),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _buildStatCard(
            'Rattrapage',
            '$rattrapage',
            Icons.warning_rounded,
            const Color(0xFFE53935),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _buildStatCard(
            'Moyenne',
            classAverage.toStringAsFixed(1),
            Icons.analytics_rounded,
            const Color(0xFFFFA726),
          ),
        ),
      ],
    );
  }

  /// Construit une carte de statistique individuelle.
  Widget _buildStatCard(
    String label,
    String value,
    IconData icon,
    Color color,
  ) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: color.withValues(alpha: 0.2)),
      ),
      child: Column(
        children: [
          Icon(icon, color: color, size: 24),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              color: color,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: TextStyle(
              color: Colors.white.withValues(alpha: 0.6),
              fontSize: 11,
            ),
          ),
        ],
      ),
    );
  }

  /// Construit le tableau de résultats avec tri.
  Widget _buildResultsTable() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.06),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withValues(alpha: 0.1)),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(16),
        child: SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: DataTable(
            sortColumnIndex: _sortColumnIndex,
            sortAscending: _sortAscending,
            headingRowColor: WidgetStateProperty.all(
              Colors.white.withValues(alpha: 0.08),
            ),
            dataRowColor: WidgetStateProperty.resolveWith((states) {
              if (states.contains(WidgetState.hovered)) {
                return Colors.white.withValues(alpha: 0.05);
              }
              return Colors.transparent;
            }),
            columnSpacing: 20,
            horizontalMargin: 16,
            columns: [
              _buildSortableColumn('Nom', 0),
              _buildSortableColumn('Note 1', 1),
              _buildSortableColumn('Note 2', 2),
              _buildSortableColumn('Moyenne', 3),
              _buildSortableColumn('Note/5', 4),
              _buildSortableColumn('Grade', 5),
              _buildSortableColumn('Statut', 6),
            ],
            rows: _filteredStudents.map((student) {
              final isValidated = student.isValidated;
              final statusColor = isValidated
                  ? const Color(0xFF4CAF50)
                  : const Color(0xFFEF5350);

              return DataRow(
                cells: [
                  DataCell(
                    Text(
                      student.name,
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                  DataCell(
                    Text(
                      student.note1.toStringAsFixed(1),
                      style: const TextStyle(color: Colors.white70),
                    ),
                  ),
                  DataCell(
                    Text(
                      student.note2.toStringAsFixed(1),
                      style: const TextStyle(color: Colors.white70),
                    ),
                  ),
                  DataCell(
                    Text(
                      student.moyenne.toStringAsFixed(2),
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                  DataCell(
                    Text(
                      student.noteSur5.toStringAsFixed(2),
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                  DataCell(_buildGradeBadge(student.grade)),
                  DataCell(_buildStatusBadge(student.statut, statusColor)),
                ],
              );
            }).toList(),
          ),
        ),
      ),
    );
  }

  /// Construit une colonne triable pour le DataTable.
  DataColumn _buildSortableColumn(String label, int index) {
    return DataColumn(
      label: Text(
        label,
        style: const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.bold,
          fontSize: 13,
        ),
      ),
      onSort: _sortStudents,
    );
  }

  /// Construit un badge de grade avec couleur appropriée.
  Widget _buildGradeBadge(String grade) {
    Color color;
    switch (grade) {
      case 'A':
        color = const Color(0xFF4CAF50);
        break;
      case 'B':
        color = const Color(0xFF8BC34A);
        break;
      case 'C':
        color = const Color(0xFFFFA726);
        break;
      case 'D':
        color = const Color(0xFFFF7043);
        break;
      default:
        color = const Color(0xFFEF5350);
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.2),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color.withValues(alpha: 0.4)),
      ),
      child: Text(
        grade,
        style: TextStyle(
          color: color,
          fontWeight: FontWeight.bold,
          fontSize: 14,
        ),
      ),
    );
  }

  /// Construit un badge de statut (Validé / Rattrapage).
  Widget _buildStatusBadge(String status, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            status == 'Validé'
                ? Icons.check_circle_outline
                : Icons.warning_amber_rounded,
            color: color,
            size: 16,
          ),
          const SizedBox(width: 6),
          Text(
            status,
            style: TextStyle(
              color: color,
              fontWeight: FontWeight.w600,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }

  /// Construit le graphique de distribution des grades (bar chart).
  Widget _buildPerformanceChart() {
    // Compter les étudiants par grade
    final gradeCount = {'A': 0, 'B': 0, 'C': 0, 'D': 0, 'F': 0};
    for (final student in _students) {
      gradeCount[student.grade] = (gradeCount[student.grade] ?? 0) + 1;
    }

    final grades = ['A', 'B', 'C', 'D', 'F'];
    final colors = [
      const Color(0xFF4CAF50),
      const Color(0xFF8BC34A),
      const Color(0xFFFFA726),
      const Color(0xFFFF7043),
      const Color(0xFFEF5350),
    ];

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.06),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withValues(alpha: 0.1)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Row(
            children: [
              Icon(Icons.bar_chart_rounded, color: Colors.white70, size: 22),
              SizedBox(width: 10),
              Text(
                'Distribution des Grades',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          SizedBox(
            height: 200,
            child: BarChart(
              BarChartData(
                alignment: BarChartAlignment.spaceAround,
                maxY:
                    (_students.isNotEmpty
                        ? gradeCount.values
                              .reduce((a, b) => a > b ? a : b)
                              .toDouble()
                        : 5) +
                    1,
                barTouchData: BarTouchData(
                  enabled: true,
                  touchTooltipData: BarTouchTooltipData(
                    tooltipRoundedRadius: 8,
                    getTooltipItem: (group, groupIndex, rod, rodIndex) {
                      return BarTooltipItem(
                        '${grades[group.x]}: ${rod.toY.toInt()} étudiant(s)',
                        const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.w500,
                          fontSize: 12,
                        ),
                      );
                    },
                  ),
                ),
                titlesData: FlTitlesData(
                  show: true,
                  bottomTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      getTitlesWidget: (value, meta) {
                        final index = value.toInt();
                        if (index >= 0 && index < grades.length) {
                          return Padding(
                            padding: const EdgeInsets.only(top: 8),
                            child: Text(
                              grades[index],
                              style: TextStyle(
                                color: colors[index],
                                fontWeight: FontWeight.bold,
                                fontSize: 14,
                              ),
                            ),
                          );
                        }
                        return const SizedBox.shrink();
                      },
                    ),
                  ),
                  leftTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      reservedSize: 30,
                      getTitlesWidget: (value, meta) {
                        if (value == value.roundToDouble()) {
                          return Text(
                            value.toInt().toString(),
                            style: TextStyle(
                              color: Colors.white.withValues(alpha: 0.5),
                              fontSize: 12,
                            ),
                          );
                        }
                        return const SizedBox.shrink();
                      },
                    ),
                  ),
                  topTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  rightTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                ),
                gridData: FlGridData(
                  show: true,
                  drawVerticalLine: false,
                  horizontalInterval: 1,
                  getDrawingHorizontalLine: (value) => FlLine(
                    color: Colors.white.withValues(alpha: 0.08),
                    strokeWidth: 1,
                  ),
                ),
                borderData: FlBorderData(show: false),
                barGroups: List.generate(grades.length, (index) {
                  return BarChartGroupData(
                    x: index,
                    barRods: [
                      BarChartRodData(
                        toY: gradeCount[grades[index]]!.toDouble(),
                        color: colors[index],
                        width: 28,
                        borderRadius: const BorderRadius.only(
                          topLeft: Radius.circular(6),
                          topRight: Radius.circular(6),
                        ),
                        backDrawRodData: BackgroundBarChartRodData(
                          show: true,
                          toY:
                              (_students.isNotEmpty
                                  ? gradeCount.values
                                        .reduce((a, b) => a > b ? a : b)
                                        .toDouble()
                                  : 5) +
                              1,
                          color: Colors.white.withValues(alpha: 0.04),
                        ),
                      ),
                    ],
                  );
                }),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
