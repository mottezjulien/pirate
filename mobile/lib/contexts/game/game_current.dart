

import '../geo/domain/model/coordinate.dart';
import 'domain/model/game_session.dart';

class GameSessionCurrent {

  static final _GameSessionCurrent _instance = _GameSessionCurrent();

  static bool get hasSession => _instance.hasSession;

  static String get sessionId => _instance._session!.id;

  static set session(GameSession session) => _instance._session = session;

  static Coordinate get coordinate => _instance.coordinate;

  static Stream<Coordinate> get streamCoordinate => _instance.streamCoordinate;

}

class _GameSessionCurrent {

  GameSession? _session;

  bool get hasSession => _session != null;

  Coordinate get coordinate => _session!.coordinate;

  Stream<Coordinate> get streamCoordinate => _session!.streamCoordinate;

}