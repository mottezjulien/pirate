
import 'package:mobile/contexts/game/domain/model/game_session.dart';

import '../../../generic/repository/generic_repository.dart';
import '../domain/model/game_goal.dart';
import '../game_current.dart';

class GameSessionRepository {

  static const resourcePath = '/sessions';

  Future<GameSession> createLyonPirate() async {
    GenericRepository genericRepository = GenericRepository();
    return sessionToModel(await genericRepository.post(
        path: resourcePath,
        body: {
      'templateCode': 'first' //'templateCode': 'pirate_lyon'
    }));
  }

  GameSession sessionToModel(Map<String, dynamic> json) {
    return GameSession(
      id: json['id'],
      label: json['label'],
    );
  }

  Future<List<GameGoal>> findGoals() async {
    GenericRepository genericRepository = GenericRepository();
    var response = await genericRepository.get(path: "$resourcePath/${GameSessionCurrent.sessionId}/goals/");
    return response.map((goal) => goalToModel(goal)).toList();
  }

  GameGoal goalToModel(goal) {
    return GameGoal(
      id: goal['id'],
      label: goal['label'],
      state: GameGoalState.values.firstWhere((e) => e.toString() == goal['state']),
      targets: goal['targets'].map((target) => GameTarget(id: target['id'], label: target['label'])).toList()
    );
  }

}