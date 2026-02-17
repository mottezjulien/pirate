
import '../../../generic/repository/generic_repository.dart';
import '../../session/game_current.dart';
import 'board.dart';

class GameConfigBoardRepository {

  Future<List<BoardSpace>> findBoardSpaces() async {
    final GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    var response = await genericRepository.get(path: "/instances/${GameCurrent.sessionId}/boards/");
    return (response as List).map((json) => BoardSpace.fromJson(json)).toList();
  }

}