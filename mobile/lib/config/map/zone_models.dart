import 'package:flutter/material.dart';

/// Modèle représentant une zone sur une carte
class MapZone {
  final String id;
  final String name;
  final Rect bounds;
  final Offset pointerPosition;
  final List<String>? subMapIds;

  const MapZone({
    required this.id,
    required this.name,
    required this.bounds,
    required this.pointerPosition,
    this.subMapIds,
  });

  /// Vérifie si un point est dans cette zone
  bool containsPoint(Offset point) {
    return bounds.contains(point);
  }

  /// Crée une copie avec des modifications
  MapZone copyWith({
    String? id,
    String? name,
    Rect? bounds,
    Offset? pointerPosition,
    List<String>? subMapIds,
  }) {
    return MapZone(
      id: id ?? this.id,
      name: name ?? this.name,
      bounds: bounds ?? this.bounds,
      pointerPosition: pointerPosition ?? this.pointerPosition,
      subMapIds: subMapIds ?? this.subMapIds,
    );
  }

  /// Convertit en JSON pour la sauvegarde
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'label': name,
      'bounds': {
        'left': bounds.left,
        'top': bounds.top,
        'right': bounds.right,
        'bottom': bounds.bottom,
      },
      'pointerPosition': {
        'dx': pointerPosition.dx,
        'dy': pointerPosition.dy,
      },
      if (subMapIds != null) 'subMapIds': subMapIds,
    };
  }

  /// Crée depuis JSON
  factory MapZone.fromJson(Map<String, dynamic> json) {
    final boundsData = json['bounds'] as Map<String, dynamic>;
    final positionData = json['pointerPosition'] as Map<String, dynamic>;
    
    return MapZone(
      id: json['id'],
      name: json['name'],
      bounds: Rect.fromLTRB(
        boundsData['left'].toDouble(),
        boundsData['top'].toDouble(),
        boundsData['right'].toDouble(),
        boundsData['bottom'].toDouble(),
      ),
      pointerPosition: Offset(
        positionData['dx'].toDouble(),
        positionData['dy'].toDouble(),
      ),
      subMapIds: json['subMapIds'] != null 
        ? List<String>.from(json['subMapIds'])
        : null,
    );
  }

  @override
  String toString() {
    return 'MapZone(id: $id, name: $name, bounds: $bounds, subMaps: $subMapIds)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is MapZone && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}

/// Modèle représentant un point sur une carte
class MapPoint {
  final String id;
  String name;
  Offset position;
  List<String> subMapIds;
  Offset? originalTapPosition; // Position originale du clic pour l'affichage

  MapPoint({
    required this.id,
    required this.name,
    required this.position,
    List<String>? subMapIds,
    this.originalTapPosition,
  }) : subMapIds = subMapIds ?? [];

  /// Vérifie si un point de clic est proche de ce point (dans un rayon de 20 pixels)
  bool isNear(Offset clickPoint, {double radius = 20.0}) {
    final distance = (position - clickPoint).distance;
    return distance <= radius;
  }

  /// Convertit en JSON pour la sauvegarde
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'label': name,
      'position': {
        'dx': position.dx,
        'dy': position.dy,
      },
      if (subMapIds.isNotEmpty) 'subMapIds': subMapIds,
    };
  }

  @override
  String toString() {
    return 'MapPoint(id: $id, name: $name, position: $position)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is MapPoint && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}

/// Configuration complète d'une carte avec ses zones
class MapConfiguration {
  final String mapId;
  final String mapName;
  final String imagePath;
  final Size imageSize;
  final List<MapZone> zones;

  const MapConfiguration({
    required this.mapId,
    required this.mapName,
    required this.imagePath,
    required this.imageSize,
    required this.zones,
  });

  /// Trouve une zone par son ID
  MapZone? findZoneById(String zoneId) {
    try {
      return zones.firstWhere((zone) => zone.id == zoneId);
    } catch (e) {
      return null;
    }
  }

  /// Trouve la zone qui contient un point donné
  MapZone? findZoneContaining(Offset point) {
    for (final zone in zones) {
      if (zone.containsPoint(point)) {
        return zone;
      }
    }
    return null;
  }

  /// Convertit en JSON
  Map<String, dynamic> toJson() {
    return {
      'mapId': mapId,
      'mapName': mapName,
      'imagePath': imagePath,
      'imageSize': {
        'width': imageSize.width,
        'height': imageSize.height,
      },
      'zones': zones.map((zone) => zone.toJson()).toList(),
    };
  }

  /// Crée depuis JSON
  factory MapConfiguration.fromJson(Map<String, dynamic> json) {
    final sizeData = json['imageSize'] as Map<String, dynamic>;
    final zonesData = json['zones'] as List<dynamic>;
    
    return MapConfiguration(
      mapId: json['mapId'],
      mapName: json['mapName'],
      imagePath: json['imagePath'],
      imageSize: Size(
        sizeData['width'].toDouble(),
        sizeData['height'].toDouble(),
      ),
      zones: zonesData
          .map((zoneData) => MapZone.fromJson(zoneData as Map<String, dynamic>))
          .toList(),
    );
  }

  @override
  String toString() {
    return 'MapConfiguration(mapId: $mapId, zones: ${zones.length})';
  }
}