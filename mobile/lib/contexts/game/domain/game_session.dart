import 'package:geolocator/geolocator.dart';

import '../data/game_repository.dart';
import '../game_current.dart';
import 'model/game_session.dart';

class GameSessionUseCase {

  String exceptionGeolocationPermission = 'Geolocation permission not granted';

  Future<void> start() async {
    await checkGeoLocalizationPermission();
    await startSession();
  }

  checkGeoLocalizationPermission() async {
    if(!_hasPermission(await Geolocator.checkPermission())) {
      if(!_hasPermission(await Geolocator.requestPermission())) {
        throw Exception(exceptionGeolocationPermission);
      }
    }
  }

  bool _hasPermission(LocationPermission status) {
    return status == LocationPermission.whileInUse
        || status == LocationPermission.always;
  }

  startSession() async {
    final GameSessionRepository repository = GameSessionRepository();
    final GameSession session = await repository.create();
    session.init();
    GameCurrent.session = session;
  }

}