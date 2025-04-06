

import 'domain/model/game.dart';

class GameCurrent {

  static final _GameCurrent _instance = _GameCurrent();

  static bool get hasGame => _instance.hasGame;

  static set game(Game game) => _instance._game = game;

}

class _GameCurrent {

  Game? _game;

  bool get hasGame => _game != null;

}