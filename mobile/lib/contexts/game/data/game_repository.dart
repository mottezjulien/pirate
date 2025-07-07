
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
      targets.add(GameTarget(
          id: target['id'],
          label: target['label'],
          done: target['done'] == true ? true : false,
          optional: target['optional'] == true ? true : false
      ));
    });
    return GameGoal(
      id: goal['id'],
      label: goal['label'],
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
    GameMapDefinition definition = GameMapDefinition(
        type: json['definitionType'],
        value: json['definitionValue']
    );

    /*List<GameMapPosition> positions = json['positions'].map((json) {
      Coordinate point = Coordinate(lat: json['point']['lat'], lng: json['point']['lng']);
      List<String> boardIds = json['boardIds'].toList();
      return GameMapPosition(
          priority: json['priority'],
          point: point,
          boardIds: boardIds);
    }).toList();*/

    GameMapPositionPourcent? position;
    if(json['position'] != null) {
      position = GameMapPositionPourcent(x: json['position']['x'], y: json['position']['y']);
    }

    return GameMap(
      id: json['id'],
      label: json['label'],
      priority: json['priority'],
      definition: definition,
      position: position
    );
  }

  /*
      public record GameMapResponseDTO(String id, String label, int priority,
                                     String definitionType, String definitionValue,
                                     Pourcent pourcent) {

        public record Pourcent(double x, double y) {
            public static Pourcent toModel(Map.Position.Point model) {
                return new Pourcent(model.x(), model.y());
            }
        }

        public static GameMapResponseDTO fromModel(Map map, Optional<Map.Position> optPosition, Language language) {
            return new GameMapResponseDTO(map.id().value(), map.label().value(language), map.priority().value(),
                    map.definition().type().name(), map.definition().value(),
                    optPosition.map(position -> Pourcent.toModel(position.point())).orElse(null));
        }

    }
   */



}