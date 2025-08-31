import 'dart:ui';

/// Position relative sur une carte (coordonnées 0-1)
class MapRelativePosition {
  final double x;
  final double y;
  
  const MapRelativePosition(this.x, this.y);
  
  /// Convertit en Offset
  Offset toOffset() => Offset(x, y);
  
  /// Crée depuis un Offset
  factory MapRelativePosition.fromOffset(Offset offset) {
    return MapRelativePosition(offset.dx, offset.dy);
  }
  
  /// Crée depuis des coordonnées absolues
  factory MapRelativePosition.fromAbsolute({
    required Offset absolutePosition,
    required Size imageSize,
  }) {
    return MapRelativePosition(
      absolutePosition.dx / imageSize.width,
      absolutePosition.dy / imageSize.height,
    );
  }
  
  /// Convertit en coordonnées absolutes
  Offset toAbsolute(Size imageSize) {
    return Offset(x * imageSize.width, y * imageSize.height);
  }
  
  @override
  String toString() => 'MapRelativePosition($x, $y)';
  
  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is MapRelativePosition && other.x == x && other.y == y;
  }
  
  @override
  int get hashCode => Object.hash(x, y);
}

/// Bounds relatifs sur une carte (coordonnées 0-1)
class MapRelativeBounds {
  final double left;
  final double top;
  final double right;
  final double bottom;
  
  const MapRelativeBounds({
    required this.left,
    required this.top,
    required this.right,
    required this.bottom,
  });
  
  /// Largeur relative
  double get width => right - left;
  
  /// Hauteur relative
  double get height => bottom - top;
  
  /// Centre relatif
  MapRelativePosition get center => MapRelativePosition(
    left + width / 2,
    top + height / 2,
  );
  
  /// Convertit en Rect
  Rect toRect() => Rect.fromLTRB(left, top, right, bottom);
  
  /// Crée depuis un Rect
  factory MapRelativeBounds.fromRect(Rect rect) {
    return MapRelativeBounds(
      left: rect.left,
      top: rect.top,
      right: rect.right,
      bottom: rect.bottom,
    );
  }
  
  /// Crée depuis des coordonnées absolues
  factory MapRelativeBounds.fromAbsolute({
    required Rect absoluteBounds,
    required Size imageSize,
  }) {
    return MapRelativeBounds(
      left: absoluteBounds.left / imageSize.width,
      top: absoluteBounds.top / imageSize.height,
      right: absoluteBounds.right / imageSize.width,
      bottom: absoluteBounds.bottom / imageSize.height,
    );
  }
  
  /// Convertit en coordonnées absolues
  Rect toAbsolute(Size imageSize) {
    return Rect.fromLTRB(
      left * imageSize.width,
      top * imageSize.height,
      right * imageSize.width,
      bottom * imageSize.height,
    );
  }
  
  @override
  String toString() => 'MapRelativeBounds(left: $left, top: $top, right: $right, bottom: $bottom)';
  
  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is MapRelativeBounds && 
           other.left == left && 
           other.top == top && 
           other.right == right && 
           other.bottom == bottom;
  }
  
  @override
  int get hashCode => Object.hash(left, top, right, bottom);
}