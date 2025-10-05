import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'zone_models.dart';
import '../../shared/map/map_positioning_utils.dart';

/// Éditeur graphique pour définir des zones sur une image de carte
class MapZoneEditor extends StatefulWidget {
  final String imagePath;
  final String mapId;
  final String mapName;
  final bool isUrl;

  const MapZoneEditor({
    super.key,
    required this.imagePath,
    required this.mapId,
    required this.mapName,
    this.isUrl = false,
  });

  @override
  State<MapZoneEditor> createState() => _MapZoneEditorState();
}

class _MapZoneEditorState extends State<MapZoneEditor> {
  final List<MapZone> _zones = [];
  final List<MapPoint> _points = [];
  final GlobalKey _imageKey = GlobalKey();
  final Size _imageSize = const Size(800, 600); // Taille par défaut
  bool _isDrawing = false;
  Offset? _startPoint;
  Offset? _currentPoint;
  MapZone? _selectedZone;
  MapPoint? _selectedPoint;
  bool _showZones = true;
  bool _showPoints = true;
  String _editMode = 'zone'; // 'zone' ou 'point'

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('📝 ${widget.mapName}'),
        backgroundColor: Colors.blue,
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
          // Sauvegarder
          IconButton(
            icon: const Icon(Icons.save),
            onPressed: _saveConfiguration,
            tooltip: 'Sauvegarder la configuration',
          ),
        ],
      ),
      body: Column(
        children: [
          // Barre d'outils
          Container(
            padding: const EdgeInsets.all(8),
            color: Colors.grey.withOpacity(0.1),
            child: Row(
              children: [
                // Mode d'édition
                SegmentedButton<String>(
                  segments: const [
                    ButtonSegment<String>(
                      value: 'zone',
                      label: Text('Zones'),
                      icon: Icon(Icons.crop_square),
                    ),
                    ButtonSegment<String>(
                      value: 'point',
                      label: Text('Points'),
                      icon: Icon(Icons.place),
                    ),
                  ],
                  selected: {_editMode},
                  onSelectionChanged: (Set<String> selection) {
                    setState(() {
                      _editMode = selection.first;
                      _selectedZone = null;
                      _selectedPoint = null;
                    });
                  },
                ),
                const SizedBox(width: 16),
                Text(
                  _editMode == 'zone' 
                    ? 'Zones: ${_zones.length}' 
                    : 'Points: ${_points.length}',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
                const SizedBox(width: 16),
                if (_editMode == 'zone' && _selectedZone != null) ...[
                  Text('Zone: ${_selectedZone!.name}'),
                  const SizedBox(width: 8),
                  IconButton(
                    icon: const Icon(Icons.edit, size: 20),
                    onPressed: () => _editZone(_selectedZone!),
                    tooltip: 'Modifier la zone',
                  ),
                  IconButton(
                    icon: const Icon(Icons.delete, size: 20),
                    onPressed: () => _deleteZone(_selectedZone!),
                    tooltip: 'Supprimer la zone',
                  ),
                ],
                if (_editMode == 'point' && _selectedPoint != null) ...[
                  Text('Point: ${_selectedPoint!.name}'),
                  const SizedBox(width: 8),
                  IconButton(
                    icon: const Icon(Icons.edit, size: 20),
                    onPressed: () {
                      // TODO: Implémenter _editPoint
                      print('Edit point: ${_selectedPoint!.name}');
                    },
                    tooltip: 'Modifier le point',
                  ),
                  IconButton(
                    icon: const Icon(Icons.delete, size: 20),
                    onPressed: () {
                      setState(() {
                        _points.remove(_selectedPoint!);
                        _selectedPoint = null;
                      });
                    },
                    tooltip: 'Supprimer le point',
                  ),
                ],
                const Spacer(),
                ElevatedButton.icon(
                  onPressed: _editMode == 'zone' ? _clearAllZones : () {
                    setState(() {
                      _points.clear();
                      _selectedPoint = null;
                    });
                  },
                  icon: const Icon(Icons.clear_all),
                  label: Text(_editMode == 'zone' ? 'Effacer zones' : 'Effacer points'),
                ),
              ],
            ),
          ),
          
          // Zone d'édition
          Expanded(
            child: Container(
              color: Colors.grey.withOpacity(0.2),
              child: Center(
                child: GestureDetector(
                  onPanStart: _onPanStart,
                  onPanUpdate: _onPanUpdate,
                  onPanEnd: _onPanEnd,
                  onTapDown: _onTapDown,
                  child: Stack(
                    children: [
                      // Image de fond
                      widget.isUrl 
                        ? Image.network(
                            widget.imagePath,
                            key: _imageKey,
                            fit: BoxFit.contain,
                            errorBuilder: (context, error, stackTrace) {
                              return Container(
                                width: 400,
                                height: 300,
                                color: Colors.grey.withOpacity(0.3),
                                child: Column(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Icon(
                                      Icons.broken_image,
                                      size: 64,
                                      color: Colors.grey,
                                    ),
                                    const SizedBox(height: 8),
                                    const Text('Image non trouvée'),
                                    Text(
                                      widget.imagePath,
                                      style: const TextStyle(fontSize: 12),
                                    ),
                                  ],
                                ),
                              );
                            },
                          )
                        : Image.asset(
                            widget.imagePath,
                            key: _imageKey,
                            fit: BoxFit.contain,
                            errorBuilder: (context, error, stackTrace) {
                          return Container(
                            width: 400,
                            height: 300,
                            color: Colors.grey.withOpacity(0.3),
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(
                                  Icons.broken_image,
                                  size: 64,
                                  color: Colors.grey,
                                ),
                                const SizedBox(height: 8),
                                const Text('Image non trouvée'),
                                Text(
                                  widget.imagePath,
                                  style: const TextStyle(fontSize: 12),
                                ),
                              ],
                            ),
                          );
                        },
                      ),
                      
                      // Zones définies
                      if (_showZones) ..._buildZoneWidgets(),
                      
                      // Points définis
                      if (_showPoints) ..._buildPointWidgets(),
                      
                      // Zone en cours de création
                      if (_isDrawing && _startPoint != null && _currentPoint != null)
                        _buildDrawingZone(),
                    ],
                  ),
                ),
              ),
            ),
          ),
          
          // Liste des zones ou points
          Container(
            height: 120,
            padding: const EdgeInsets.all(8),
            color: Colors.white,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  _editMode == 'zone' ? 'Zones définies:' : 'Points définis:',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                Expanded(
                  child: _editMode == 'zone'
                    ? _zones.isEmpty
                        ? const Center(
                            child: Text(
                              'Aucune zone définie.\nCliquez et glissez sur l\'image pour créer une zone.',
                              textAlign: TextAlign.center,
                              style: TextStyle(color: Colors.grey),
                            ),
                          )
                        : ListView.builder(
                            scrollDirection: Axis.horizontal,
                            itemCount: _zones.length,
                            itemBuilder: (context, index) {
                              final zone = _zones[index];
                              final isSelected = zone == _selectedZone;
                            return Container(
                              margin: const EdgeInsets.only(right: 8),
                              child: FilterChip(
                                label: Text(zone.name),
                                selected: isSelected,
                                onSelected: (selected) {
                                  setState(() {
                                    _selectedZone = selected ? zone : null;
                                  });
                                },
                                backgroundColor: isSelected ? Colors.blue.withOpacity(0.1) : null,
                              ),
                            );
                          },
                        )
                    : _points.isEmpty
                        ? const Center(
                            child: Text(
                              'Aucun point défini.\nCliquez sur l\'image pour créer un point.',
                              textAlign: TextAlign.center,
                              style: TextStyle(color: Colors.grey),
                            ),
                          )
                        : ListView.builder(
                            scrollDirection: Axis.horizontal,
                            itemCount: _points.length,
                            itemBuilder: (context, index) {
                              final point = _points[index];
                              final isSelected = point == _selectedPoint;
                              return Container(
                                margin: const EdgeInsets.only(right: 8),
                                child: FilterChip(
                                  label: Text(point.name),
                                  selected: isSelected,
                                  onSelected: (selected) {
                                    setState(() {
                                      _selectedPoint = selected ? point : null;
                                    });
                                  },
                                  backgroundColor: isSelected ? Colors.orange.withOpacity(0.1) : null,
                                  avatar: const Icon(Icons.place, size: 16),
                                ),
                              );
                            },
                          ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  List<Widget> _buildZoneWidgets() {
    return _zones.map((zone) {
      final isSelected = zone == _selectedZone;
      return Positioned(
        left: zone.bounds.left,
        top: zone.bounds.top,
        width: zone.bounds.width,
        height: zone.bounds.height,
        child: Container(
          decoration: BoxDecoration(
            border: Border.all(
              color: isSelected ? Colors.red : Colors.blue,
              width: isSelected ? 3 : 2,
            ),
            color: (isSelected ? Colors.red : Colors.blue).withOpacity(0.2),
          ),
          child: Stack(
            children: [
              // Nom de la zone
              Positioned(
                top: 2,
                left: 2,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
                  decoration: BoxDecoration(
                    color: isSelected ? Colors.red : Colors.blue,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    zone.name,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
              
              // Point de positionnement du pointeur
              Positioned(
                left: zone.pointerPosition.dx - zone.bounds.left - 4,
                top: zone.pointerPosition.dy - zone.bounds.top - 4,
                child: Container(
                  width: 8,
                  height: 8,
                  decoration: const BoxDecoration(
                    color: Colors.red,
                    shape: BoxShape.circle,
                  ),
                ),
              ),
            ],
          ),
        ),
      );
    }).toList();
  }

  List<Widget> _buildPointWidgets() {
    return _points.map((point) {
      final isSelected = point == _selectedPoint;
      
      // CORRECTION: Afficher le picto à la position originale du clic
      // point.position.dx contient la position convertie, mais nous voulons
      // afficher le picto là où l'utilisateur a cliqué
      final displayX = point.originalTapPosition?.dx ?? point.position.dx;
      final displayY = point.originalTapPosition?.dy ?? point.position.dy;
      
      return Positioned(
        left: displayX - 12, // Centrer le point sur la position du clic
        top: displayY - 12,
        child: GestureDetector(
          onTap: () {
            setState(() {
              _selectedPoint = point;
              _selectedZone = null;
            });
          },
          child: Container(
            width: 24,
            height: 24,
            decoration: BoxDecoration(
              color: isSelected ? Colors.red : Colors.orange,
              shape: BoxShape.circle,
              border: Border.all(
                color: Colors.white,
                width: 2,
              ),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.3),
                  blurRadius: 4,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Icon(
              Icons.place,
              color: Colors.white,
              size: 16,
            ),
          ),
        ),
      );
    }).toList();
  }

  Widget _buildDrawingZone() {
    final rect = Rect.fromPoints(_startPoint!, _currentPoint!);
    return Positioned(
      left: rect.left,
      top: rect.top,
      width: rect.width,
      height: rect.height,
      child: Container(
        decoration: BoxDecoration(
          border: Border.all(color: Colors.green, width: 2),
          color: Colors.green.withOpacity(0.2),
        ),
      ),
    );
  }

  void _onPanStart(DragStartDetails details) {
    // En mode point, on ne fait rien (la création se fait avec onTapDown)
    if (_editMode == 'point') {
      return;
    }
    
    // En mode zone, commencer le dessin
    final RenderBox? renderBox = _imageKey.currentContext?.findRenderObject() as RenderBox?;
    if (renderBox != null) {
      final localPosition = renderBox.globalToLocal(details.globalPosition);
      setState(() {
        _isDrawing = true;
        _startPoint = localPosition;
        _currentPoint = localPosition;
        _selectedZone = null;
        _selectedPoint = null;
      });
    }
  }

  void _onPanUpdate(DragUpdateDetails details) {
    if (_isDrawing) {
      final RenderBox? renderBox = _imageKey.currentContext?.findRenderObject() as RenderBox?;
      if (renderBox != null) {
        final localPosition = renderBox.globalToLocal(details.globalPosition);
        setState(() {
          _currentPoint = localPosition;
        });
      }
    }
  }

  void _onPanEnd(DragEndDetails details) {
    if (_isDrawing && _startPoint != null && _currentPoint != null) {
      final rect = Rect.fromPoints(_startPoint!, _currentPoint!);
      
      // Vérifier que la zone a une taille minimale
      if (rect.width > 20 && rect.height > 20) {
        _createZone(rect);
      }
      
      setState(() {
        _isDrawing = false;
        _startPoint = null;
        _currentPoint = null;
      });
    }
  }

  void _onTapDown(TapDownDetails details) {
    final RenderBox? renderBox = _imageKey.currentContext?.findRenderObject() as RenderBox?;
    if (renderBox != null) {
      final localPosition = renderBox.globalToLocal(details.globalPosition);
      
      if (_editMode == 'point') {
        print('🎯 TAP ÉDITEUR: Position brute = (${localPosition.dx.toStringAsFixed(1)}, ${localPosition.dy.toStringAsFixed(1)})');
        
        // En mode point, créer un point
        _createPoint(localPosition);
      } else {
        // En mode zone, désélectionner
        setState(() {
          _selectedZone = null;
          _selectedPoint = null;
        });
      }
    }
  }

  void _createZone(Rect bounds) {
    _showZoneDialog(
      title: 'Nouvelle zone',
      initialName: 'Zone ${_zones.length + 1}',
      onSave: (name, subMapIds) {
        final zone = MapZone(
          id: 'zone_${DateTime.now().millisecondsSinceEpoch}',
          name: name,
          bounds: bounds,
          pointerPosition: bounds.center, // Par défaut au centre
          subMapIds: subMapIds,
        );
        
        setState(() {
          _zones.add(zone);
          _selectedZone = zone;
        });
      },
    );
  }

  /// Convertit les coordonnées du container vers les coordonnées de l'image réelle
  Offset _convertContainerToImageCoordinates(Offset containerPosition) {
    final RenderBox? renderBox = _imageKey.currentContext?.findRenderObject() as RenderBox?;
    if (renderBox == null) return containerPosition;
    
    final layout = MapPositioningUtils.calculateImageLayout(
      containerSize: renderBox.size,
      originalImageSize: _imageSize,
    );
    
    return MapPositioningUtils.convertContainerToImageCoordinates(
      containerPosition: containerPosition,
      layout: layout,
    );
  }

  void _createPoint(Offset position) {
    // Convertir la position du container vers les coordonnées de l'image réelle
    final imagePosition = _convertContainerToImageCoordinates(position);
    
    print('🎯 CONVERSION ÉDITEUR: Container=(${position.dx.toStringAsFixed(1)}, ${position.dy.toStringAsFixed(1)}) -> Image=(${imagePosition.dx.toStringAsFixed(1)}, ${imagePosition.dy.toStringAsFixed(1)})');
    
    _showPointDialog(
      title: 'Nouveau point',
      initialName: 'Point ${_points.length + 1}',
      onSave: (name, subMapIds) {
        final point = MapPoint(
          id: 'point_${DateTime.now().millisecondsSinceEpoch}',
          name: name,
          position: imagePosition,
          subMapIds: subMapIds,
          originalTapPosition: position, // Sauvegarder la position originale du clic
        );
        
        print('🎯 POINT CRÉÉ: Position finale = (${imagePosition.dx.toStringAsFixed(1)}, ${imagePosition.dy.toStringAsFixed(1)})');
        
        setState(() {
          _points.add(point);
          _selectedPoint = point;
        });
      },
    );
  }

  void _editZone(MapZone zone) {
    _showZoneDialog(
      title: 'Modifier la zone',
      initialName: zone.name,
      initialSubMapIds: zone.subMapIds,
      onSave: (name, subMapIds) {
        setState(() {
          final index = _zones.indexOf(zone);
          if (index != -1) {
            _zones[index] = zone.copyWith(
              name: name,
              subMapIds: subMapIds,
            );
          }
        });
      },
    );
  }

  void _deleteZone(MapZone zone) {
    setState(() {
      _zones.remove(zone);
      if (_selectedZone == zone) {
        _selectedZone = null;
      }
    });
  }

  void _clearAllZones() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Confirmer'),
        content: const Text('Voulez-vous vraiment supprimer toutes les zones ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () {
              setState(() {
                _zones.clear();
                _selectedZone = null;
              });
              Navigator.pop(context);
            },
            child: const Text('Supprimer tout'),
          ),
        ],
      ),
    );
  }

  void _showZoneDialog({
    required String title,
    required String initialName,
    List<String>? initialSubMapIds,
    required Function(String name, List<String>? subMapIds) onSave,
  }) {
    final nameController = TextEditingController(text: initialName);
    final subMapController = TextEditingController(
      text: initialSubMapIds?.join(', ') ?? '',
    );

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameController,
              decoration: const InputDecoration(
                labelText: 'Nom de la zone',
                hintText: 'Ex: Port, Cocotier, Bateau...',
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: subMapController,
              decoration: const InputDecoration(
                labelText: 'Sous-cartes (optionnel)',
                hintText: 'Ex: port, coco (séparés par des virgules)',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () {
              final name = nameController.text.trim();
              if (name.isNotEmpty) {
                final subMapText = subMapController.text.trim();
                final subMapIds = subMapText.isEmpty
                    ? null
                    : subMapText.split(',').map((s) => s.trim()).where((s) => s.isNotEmpty).toList();
                
                onSave(name, subMapIds?.isEmpty == true ? null : subMapIds);
                Navigator.pop(context);
              }
            },
            child: const Text('Sauvegarder'),
          ),
        ],
      ),
    );
  }

  void _showPointDialog({
    required String title,
    required String initialName,
    List<String>? initialSubMapIds,
    required Function(String name, List<String>? subMapIds) onSave,
  }) {
    final nameController = TextEditingController(text: initialName);
    final subMapController = TextEditingController(
      text: initialSubMapIds?.join(', ') ?? '',
    );

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameController,
              decoration: const InputDecoration(
                labelText: 'Nom du point',
                hintText: 'Ex: Trésor, Épave, Village...',
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: subMapController,
              decoration: const InputDecoration(
                labelText: 'Sous-cartes (optionnel)',
                hintText: 'Ex: tresor, epave (séparés par des virgules)',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () {
              final name = nameController.text.trim();
              if (name.isNotEmpty) {
                final subMapText = subMapController.text.trim();
                final subMapIds = subMapText.isEmpty
                    ? null
                    : subMapText.split(',').map((s) => s.trim()).where((s) => s.isNotEmpty).toList();
                
                onSave(name, subMapIds?.isEmpty == true ? null : subMapIds);
                Navigator.pop(context);
              }
            },
            child: const Text('Sauvegarder'),
          ),
        ],
      ),
    );
  }

  void _saveConfiguration() {
    if (_zones.isEmpty && _points.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Aucune zone ou point à sauvegarder'),
          backgroundColor: Colors.orange,
        ),
      );
      return;
    }

    // Calculer les dimensions réelles de l'image affichée dans l'éditeur
    final RenderBox? renderBox = _imageKey.currentContext?.findRenderObject() as RenderBox?;
    Size? displayedImageSize;
    double? imageWidth, imageHeight, offsetX, offsetY;
    
    if (renderBox != null) {
      displayedImageSize = renderBox.size;
      final imageAspectRatio = _imageSize.width / _imageSize.height;
      final containerAspectRatio = displayedImageSize.width / displayedImageSize.height;
      
      if (imageAspectRatio > containerAspectRatio) {
        // L'image est limitée par la largeur
        imageWidth = displayedImageSize.width;
        imageHeight = displayedImageSize.width / imageAspectRatio;
        offsetX = 0;
        offsetY = (displayedImageSize.height - imageHeight) / 2;
      } else {
        // L'image est limitée par la hauteur
        imageWidth = displayedImageSize.height * imageAspectRatio;
        imageHeight = displayedImageSize.height;
        offsetX = (displayedImageSize.width - imageWidth) / 2;
        offsetY = 0;
      }
    }

    // Générer la configuration JSON avec informations de debug
    final config = {
      'label': widget.mapName,
      'image': {
        'type': widget.isUrl ? 'URL' : 'ASSET',
        'path': widget.imagePath,
        'size': {
          'width': _imageSize.width,
          'height': _imageSize.height,
        },
      },
      'positions': [
        ..._zones.map((zone) {
          // IMPORTANT: Normaliser par rapport à l'image ORIGINALE, pas l'image affichée
          final scaleX = imageWidth != null ? _imageSize.width / imageWidth : 1.0;
          final scaleY = imageHeight != null ? _imageSize.height / imageHeight : 1.0;
          
          final originalLeft = zone.bounds.left * scaleX;
          final originalTop = zone.bounds.top * scaleY;
          final originalRight = zone.bounds.right * scaleX;
          final originalBottom = zone.bounds.bottom * scaleY;
          
          return {
            'id': zone.id,
            'label': zone.name,
            'type': 'ZONE',
            'bounds': {
              'left': originalLeft / _imageSize.width,
              'top': originalTop / _imageSize.height,
              'right': originalRight / _imageSize.width,
              'bottom': originalBottom / _imageSize.height,
            },
          };
        }),
        ..._points.map((point) {
          // CORRECTION DÉFINITIVE: Les coordonnées du point sont déjà sur l'image affichée de l'éditeur
          // Il faut les convertir vers l'image originale puis normaliser
          
          // 1. Convertir de l'image affichée vers l'image originale
          final displayedImageWidth = imageWidth ?? _imageSize.width;
          final displayedImageHeight = imageHeight ?? _imageSize.height;
          
          // Scale pour convertir de l'image affichée vers l'image originale
          final scaleToOriginal = _imageSize.width / displayedImageWidth;
          
          // CORRECTION FINALE: Le point.position contient la position convertie de l'image
          // Cette position correspond au centre du picto dans l'éditeur
          // Nous devons la sauvegarder comme position du centre pour le viewer
          final centerX = point.position.dx;
          final centerY = point.position.dy;
          
          // Position sur l'image originale (centre du picto)
          final originalX = centerX * scaleToOriginal;
          final originalY = centerY * scaleToOriginal;
          
          // 2. Normaliser par rapport à l'image originale
          final relativeX = originalX / _imageSize.width;
          final relativeY = originalY / _imageSize.height;
          
          print('💾 SAUVEGARDE ${point.name}:');
          print('  Position absolue (image affichée éditeur): (${point.position.dx.toStringAsFixed(1)}, ${point.position.dy.toStringAsFixed(1)})');
          print('  Image affichée éditeur: ${displayedImageWidth.toStringAsFixed(1)} x ${displayedImageHeight.toStringAsFixed(1)}');
          print('  Scale vers originale: ${scaleToOriginal.toStringAsFixed(3)}');
          print('  Position absolue (image originale): (${originalX.toStringAsFixed(1)}, ${originalY.toStringAsFixed(1)})');
          print('  Image originale size: ${_imageSize.width} x ${_imageSize.height}');
          print('  Position relative: (${relativeX.toStringAsFixed(3)}, ${relativeY.toStringAsFixed(3)})');
          
          return {
            'id': point.id,
            'label': point.name,
            'type': 'POINT',
            'position': {
              'x': relativeX,
              'y': relativeY,
            },
          };
        }),
      ],
      // Informations de debug pour vérifier les calculs
      '_debug': {
        'editor_container_size': displayedImageSize != null ? {
          'width': displayedImageSize.width,
          'height': displayedImageSize.height,
        } : null,
        'editor_image_size': imageWidth != null ? {
          'width': imageWidth,
          'height': imageHeight,
        } : null,
        'editor_offset': offsetX != null ? {
          'offsetX': offsetX,
          'offsetY': offsetY,
        } : null,
        'image_aspect_ratio': _imageSize.width / _imageSize.height,
        'original_image_size': {
          'width': _imageSize.width,
          'height': _imageSize.height,
        },
      },
    };

    final jsonString = const JsonEncoder.withIndent('  ').convert(config);

    // Copier dans le presse-papiers
    Clipboard.setData(ClipboardData(text: jsonString));

    // Afficher le résultat
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('✅ Configuration sauvegardée'),
        content: SizedBox(
          width: double.maxFinite,
          height: 400,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Configuration copiée dans le presse-papiers !\n'
                'Zones créées: ${_zones.length}\n'
                'Points créés: ${_points.length}',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              const Text('JSON généré:'),
              const SizedBox(height: 8),
              Expanded(
                child: Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.grey.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(4),
                    border: Border.all(color: Colors.grey.withOpacity(0.3)),
                  ),
                  child: SingleChildScrollView(
                    child: SelectableText(
                      jsonString,
                      style: const TextStyle(
                        fontFamily: 'monospace',
                        fontSize: 12,
                      ),
                    ),
                  ),
                ),
              ),
            ],
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

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Configuration sauvegardée ! ${_zones.length} zones créées.'),
        backgroundColor: Colors.green,
      ),
    );
  }
}