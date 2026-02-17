import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/cupertino.dart';
import 'package:geolocator/geolocator.dart';
import 'package:mobile/generic/components/dialog.dart';

import '../../../generic/app_current.dart';
import '../data/game_repository.dart';
import '../game_current.dart';
import 'foreground_task_handler.dart';

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

  Future<void> startSession() async {

    final Dialog dialog = Dialog();
    final BuildContext? dialogContext = dialog.showMessage(message: "config.preparing.label".tr(),  isClosable: false);
    final GameSessionRepository repository = GameSessionRepository();
    final GameSessionResponseDTO sessionCreate = await repository.create();
    GameCurrent.session = sessionCreate.session;
    AppCurrent.gameSessionAuth = sessionCreate.auth;
    await sessionCreate.session.init();
    await ForegroundTaskHandler.start();
    await repository.start();
    if(dialogContext != null) {
      Navigator.pop(dialogContext);
    }
  }

  Future<void> stop() async {
    GameCurrent.stopSession();
    await ForegroundTaskHandler.stop();
    final GameSessionRepository repository = GameSessionRepository();
    await repository.stop();
    GameCurrent.removeSession();
  }

}