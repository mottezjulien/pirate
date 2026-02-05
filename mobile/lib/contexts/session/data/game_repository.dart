
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:mobile/contexts/connect/auth.dart';
import 'package:mobile/contexts/geo/domain/model/coordinate.dart';

import '../../../generic/app_current.dart';
import '../../../generic/config/server.dart';
import '../../../generic/repository/generic_repository.dart';
import '../../../generic/repository/http_headers.dart';
import '../domain/model/game_goal.dart';
import '../domain/model/game_session.dart';
import '../game_current.dart';

class GameSessionRepository {

  static const resourcePath = '/sessions';

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
    return GameSession(id: json['id']);
  }

  GameSessionResponseDTO sessionCreateToModel(Map<String, dynamic> json) {
    GameSession session = sessionToModel(json);
    Auth auth = Auth(token: json['gameToken']);
    return GameSessionResponseDTO(session: session, auth: auth);
  }

  Future<void> move(Coordinate coordinate, List<String> spaceIds) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    var path = "$resourcePath/${GameCurrent.sessionId}/move/";
    await genericRepository.post(path: path,
        body: {
          'lat': coordinate.lat,
          'lng': coordinate.lng,
          'spaceIds': spaceIds
        }, decode: false);
  }

  Future<List<BoardSpace>> findBoardSpaces() async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    var response = await genericRepository.get(path: "$resourcePath/${GameCurrent.sessionId}/boards/");
    return (response as List).map((json) => BoardSpace.fromJson(json)).toList();
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

/// Represents a geographic zone (board space) where events can be triggered
class BoardSpace {
  final String id;
  final String label;
  final List<BoardRectangle> rectangles;

  BoardSpace({required this.id, required this.label, required this.rectangles});

  factory BoardSpace.fromJson(Map<String, dynamic> json) {
    return BoardSpace(
      id: json['id'] as String,
      label: json['label'] as String? ?? '',
      rectangles: (json['rectangles'] as List)
          .map((r) => BoardRectangle.fromJson(r))
          .toList(),
    );
  }

  /// Check if a coordinate is inside any of this space's rectangles
  bool contains(Coordinate coordinate) {
    return rectangles.any((rect) => rect.contains(coordinate));
  }
}

/// Represents a GPS rectangle (bounding box)
class BoardRectangle {
  final Coordinate bottomLeft;
  final Coordinate topRight;

  BoardRectangle({required this.bottomLeft, required this.topRight});

  factory BoardRectangle.fromJson(Map<String, dynamic> json) {
    final bl = json['bottomLeft'];
    final tr = json['topRight'];
    return BoardRectangle(
      bottomLeft: Coordinate(
        lat: (bl['lat'] as num).toDouble(),
        lng: (bl['lng'] as num).toDouble(),
      ),
      topRight: Coordinate(
        lat: (tr['lat'] as num).toDouble(),
        lng: (tr['lng'] as num).toDouble(),
      ),
    );
  }

  /// Check if a coordinate is inside this rectangle
  bool contains(Coordinate coordinate) {
    return coordinate.lat >= bottomLeft.lat &&
        coordinate.lat <= topRight.lat &&
        coordinate.lng >= bottomLeft.lng &&
        coordinate.lng <= topRight.lng;
  }
}