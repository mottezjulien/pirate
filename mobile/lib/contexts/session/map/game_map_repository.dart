
import 'package:latlong2/latlong.dart';

import '../../../generic/repository/generic_repository.dart';
import '../game_current.dart';

class GameMapRepository {

  static const resourcePath = '/sessions';

  Future<List<GameMap>> get() async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    final response = await genericRepository.get(path: "$resourcePath/${GameCurrent.sessionId}/maps/");
    final List<GameMap> maps = [];
    response.forEach((jsonMap) {
      maps.add(GameMap.fromJson(jsonMap));
    });
    return maps;
  }
}

class GameMap {

  final String id;
  final String label;
  final MapImage image;
  final MapBounds bounds;
  final MapImage? pointer;
  final List<MapObject> objects;

  GameMap({
    required this.id,
    required this.label,
    required this.image,
    required this.bounds,
    this.pointer,
    required this.objects,
  });

  String get imageValue => image.value;

  factory GameMap.fromJson(Map<String, dynamic> json) {
    MapImage? pointer;
    if (json['pointer'] != null) {
      pointer = MapImage.fromJson(json['pointer']);
    }

    List<MapObject> objects = [];
    if (json['objects'] != null) {
      objects = (json['objects'] as List)
          .map((o) => MapObject.fromJson(o))
          .toList();
    }

    return GameMap(
      id: json['id'] as String,
      label: json['label'] as String? ?? '',
      image: MapImage.fromJson(json['image'] as Map<String, dynamic>),
      bounds: MapBounds.fromJson(json['bounds'] as Map<String, dynamic>),
      pointer: pointer,
      objects: objects,
    );
  }
}

class MapImage {
  final String type;
  final String value;

  MapImage({required this.type, required this.value});

  factory MapImage.fromJson(Map<String, dynamic> json) {
    return MapImage(
      type: json['type'] as String,
      value: json['value'] as String,
    );
  }

  bool get isAsset => type == 'ASSET';
  bool get isWeb => type == 'WEB';
}

class MapBounds {
  final LatLng bottomLeft;
  final LatLng topRight;

  MapBounds({required this.bottomLeft, required this.topRight});

  factory MapBounds.fromJson(Map<String, dynamic> json) {
    final bl = json['bottomLeft'];
    final tr = json['topRight'];
    return MapBounds(
      bottomLeft: LatLng(
        (bl['lat'] as num).toDouble(),
        (bl['lng'] as num).toDouble(),
      ),
      topRight: LatLng(
        (tr['lat'] as num).toDouble(),
        (tr['lng'] as num).toDouble(),
      ),
    );
  }

  /// Check if a position is within these bounds
  bool contains(LatLng position) {
    return position.latitude >= bottomLeft.latitude &&
        position.latitude <= topRight.latitude &&
        position.longitude >= bottomLeft.longitude &&
        position.longitude <= topRight.longitude;
  }

  /// Get the center of the bounds
  LatLng get center => LatLng(
    (bottomLeft.latitude + topRight.latitude) / 2,
    (bottomLeft.longitude + topRight.longitude) / 2,
  );
}

class MapObject {
  final String id;
  final String label;
  final String type; // "POINT" or "IMAGE"
  final LatLng position;
  final String? color; // for POINT type
  final MapImage? image; // for IMAGE type

  MapObject({
    required this.id,
    required this.label,
    required this.type,
    required this.position,
    this.color,
    this.image,
  });

  factory MapObject.fromJson(Map<String, dynamic> json) {
    final pos = json['position'];
    return MapObject(
      id: json['id'] as String,
      label: json['label'] as String? ?? '',
      type: json['type'] as String,
      position: LatLng(
        (pos['lat'] as num).toDouble(),
        (pos['lng'] as num).toDouble(),
      ),
      color: json['color'] as String?,
      image: json['image'] != null
          ? MapImage.fromJson(json['image'] as Map<String, dynamic>)
          : null,
    );
  }

  bool get isPoint => type == 'POINT';
  bool get isImage => type == 'IMAGE';
}
