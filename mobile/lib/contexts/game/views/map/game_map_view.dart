import 'package:flutter/material.dart';
import '../../../../style/style.dart';
import '../../data/map_data_service.dart';
import '../../domain/model/map_models.dart';
import '../../../../shared/map/map_viewer_widget.dart';

class GameMapView extends StatefulWidget {

  const GameMapView({super.key});

  @override
  State<GameMapView> createState() => _GameMapViewState();

}

class _GameMapViewState extends State<GameMapView> {
  static const double maxHeight = 0.7;
  final MapDataService _mapDataService = MapDataService();
  final PageController _pageController = PageController();
  final ValueNotifier<int> _currentIndex = ValueNotifier<int>(0);
  
  // Variables pour contrôler l'affichage des zones et points
  bool _showZones = true;
  bool _showPoints = true;
  
  // Variables pour stocker les informations de debug
  List<GameMapData> _loadedMaps = [];
  final Map<String, Map<String, dynamic>> _debugInfo = {};

  @override
  void dispose() {
    _pageController.dispose();
    _currentIndex.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Cartes du Jeu'),
        backgroundColor: Colors.blue.shade800,
        foregroundColor: Colors.white,
        actions: [
          // Toggle zones
          IconButton(
            icon: Icon(_showZones ? Icons.crop_square : Icons.crop_square_outlined),
            onPressed: () {
              setState(() {
                _showZones = !_showZones;
              });
            },
            tooltip: _showZones ? 'Masquer les zones' : 'Afficher les zones',
          ),
          // Toggle points
          IconButton(
            icon: Icon(_showPoints ? Icons.place : Icons.place_outlined),
            onPressed: () {
              setState(() {
                _showPoints = !_showPoints;
              });
            },
            tooltip: _showPoints ? 'Masquer les points' : 'Afficher les points',
          ),
          // Debug info
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: () => _showDebugInfo(),
            tooltip: 'Informations de debug',
          ),
        ],
      ),
      body: FutureBuilder<List<GameMapData>>(
        future: _mapDataService.loadMaps(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            _loadedMaps = snapshot.data!;
            return _buildMap(context, snapshot.data!);
          } else if (snapshot.hasError) {
            return Center(child: Text('Erreur : ${snapshot.error}'));
          } else {
            return const Center(child: CircularProgressIndicator());
          }
        },
      ),
    );
  }

  Widget _buildMap(BuildContext context, List<GameMapData> maps) {
    return Column(
      children: [
        SizedBox(
          height: MediaQuery.of(context).size.height * maxHeight,
          child: PageView.builder(
            controller: _pageController,
            itemCount: maps.length,
            physics: const BouncingScrollPhysics(),
            onPageChanged: (index) {
              _currentIndex.value = index;
            },
            itemBuilder: (BuildContext context, int index) {
              return _buildMapSlide(context, map: maps[index]);
            },
          ),
        ),
        const SizedBox(height: 16),
        ValueListenableBuilder<int>(
          valueListenable: _currentIndex,
          builder: (BuildContext context, int currentIndex, Widget? _) {
            return Column(
              children: [
                CarouselDots(
                  totalItems: maps.length,
                  currentIndex: currentIndex,
                ),
                const SizedBox(height: 8),
                Text(
                  maps[currentIndex].label,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                _buildMapInfo(maps[currentIndex]),
              ],
            );
          },
        ),
      ],
    );
  }

  Widget _buildMapSlide(BuildContext context, {required GameMapData map}) {
    // SOLUTION FINALE: Utiliser le widget partagé avec les mêmes dimensions que l'éditeur
    const double containerWidth = 430.0;  // Même largeur que l'éditeur
    const double containerHeight = 238.8; // Même hauteur que l'éditeur
    
    print('🔍 LAYOUT VIEWER CONTAINER:');
    print('  Container demandé: ${containerWidth} x ${containerHeight}');
    print('  Margin horizontal: 8px de chaque côté');
    
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 8),
      // DEBUG: Bordure rouge pour voir exactement où est le container
      decoration: BoxDecoration(
        border: Border.all(color: Colors.red, width: 2),
      ),
      // IMPORTANT: Centrer le widget dans l'espace disponible
      child: Center(
        child: Container(
          // FORCER exactement les mêmes dimensions que l'éditeur
          width: containerWidth,
          height: containerHeight,
          // DEBUG: Bordure bleue pour voir exactement où est le MapViewerWidget
          decoration: BoxDecoration(
            border: Border.all(color: Colors.blue, width: 1),
          ),
          child: MapViewerWidget(
            map: map,
            showZones: _showZones,
            showPoints: _showPoints,
            width: containerWidth,
            height: containerHeight,
            onPositionTap: _showPositionInfo,
          ),
        ),
      ),
    );
  }






  Widget _buildMapInfo(GameMapData map) {
    final zones = map.positions.where((p) => p.isZone).length;
    final points = map.positions.where((p) => p.isPoint).length;
    
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        if (_showZones) ...[
          Icon(Icons.crop_square, size: 16, color: Colors.blue),
          const SizedBox(width: 4),
          Text('$zones zones'),
          const SizedBox(width: 16),
        ],
        if (_showPoints) ...[
          Icon(Icons.place, size: 16, color: Colors.orange),
          const SizedBox(width: 4),
          Text('$points points'),
        ],
      ],
    );
  }

  void _showPositionInfo(GameMapPosition position) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(position.label),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Type: ${position.type}'),
            Text('ID: ${position.id}'),
            if (position.isPoint && position.position != null)
              Text('Position: (${position.position!.dx.toStringAsFixed(1)}, ${position.position!.dy.toStringAsFixed(1)})'),
            if (position.isZone && position.bounds != null)
              Text('Zone: ${position.bounds!.width.toStringAsFixed(1)} x ${position.bounds!.height.toStringAsFixed(1)}'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Fermer'),
          ),
        ],
      ),
    );
  }

  void _showDebugInfo() {
    if (_loadedMaps.isEmpty) return;
    
    final currentMap = _loadedMaps[_currentIndex.value];
    final viewerDebug = _debugInfo[currentMap.label];
    final editorDebug = currentMap.debug;
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Debug: ${currentMap.label}'),
        content: SizedBox(
          width: double.maxFinite,
          height: 500,
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('🎯 COMPARAISON ÉDITEUR vs VIEWER', 
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                const SizedBox(height: 16),
                
                if (editorDebug != null) ...[
                  const Text('📝 ÉDITEUR:', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.blue)),
                  Text('Container: ${editorDebug['editor_container_size']}'),
                  Text('Scale: ${editorDebug['editor_scale']}'),
                  Text('Offset: ${editorDebug['editor_offset']}'),
                  Text('Aspect Ratio: ${editorDebug['image_aspect_ratio']?.toStringAsFixed(3)}'),
                  const SizedBox(height: 12),
                ],
                
                if (viewerDebug != null) ...[
                  const Text('👁️ VIEWER:', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.green)),
                  Text('Container: ${viewerDebug['viewer_container_size']}'),
                  Text('Scale: ${viewerDebug['viewer_scale']}'),
                  Text('Offset: ${viewerDebug['viewer_offset']}'),
                  Text('Aspect Ratio: ${viewerDebug['image_aspect_ratio']?.toStringAsFixed(3)}'),
                  Text('Calculated Image: ${viewerDebug['calculated_image_size']}'),
                  const SizedBox(height: 12),
                ],
                
                if (editorDebug != null && viewerDebug != null) ...[
                  const Text('⚖️ DIFFÉRENCES:', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.orange)),
                  _buildDifferenceText('ScaleX', 
                    editorDebug['editor_scale']?['scaleX'], 
                    viewerDebug['viewer_scale']?['scaleX']),
                  _buildDifferenceText('ScaleY', 
                    editorDebug['editor_scale']?['scaleY'], 
                    viewerDebug['viewer_scale']?['scaleY']),
                  _buildDifferenceText('OffsetX', 
                    editorDebug['editor_offset']?['offsetX'], 
                    viewerDebug['viewer_offset']?['offsetX']),
                  _buildDifferenceText('OffsetY', 
                    editorDebug['editor_offset']?['offsetY'], 
                    viewerDebug['viewer_offset']?['offsetY']),
                  const SizedBox(height: 12),
                ],
                
                const Text('📊 IMAGE INFO:', style: TextStyle(fontWeight: FontWeight.bold)),
                Text('Original Size: ${currentMap.image.size.width} x ${currentMap.image.size.height}'),
                Text('Path: ${currentMap.image.path}'),
                Text('Type: ${currentMap.image.type}'),
                const SizedBox(height: 12),
                
                const Text('📍 POSITIONS:', style: TextStyle(fontWeight: FontWeight.bold)),
                Text('Zones: ${currentMap.positions.where((p) => p.isZone).length}'),
                Text('Points: ${currentMap.positions.where((p) => p.isPoint).length}'),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Fermer'),
          ),
        ],
      ),
    );
  }

  Widget _buildDifferenceText(String label, double? editorValue, double? viewerValue) {
    if (editorValue == null || viewerValue == null) {
      return Text('$label: N/A');
    }
    
    final diff = (viewerValue - editorValue).abs();
    final color = diff < 0.01 ? Colors.green : (diff < 1.0 ? Colors.orange : Colors.red);
    
    return Text(
      '$label: Editor=${editorValue.toStringAsFixed(3)}, Viewer=${viewerValue.toStringAsFixed(3)}, Diff=${diff.toStringAsFixed(3)}',
      style: TextStyle(color: color),
    );
  }
}



class CarouselDots extends StatelessWidget {
  const CarouselDots({
    super.key,
    required this.totalItems,
    required this.currentIndex,
  });

  final int totalItems;
  final int currentIndex;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(
        totalItems,
        (index) => AnimatedContainer(
          duration: Style.duration.default_,
          height: 8,
          margin: EdgeInsets.symmetric(horizontal: Style.dimension.small),
          width: currentIndex == index ? 16 : 8,
          decoration: BoxDecoration(
            color: currentIndex == index
                ? Style.color.oneFirstPurple
                : Style.color.lightGrey,
            borderRadius: BorderRadius.circular(Style.dimension.extraLarge),
          ),
        ),
      ),
    );
  }
}

