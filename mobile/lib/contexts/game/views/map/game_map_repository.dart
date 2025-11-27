import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class GameMapRepository {
  static const String _assetPath = 'assets/pouet/map.json';

  Future<List<GameMap>> get() async {
    final String jsonString = await rootBundle.loadString(_assetPath);
    final List<dynamic> jsonList = jsonDecode(jsonString);
    return jsonList.map((json) => GameMap.fromJson(json)).toList();
  }
}

class GameMap {
  final String id;
  final String label;
  final ImageData image;
  final List<MapItem> items;

  GameMap({
    required this.id,
    required this.label,
    required this.image,
    required this.items,
  });

  factory GameMap.fromJson(Map<String, dynamic> json) {
    final itemsList = (json['items'] as List<dynamic>?)
            ?.map((item) => MapItem.fromJson(item))
            .toList() ??
        [];

    return GameMap(
      id: json['id'] as String,
      label: json['label'] as String,
      image: ImageData.fromJson(json['image'] as Map<String, dynamic>),
      items: itemsList,
    );
  }
}

class MapItem {
  final String id;
  final String label;
  final String type;
  final Position position;
  final PointData? point;
  final ImageData? image;

  MapItem({
    required this.id,
    required this.label,
    required this.type,
    required this.position,
    this.point,
    this.image,
  });

  factory MapItem.fromJson(Map<String, dynamic> json) {
    final type = json['type'] as String;
    return MapItem(
      id: json['id'] as String,
      label: json['label'] as String,
      type: type,
      position: Position.fromJson(json['position'] as Map<String, dynamic>),
      point: type == 'POINT' ? PointData.fromJson(json['point'] as Map<String, dynamic>) : null,
      image: type == 'IMAGE' ? ImageData.fromJson(json['image'] as Map<String, dynamic>) : null,
    );
  }
}

class Position {
  final double top;
  final double left;

  Position({required this.top, required this.left});

  factory Position.fromJson(Map<String, dynamic> json) {
    return Position(
      top: (json['top'] as num).toDouble(),
      left: (json['left'] as num).toDouble(),
    );
  }
}

class PointData {
  final Color color;

  PointData({required this.color});

  factory PointData.fromJson(Map<String, dynamic> json) {
    final colorString = (json['color'] as String).toLowerCase();
    return PointData(
      color: _parseColor(colorString),
    );
  }
}

class ImageData {
  final String type;
  final String value;

  ImageData({required this.type, required this.value});

  factory ImageData.fromJson(Map<String, dynamic> json) {
    return ImageData(
      type: json['type'] as String,
      value: json['value'] as String,
    );
  }
}

Color _parseColor(String colorString) {
  const Map<String, Color> colors = {
    'red': Colors.red,
    'blue': Colors.blue,
    'green': Colors.green,
    'yellow': Colors.yellow,
    'orange': Colors.orange,
    'purple': Colors.purple,
    'pink': Colors.pink,
    'cyan': Colors.cyan,
    'white': Colors.white,
    'black': Colors.black,
    'grey': Colors.grey,
    'gray': Colors.grey,
  };

  return colors[colorString] ?? Colors.red;
}
