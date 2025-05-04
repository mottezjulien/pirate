

import 'domain/model/game_session.dart';

class GameSessionCurrent {

  static final _GameSessionCurrent _instance = _GameSessionCurrent();

  static bool get hasSession => _instance.hasSession;

  static String get sessionId => _instance._session!.id;

  static set session(GameSession session) => _instance._session = session;

}

class _GameSessionCurrent {

  GameSession? _session;

  bool get hasSession => _session != null;

}