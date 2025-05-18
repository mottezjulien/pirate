import 'package:geolocator/geolocator.dart';

import '../../../geo/domain/model/coordinate.dart';
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

  Coordinate get coordinate => gameLocation.coordinate;

  Stream<Coordinate> get streamCoordinate => gameLocation.stream;

}

class GameLocation {

  late Stream<Position> _streamPosition;
  Position? last;

  void init() {
    const LocationSettings locationSettings = LocationSettings(accuracy: LocationAccuracy.bestForNavigation);
    _streamPosition = Geolocator.getPositionStream(locationSettings: locationSettings);
    _streamPosition.listen((position) {
      onMove(position);
    });
  }


  void onMove(Position position) {
    if(last == null || last != position) { //TODO IF MOVE IS ENOUGH
      last = position;
      _fireMove(position);
    }
  }

  void _fireMove(Position position) {
    //REST -> WebSockets ???
    GameSessionRepository repository = GameSessionRepository();
    repository.move(Coordinate(lat: position.latitude, lng: position.longitude));
  }

  Coordinate get coordinate => Coordinate(lat: last!.latitude, lng: last!.longitude);

  Stream<Coordinate> get stream => _streamPosition
      .map((position) => Coordinate(lat: position.latitude, lng: position.longitude));

}
