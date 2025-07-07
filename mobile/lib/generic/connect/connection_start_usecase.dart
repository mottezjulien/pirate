
import '../config/device.dart';
import 'connection_current.dart';
import 'connection_repository.dart';

class ConnectionStartUseCase {
  
  final String exceptionConnectionFailed = 'Connection not started';

  Future<void> apply() async {
    try {
      final ConnectionRepository repository = ConnectionRepository();

      ConnectionCurrent.auth = await repository.byDeviceId(
          deviceId: await Device.id()
      );
    } catch(e) {
      throw Exception(exceptionConnectionFailed);
    }
  }

}