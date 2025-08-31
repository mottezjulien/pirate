import 'package:flutter/material.dart';

/// Modèle pour une carte de jeu
class GameMapData {
  final String label;
  final GameMapImage image;
  final List<GameMapPosition> positions;
  final Map<String, dynamic>? debug;

  GameMapData({
    required this.label,
    required this.image,
    required this.positions,
    this.debug,
  });

  factory GameMapData.fromJson(Map<String, dynamic> json) {
    return GameMapData(
      label: json['label'],
      image: GameMapImage.fromJson(json['image']),
      positions: (json['positions'] as List)
          .map((pos) => GameMapPosition.fromJson(pos))
          .toList(),
      debug: json['_debug'],
    );
  }
}

/// Modèle pour l'image d'une carte
class GameMapImage {
  final String type;
  final String path;
  final GameMapSize size;

  GameMapImage({
    required this.type,
    required this.path,
    required this.size,
  });

  factory GameMapImage.fromJson(Map<String, dynamic> json) {
    return GameMapImage(
      type: json['type'],
      path: json['path'],
      size: GameMapSize.fromJson(json['size']),
    );
  }
}

/// Modèle pour la taille d'une image
class GameMapSize {
  final double width;
  final double height;

  GameMapSize({
    required this.width,
    required this.height,
  });

  factory GameMapSize.fromJson(Map<String, dynamic> json) {
    return GameMapSize(
      width: json['width'].toDouble(),
      height: json['height'].toDouble(),
    );
  }
}

/// Modèle pour une position (zone ou point) sur la carte
class GameMapPosition {
  final String id;
  final String label;
  final String type;
  final Offset? position;
  final Rect? bounds;

  GameMapPosition({
    required this.id,
    required this.label,
    required this.type,
    this.position,
    this.bounds,
  });

  factory GameMapPosition.fromJson(Map<String, dynamic> json) {
    Offset? position;
    Rect? bounds;

    if (json['type'] == 'POINT' && json['position'] != null) {
      final pos = json['position'];
      position = Offset(pos['x'].toDouble(), pos['y'].toDouble());
    } else if (json['type'] == 'ZONE' && json['bounds'] != null) {
      final b = json['bounds'];
      bounds = Rect.fromLTRB(
        b['left'].toDouble(),
        b['top'].toDouble(),
        b['right'].toDouble(),
        b['bottom'].toDouble(),
      );
    }

    return GameMapPosition(
      id: json['id'],
      label: json['label'],
      type: json['type'],
      position: position,
      bounds: bounds,
    );
  }

  bool get isPoint => type == 'POINT';
  bool get isZone => type == 'ZONE';
}