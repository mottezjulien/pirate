import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/contexts/config/board/board.dart';
import 'package:mobile/contexts/geo/domain/model/coordinate.dart';
import 'package:mobile/contexts/session/domain/model/game_session_move_usecase.dart';

void main() {
  group('GameSessionMoveUseCase', () {
    // Define shared spaces for tests
    final spaceA = BoardSpace(
      id: 'A',
      label: 'Space A',
      rectangles: [
        BoardRectangle(
          bottomLeft: Coordinate(lat: 0, lng: 0),
          topRight: Coordinate(lat: 10, lng: 10),
        ),
      ],
    );

    final spaceB = BoardSpace(
      id: 'B',
      label: 'Space B',
      rectangles: [
        BoardRectangle(
          bottomLeft: Coordinate(lat: 5, lng: 5),
          topRight: Coordinate(lat: 15, lng: 15),
        ),
      ],
    );

    final spaceC = BoardSpace(
      id: 'C',
      label: 'Space C',
      rectangles: [
        BoardRectangle(
          bottomLeft: Coordinate(lat: 20, lng: 20),
          topRight: Coordinate(lat: 30, lng: 30),
        ),
      ],
    );

    final board = Board(spaces: [spaceA, spaceB, spaceC]);
    final useCase = GameSessionMoveUseCase(board: board);

    test('should return Nothing when current coordinate is in both A and B and lastSpaces was [A, B]', () {
      // Coordinate (7, 7) is in A (0,0 to 10,10) and B (5,5 to 15,15)
      final currentCoordinate = Coordinate(lat: 7, lng: 7);
      final lastSpaces = [spaceA, spaceB];

      final result = useCase.apply(
        currentCoordinate: currentCoordinate,
        lastSpaces: lastSpaces,
      );

      expect(result, isA<GameSessionMoveResultNothing>());
    });

    test('should return Update with B when current coordinate is only in B and lastSpaces was [A, B]', () {
      // Coordinate (12, 12) is in B (5,5 to 15,15) but NOT in A (0,0 to 10,10)
      final currentCoordinate = Coordinate(lat: 12, lng: 12);
      final lastSpaces = [spaceA, spaceB];

      final result = useCase.apply(
        currentCoordinate: currentCoordinate,
        lastSpaces: lastSpaces,
      );

      expect(result, isA<GameSessionMoveResultUpdate>());
      final updateResult = result as GameSessionMoveResultUpdate;
      expect(updateResult.current, [spaceB]);
    });
  });
}
