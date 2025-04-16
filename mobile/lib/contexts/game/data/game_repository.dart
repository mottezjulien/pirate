
import 'package:mobile/contexts/game/domain/model/game.dart';

import '../../../generic/repository/generic_repository.dart';

class GameRepository {

  static const resourcePath = '/games';

  Future<Game> createLyonPirate() async {

    GenericRepository genericRepository = GenericRepository();

    return toModel(await genericRepository.post(
        resourcePath: resourcePath,
        body: {
      //'templateCode': 'pirate_lyon'
      'templateCode': 'first'
    }));
  }

  Game toModel(Map<String, dynamic> json) {
    return Game(
      id: json['id'],
      label: json['label'],
    );
  }

}