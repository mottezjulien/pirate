
import '../../../generic/repository/generic_repository.dart';
import '../game_current.dart';
import '../image/game_image_repository.dart';

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
