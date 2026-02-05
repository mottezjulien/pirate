

import 'package:flutter/material.dart';

import '../geo/domain/model/coordinate.dart';
import 'domain/model/game_session.dart';

class GameCurrent {

  static final _GameSession _sessionInstance = _GameSession();

  static bool get hasSession => _sessionInstance.hasSession;

  static String get sessionId => _sessionInstance._session!.id;

  static set session(GameSession session) => _sessionInstance._session = session;

  static void addOnMoveListener(onMoveListener) => _sessionInstance.addOnMoveListener(onMoveListener);

  static void removeOnMoveListener(onMoveListener) => _sessionInstance.removeOnMoveListener(onMoveListener);

  static void addOnGoalListener(onGoalListener) => _sessionInstance.addOnGoalListener(onGoalListener);

  static void removeOnGoalListener(onGoalListener) => _sessionInstance.removeOnGoalListener(onGoalListener);

  static final _Style _styleInstance = _Style();

  static _Style get style => _styleInstance;

  static void stopSession() => _sessionInstance._session?.stop();

  static void removeSession() => _sessionInstance._session = null;

  /// Get the player's current GPS position
  static Coordinate? get currentPosition {
    if (!hasSession) return null;
    try {
      return _sessionInstance.coordinate;
    } catch (_) {
      return null;
    }
  }

}


class _GameSession {

  GameSession? _session;

  bool get hasSession => _session != null;

  Coordinate get coordinate => _session!.coordinate;

  void addOnMoveListener(onMoveListener) => _session!.addOnMoveListener(onMoveListener);

  void removeOnMoveListener(onMoveListener) => _session!.removeOnMoveListener(onMoveListener);

  void addOnGoalListener(onGoalListener) => _session!.addOnGoalListener(onGoalListener);

  void removeOnGoalListener(onGoalListener) => _session!.removeOnGoalListener(onGoalListener);

}

//TODO

class _Style {
  _Color color = _Color();
  _Dimension dimension = _Dimension();
  _Duration duration = _Duration();
}

class _Color {
  Color get primary => Colors.deepOrange;
  Color get background => Colors.blueAccent; //TODO ???
  Color get lightGrey => Color(0x61D1D1D1); //TODO ???
}

class _Dimension {
  final double small = 4.0;
  final double medium = 8.0;
  final double large = 12.0;
  final double extraLarge = 16.0;
  final double xxLarge = 20.0;
  final double xxxLarge = 24.0;
}

class _Duration {
  Duration get default_ => Duration(milliseconds: 300);
}

