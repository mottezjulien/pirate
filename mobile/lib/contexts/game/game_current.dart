

import 'domain/model/game.dart';

class GameCurrent {

  static final _GameCurrent _instance = _GameCurrent();

  static bool get hasGame => _instance.hasGame;

}

class _GameCurrent {

  Game? game;

  bool get hasGame => game != null;

}