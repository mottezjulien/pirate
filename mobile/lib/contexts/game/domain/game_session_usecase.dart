import 'package:geolocator/geolocator.dart';
import 'package:mobile/generic/dialog.dart';

import '../data/game_repository.dart';
import '../game_current.dart';
import 'model/game_session.dart';

class GameSessionUseCase {

  Future<void> start() async {
    await checkGeoLocalizationPermission();
    await startSession();
  }

  checkGeoLocalizationPermission() async {
    if(!_hasPermission(await Geolocator.checkPermission())) {
      if(!_hasPermission(await Geolocator.requestPermission())) {
        throw Exception('Geolocation permission not granted');
      }
    }
  }

  bool _hasPermission(LocationPermission status) {
    return status == LocationPermission.whileInUse
        || status == LocationPermission.always;
  }

  startSession() async {
    Dialog dialog = new Dialog();
    dialog.showMessage(
        message: "Session de jeux en cours de preparation :)",
        isClosable: false
    );
    final GameSessionRepository repository = GameSessionRepository();
    final GameSession session = await repository.create();
    GameCurrent.session = session;
    await session.init();
    repository.start();
  }

  Future<void> stop() async {
    GameCurrent.stopSession();
    final GameSessionRepository repository = GameSessionRepository();
    await repository.stop();
    GameCurrent.removeSession();
  }

}