import 'package:flutter/material.dart';

class ConfigPoint {
  final String id;
  final double top;
  final double left;
  final Color color;

  ConfigPoint({
    required this.id,
    required this.top,
    required this.left,
    this.color = Colors.red,
  });
}

class ConfigImage {
  final String assetPath;
  final List<ConfigPoint> points;

  ConfigImage({
    required this.assetPath,
    this.points = const [],
  });

  ConfigImage copyWith({List<ConfigPoint>? points}) {
    return ConfigImage(
      assetPath: assetPath,
      points: points ?? this.points,
    );
  }
}
