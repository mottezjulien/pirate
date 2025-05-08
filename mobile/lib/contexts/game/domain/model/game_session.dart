import 'package:geolocator/geolocator.dart';

import '../../../geo/domain/model/coord.dart';
import '../../data/game_repository.dart';

class GameSession {

  final String _id, _label;
  late GameLocation gameLocation;

  GameSession({required String id, required String label})
      : _id = id, _label = label;

  String get id => _id;

  String get label => _label;

  void init() {
    gameLocation = GameLocation();
    gameLocation.init();
  }

}

class GameLocation {

  late Stream<Position> streamPosition;
  Position? last;

  void init() {
    const LocationSettings locationSettings = LocationSettings(accuracy: LocationAccuracy.bestForNavigation);
    streamPosition = Geolocator.getPositionStream(locationSettings: locationSettings);
    streamPosition.listen((position) {
      onMove(position);
    });
  }


  void onMove(Position position) {
    if(last == null || last != position) { //TODO IF MOVE IS ENOUGH
      last = position;
      fireMove(position);
    }
  }

  void fireMove(Position position) {
    //REST -> WebSockets ???
    GameSessionRepository repository = GameSessionRepository();
    repository.move(Coordinate(lat: position.latitude, lng: position.longitude));
  }



}