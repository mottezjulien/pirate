
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:mobile/contexts/connect/auth.dart';
import 'package:mobile/contexts/geo/domain/model/coordinate.dart';

import '../../../generic/config/server.dart';
import '../../../generic/repository/generic_repository.dart';
import '../../../generic/repository/http_headers.dart';
import '../domain/model/game_goal.dart';
import '../domain/model/game_session.dart';
import '../game_current.dart';

class GameSessionRepository {

  static const resourcePath = '/sessions';

  Future<GameSessionCreate> create() async {
    var uri = Server.session(resourcePath);
    final http.Response response = await http.post(uri,
        headers: Headers.userAuth(),
        body: jsonEncode({
          //'templateCode': 'TEST_DISCUSSION'
          //'templateCode': 'ChezWamEasy' //TODO 'templateCode': 'pirate_lyon'
          'templateCode': 'LYON_9'
        })
    );
    return sessionCreateToModel(jsonDecode(response.body));
  }

  Future<GameSession> start() async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    return sessionToModel(await genericRepository.post(
        path: "$resourcePath/${GameCurrent.sessionId}/start"
    ));
  }

  Future<GameSession> stop() async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    return sessionToModel(await genericRepository.post(
        path: "$resourcePath/${GameCurrent.sessionId}/stop"
    ));
  }

  GameSession sessionToModel(Map<String, dynamic> json) {
    return GameSession(id: json['id']);
  }

  GameSessionCreate sessionCreateToModel(Map<String, dynamic> json) {
    GameSession session = sessionToModel(json);
    Auth auth = Auth(token: json['gameToken']);
    return GameSessionCreate(session: session, auth: auth);
  }

  Future<void> move(Coordinate coordinate) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    var path = "$resourcePath/${GameCurrent.sessionId}/move/";
    await genericRepository.post(path: path,
        body: {
          'lat': coordinate.lat,
          'lng': coordinate.lng
        }, decode: false);
  }

  Future<List<GameGoal>> findGoals() async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    var response = await genericRepository.get(path: "$resourcePath/${GameCurrent.sessionId}/goals/");

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

}

class GameSessionCreate {

  final GameSession session;
  final Auth auth;

  GameSessionCreate({required this.session, required this.auth});

}