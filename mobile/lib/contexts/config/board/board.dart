
import '../../geo/domain/model/coordinate.dart';


class Board {
  final List<BoardSpace> spaces;

  Board({required this.spaces});

  List<BoardSpace> fromCoordinate({required Coordinate coordinate}) {
    return spaces
        .where((space) => space.contains(coordinate))
        .toList();
  }

}

/// Represents a geographic zone (board space) where events can be triggered
class BoardSpace {
  final String id;
  final String label;
  final List<BoardRectangle> rectangles;

  BoardSpace({required this.id, required this.label, required this.rectangles});

  factory BoardSpace.fromJson(Map<String, dynamic> json) {
    return BoardSpace(
      id: json['id'] as String,
      label: json['label'] as String? ?? '',
      rectangles: (json['rectangles'] as List)
          .map((r) => BoardRectangle.fromJson(r))
          .toList(),
    );
  }

  /// Check if a coordinate is inside any of this space's rectangles
  bool contains(Coordinate coordinate) {
    return rectangles.any((rect) => rect.contains(coordinate));
  }
}


/// Represents a GPS rectangle (bounding box)
class BoardRectangle {
  final Coordinate bottomLeft;
  final Coordinate topRight;

  BoardRectangle({required this.bottomLeft, required this.topRight});

  factory BoardRectangle.fromJson(Map<String, dynamic> json) {
    final bl = json['bottomLeft'];
    final tr = json['topRight'];
    return BoardRectangle(
      bottomLeft: Coordinate(
        lat: (bl['lat'] as num).toDouble(),
        lng: (bl['lng'] as num).toDouble(),
      ),
      topRight: Coordinate(
        lat: (tr['lat'] as num).toDouble(),
        lng: (tr['lng'] as num).toDouble(),
      ),
    );
  }

  /// Check if a coordinate is inside this rectangle
  bool contains(Coordinate coordinate) {
    return coordinate.lat >= bottomLeft.lat &&
        coordinate.lat <= topRight.lat &&
        coordinate.lng >= bottomLeft.lng &&
        coordinate.lng <= topRight.lng;
  }
}