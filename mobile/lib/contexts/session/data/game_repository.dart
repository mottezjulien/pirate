
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:mobile/contexts/connect/auth.dart';

import '../../../generic/app_current.dart';
import '../../../generic/config/server.dart';
import '../../../generic/repository/generic_repository.dart';
import '../../../generic/repository/http_headers.dart';
import '../../config/board/board.dart';
import '../domain/model/game_goal.dart';
import '../domain/model/game_session.dart';
import '../game_current.dart';

class GameSessionRepository {

  static const resourcePath = '/instances';

  Future<GameSessionResponseDTO?> find() async {
    var uri = Server.session(resourcePath);
    final http.Response response = await http.get(uri, headers: Headers.userAuth());
    if(response.statusCode == 404) {
      return null;
    }
    return sessionCreateToModel(jsonDecode(response.body));
  }

  Future<GameSessionResponseDTO> create() async {
    var uri = Server.session(resourcePath);
    final http.Response response = await http.post(uri,
        headers: Headers.userAuth(),
        body: jsonEncode({
          'templateId': AppCurrent.templateId
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
    return GameSession(id: json['id'], playerId: json['playerId'], state: json['state']);
  }

  GameSessionResponseDTO sessionCreateToModel(Map<String, dynamic> json) {
    GameSession session = sessionToModel(json);
    Auth auth = Auth(token: json['auth']['token'], state: json['auth']['state']);
    return GameSessionResponseDTO(session: session, auth: auth);
  }

  Future<void> move(List<BoardSpace> spaces) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    var path = "$resourcePath/${GameCurrent.sessionId}/move/";
    await genericRepository.post(path: path,
        body: {'spaceIds': spaces.map((space) => space.id).toList()}, decode: false);
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

  Future<void> confirmAnswer({required String confirmId, required bool answer}) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    var path = "$resourcePath/${GameCurrent.sessionId}/messages/$confirmId/confirm";
    await genericRepository.post(path: path, body: {'answer': answer}, decode: false);
  }

}

class GameSessionResponseDTO {
  final GameSession session;
  final Auth auth;
  GameSessionResponseDTO({required this.session, required this.auth});
}

