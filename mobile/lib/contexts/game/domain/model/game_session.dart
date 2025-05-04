import 'dart:developer';

import 'package:geolocator/geolocator.dart';

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
/*

 */

}

class GameLocation {

  late Stream<Position> streamPosition;
  Position? last;

  void init() {
    const LocationSettings locationSettings = LocationSettings(accuracy: LocationAccuracy.bestForNavigation);
    streamPosition = Geolocator.getPositionStream(locationSettings: locationSettings);
    streamPosition.listen((position) {
      print(position.latitude);
      print(position.longitude);
      onMove(position);
      /*if(game.isPlaying()) {
          repository.move(Coord(lat: position.latitude, lng: position.longitude));
        }*/
    });
  }


  void onMove(Position position) {
    if(last == null || last != position) {
      last = position;
      fireMove(position);
    }
  }

  void fireMove(Position position) {
    log("fireMove");
  }



}