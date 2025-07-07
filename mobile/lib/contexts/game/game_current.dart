

import '../geo/domain/model/coordinate.dart';
import 'domain/model/game_session.dart';

class GameSessionCurrent {

  static final _GameSessionCurrent _instance = _GameSessionCurrent();

  static bool get hasSession => _instance.hasSession;

  static String get sessionId => _instance._session!.id;

  static set session(GameSession session) => _instance._session = session;

  static void addOnMoveListener(onMoveListener) => _instance.addOnMoveListener(onMoveListener);

  static void removeOnMoveListener(onMoveListener) => _instance.removeOnMoveListener(onMoveListener);

  static void addOnGoalListener(onGoalListener) => _instance.addOnGoalListener(onGoalListener);

  static void removeOnGoalListener(onGoalListener) => _instance.removeOnGoalListener(onGoalListener);

}

class _GameSessionCurrent {

  GameSession? _session;

  bool get hasSession => _session != null;

  Coordinate get coordinate => _session!.coordinate;

  void addOnMoveListener(onMoveListener) => _session!.addOnMoveListener(onMoveListener);

  void removeOnMoveListener(onMoveListener) => _session!.removeOnMoveListener(onMoveListener);

  void addOnGoalListener(onGoalListener) => _session!.addOnGoalListener(onGoalListener);

  void removeOnGoalListener(onGoalListener) => _session!.removeOnGoalListener(onGoalListener);

}