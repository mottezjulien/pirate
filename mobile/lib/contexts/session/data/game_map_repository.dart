
import 'package:flutter/material.dart';

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
  final ImageDetails image;
  final Pointer? pointer;

  GameMap({
    required this.id,
    required this.image,
    this.pointer
  });

  String get imageValue => image.value;

  List<ImageObject> get imageObjects => image.objects;

  factory GameMap.fromJson(Map<String, dynamic> json) {
    Pointer? pointer;
    if(json['pointer'] != null) {
     pointer = Pointer.fromJson(json['pointer']);
    }
    return GameMap(
      id: json['id'] as String,
      image: ImageDetails.fromJson(json['image'] as Map<String, dynamic>),
      pointer: pointer,
    );
  }
}

class ImageDetails {
  final String id;
  final String label;
  final ImageData image;
  final List<ImageObject> objects;

  ImageDetails({required this.id, required this.label,
    required this.image, required this.objects});

  String get type => image.type;
  String get value => image.value;

  factory ImageDetails.fromJson(Map<String, dynamic> json) {
    final objectLists = (json['objects'] as List<dynamic>).map((object) => ImageObject.fromJson(object)).toList();
    return ImageDetails(id: json['id'] as String, label: json['label'] as String,
        image: ImageData.fromJson(json['image'] as Map<String, dynamic>), objects: objectLists);
  }
}


class Pointer {

  final ImagePosition position;
  final ImageData image;

  Pointer({required this.position, required this.image});

  factory Pointer.fromJson(Map<String, dynamic> json) {
    return Pointer(
        position: ImagePosition.fromJson(json['position'] as Map<String,dynamic>),
        image: ImageData.fromJson(json['image'] as Map<String,dynamic>));
  }

  ImageObject toImageObject() {
    return ImageObject(id: "", label:"", type: "IMAGE", position: position, image: image);
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

