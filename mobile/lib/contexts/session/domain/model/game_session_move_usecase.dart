
import 'package:flutter/foundation.dart';

import '../../../config/board/board.dart';
import '../../../geo/domain/model/coordinate.dart';

class GameSessionMoveUseCase {

  final Board board;

  GameSessionMoveUseCase({required this.board});

  GameSessionMoveResult apply({
    required Coordinate currentCoordinate,
    required List<BoardSpace> lastSpaces
  }) {
    final List<BoardSpace> currentSpaces = board.fromCoordinate(coordinate: currentCoordinate);
    if(!listEquals(currentSpaces, lastSpaces)) {
      return GameSessionMoveResultUpdate(current: currentSpaces);
    }
    return GameSessionMoveResultNothing();
  }

}

abstract class GameSessionMoveResult {

}

class GameSessionMoveResultNothing extends GameSessionMoveResult {

}

class GameSessionMoveResultUpdate extends GameSessionMoveResult {

  final List<BoardSpace> current;

  GameSessionMoveResultUpdate({required this.current});

}

