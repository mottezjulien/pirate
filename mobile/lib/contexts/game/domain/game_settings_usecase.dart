import 'package:geolocator/geolocator.dart';

import '../data/game_repository.dart';
import '../game_current.dart';

class GameSettingsUseCase {

  String exceptionGeolocationPermission = 'Geolocation permission not granted';

  Future<void> apply() async {
    
    await checkGeoLocalizationPermission();

    await initGame();
    //await loadGame();
    
    /*Geolocator.checkPermission()
        .then((permissionStatus) async {
      if(hasPermission(permissionStatus)) {
        await Geolocator.requestPermission();
      }
      const LocationSettings locationSettings = LocationSettings(accuracy: LocationAccuracy.bestForNavigation);
      this.streamPosition = Geolocator.getPositionStream(locationSettings: locationSettings)
          .listen((position) {
        print(position.latitude);
        print(position.longitude);
        positionValueNotifier.value = position;
      });
    });*/
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
    GameCurrent.game = repository.createLyonPirate();
  }

}