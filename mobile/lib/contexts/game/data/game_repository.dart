
import 'package:mobile/contexts/game/domain/model/game.dart';

import '../../../generic/repository/generic_repository.dart';

class GameRepository {

  static const resourcePath = '/games';

  Game createLyonPirate() {

    GenericRepository genericRepository = GenericRepository();

    return toModel(genericRepository.post(
        resourcePath: resourcePath,
        body: {
      'templateCode': 'pirata_lyon'
    }));
  }

  Game toModel(Map<String, dynamic> json) {
    return Game(
      id: json['id'],
      label: json['label'],
    );
  }

}