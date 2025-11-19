
import '../../../../../generic/repository/generic_repository.dart';
import '../game_talk.dart';
import '../../../game_current.dart';

class GameSessionTalkRepository {

  Future<GameTalk> findById(String talkId) async {
    final GenericRepository genericRepository = GenericRepository();
    final responseBody = await genericRepository.get(path: "/sessions/${GameCurrent.sessionId}/talks/$talkId");
    return toModel(responseBody);
  }

  Future<GameTalk?> selectOption({required String talkId, required String optionId}) async {
    final GenericRepository genericRepository = GenericRepository();
    final responseBody = await genericRepository.post(path: "/sessions/${GameCurrent.sessionId}/talks/$talkId/options/$optionId/");
    if(responseBody == GenericRepository.noContent) {
      return null;
    }
    return toModel(responseBody);
  }


  GameTalk toModel(Map<String, dynamic> json) {
    final GameImage image = GameImage(
        type: GameImageType.fromString(json['character']['image']['type']),
        value: json['character']['image']['value']);
    final character = GameTalkCharacter(
        name: json['character']['name'],
        image: image
    );


    GameTalkResult result = GameTalkResultSimple();
    switch(GameTalkResultType.fromString(json['result']['type'])) {
      case GameTalkResultType.SIMPLE:
        result = GameTalkResultSimple();
        break;
      case GameTalkResultType.CONTINUE:
        result = GameTalkResultContinue(nextId: json['result']['nextId']);
        break;
      case GameTalkResultType.MULTIPLE:
        List<GameTalkResultOption> options = [];
        if(json['result']['options'] != null) {
          options = json['result']['options']
              .map((each) => talkOptionToModel(each)).toList().cast<GameTalkResultOption>();
        }
        result = GameTalkResultMultiple(options: options);
        break;
    }

    return GameTalk(
        id: json['id'],
        value: json['value'],
        character: character,
        result: result);
  }

  GameTalkResultOption talkOptionToModel(Map<String, dynamic> json) {
    return GameTalkResultOption(id: json['id'], value: json['value']);
  }

}

enum GameTalkResultType {
  SIMPLE, CONTINUE, MULTIPLE;
  static GameTalkResultType fromString(String str) {
    return GameTalkResultType.values.singleWhere((each) => each.name == str);
  }
}