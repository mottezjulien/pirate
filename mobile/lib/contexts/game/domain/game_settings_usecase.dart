import 'package:geolocator/geolocator.dart';

import '../data/game_repository.dart';
import '../game_current.dart';

class GameCreateUseCase {

  String exceptionGeolocationPermission = 'Geolocation permission not granted';

  Future<void> apply() async {
    await checkGeoLocalizationPermission();
    await initGame();
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

  initGame() async {
    final GameRepository repository = GameRepository();
    GameCurrent.game = await repository.createLyonPirate();
  }

}