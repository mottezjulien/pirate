
import 'package:firebase_messaging/firebase_messaging.dart';

import '../config/device.dart';
import 'connection_current.dart';
import 'connection_repository.dart';

class ConnectionStartUseCase {
  
  final String exceptionConnectionFailed = 'Connection not started';

  Future<void> apply() async {
    try {
      final ConnectionRepository repository = ConnectionRepository();

      final String firebaseToken = await FirebaseMessaging.instance.getToken() ?? '';

      ConnectionCurrent.auth = await repository.byDeviceId(
          deviceId: await Device.id(),
          firebaseToken: firebaseToken
      );
    } catch(e) {
      throw Exception(exceptionConnectionFailed);
    }
  }

}