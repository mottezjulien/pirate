
import '../../../generic/repository/generic_repository.dart';
import '../game_current.dart';

class GameImageRepository {

  static const resourcePath = '/sessions';

  Future<void> clickObject(String imageId, String objectId) async {
    GenericRepository genericRepository = GenericRepository();
    await genericRepository.post(
        path: "$resourcePath/${GameCurrent.sessionId}/images/${imageId}/objects/${objectId}",
        decode: false
    );
  }
}

