
import 'dart:ui';

import 'package:flutter/material.dart';

import '../../../generic/repository/generic_repository.dart';
import '../game_current.dart';

class GameMapRepository {

  static const resourcePath = '/sessions';

  Future<List<GameMap>> get() async {
    GenericRepository genericRepository = GenericRepository();
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
  final ImageDetails image;
  final ImageObject? pointer;

  GameMap({
    required this.id,
    required this.image,
    this.pointer
  });

  String get imageValue => image.value;

  List<ImageObject> get imageObjects => image.objects;

  factory GameMap.fromJson(Map<String, dynamic> json) {
    ImageObject? pointer;
    if(json['pointer'] != null) {
     pointer = ImageObject.fromJson(json['pointer']);
    }
    return GameMap(
      id: json['id'] as String,
      image: ImageDetails.fromJson(json['image'] as Map<String, dynamic>),
      pointer: pointer,
    );
  }

}



class ImageDetails {

  final String label;
  final ImageData image;
  final List<ImageObject> objects;

  ImageDetails({required this.label, required this.image, required this.objects});

  String get type => image.type;
  String get value => image.value;

  factory ImageDetails.fromJson(Map<String, dynamic> json) {
    final objectLists = (json['objects'] as List<dynamic>).map((object) => ImageObject.fromJson(object)).toList();
    return ImageDetails(label: json['label'] as String,
        image: ImageData.fromJson(json['image'] as Map<String, dynamic>), objects: objectLists);
  }


}


class ImageData {
  final String type;
  final String value;
  ImageData({required this.type, required this.value});

  factory ImageData.fromJson(Map<String, dynamic> json) {
    return ImageData(type: json['type'] as String, value: json['value'] as String);
  }
}

class ImageObject {

  final String id;
  final String label;
  final String type;
  final ImagePosition position;
  final ImagePoint? point;
  final ImageData? image;

  ImageObject({required this.id, required this.label, required this.type, required this.position, this.point, this.image});


  factory ImageObject.fromJson(Map<String, dynamic> json) {
    return ImageObject(
        id: json['id'] as String,
        label: json['label'] as String,
        type: json['type'] as String,
        position: ImagePosition.fromJson(json['position'] as Map<String,dynamic>),
        point: json['point'] != null ? ImagePoint(color: _parseColor(json['point']['color'])) : null,
        image: json['image'] != null ? ImageData.fromJson(json['image'] as Map<String,dynamic>) : null);
  }

  static Color _parseColor(String? colorString) {
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

}

class ImagePosition {
  final double top, left;
  ImagePosition({required this.top, required this.left});
  factory ImagePosition.fromJson(Map<String, dynamic> json) {
    return ImagePosition(top: json['top'], left: json['left']);
  }
}

class ImagePoint {
  final Color color;
  ImagePoint({required this.color});
}


/*class MapItem {
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

*/
