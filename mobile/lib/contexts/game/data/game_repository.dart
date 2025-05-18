
import 'package:mobile/contexts/game/domain/model/game_session.dart';
import 'package:mobile/contexts/geo/domain/model/coordinate.dart';

import '../../../generic/repository/generic_repository.dart';
import '../domain/model/game_goal.dart';
import '../game_current.dart';
import '../views/map/game_map_view.dart';

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


  Future<void> move(Coordinate coordinate) async {
    GenericRepository genericRepository = GenericRepository();
    var path = "$resourcePath/${GameSessionCurrent.sessionId}/move/";
    await genericRepository.post(path: path,
        body: {
          'lat': coordinate.lat,
          'lng': coordinate.lng
        }, decode: false);
  }

  Future<List<GameGoal>> findGoals() async {
    GenericRepository genericRepository = GenericRepository();
    var response = await genericRepository.get(path: "$resourcePath/${GameSessionCurrent.sessionId}/goals/");

    List<GameGoal> goals = [];
    response.forEach((goal) {
      goals.add(goalToModel(goal));
    });
    return goals;
  }

  GameGoal goalToModel(goal) {
    List<GameTarget> targets = [];
    goal['targets'].forEach((target) {
      targets.add(GameTarget(id: target['id'], label: target['label']));
    });
    return GameGoal(
      id: goal['id'],
      label: goal['label'],
      //state: GameGoalState.values.firstWhere((e) => e.name == goal['state']),
      state: GameGoalState.fromJson(goal['state']),
      targets: targets
    );
  }

  Future<List<GameMap>> findMaps() async {
    GenericRepository genericRepository = GenericRepository();
    var response = await genericRepository.get(path: "$resourcePath/${GameSessionCurrent.sessionId}/maps/");

    List<GameMap> maps = [];
    response.forEach((map) {
      maps.add(mapToModel(map));
    });
    return maps;
  }

  GameMap mapToModel(json) {
    return GameMap(
      id: json['id'],
      label: json['label'],
      definition: json['definition'],
      bottomLeft: Coordinate(lat: json['bottomLeft']['lat'], lng: json['bottomLeft']['lng']),
      topRight: Coordinate(lat: json['topRight']['lat'], lng: json['topRight']['lng']),
    );
  }



}