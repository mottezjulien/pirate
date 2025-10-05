
import '../../../../../generic/repository/generic_repository.dart';
import '../game_talk.dart';
import '../../../game_current.dart';

class GameSessionTalkRepository {

  Future<GameTalk> findById(String talkId) async {
    final GenericRepository genericRepository = GenericRepository();
    final response = await genericRepository.get(path: "/sessions/${GameCurrent.sessionId}/talks/{talkId}");
    return toModel(response);
  }

  GameTalk toModel(Map<String, dynamic> json) {
    final Image image = Image(
        type: ImageType.fromString(json['character']['image']['type']),
        value: json['character']['image']['value']);
    final character = GameTalkCharacter(
        name: json['character']['id'],
        image: image
    );
    List<GameTalkResultOption> options = [];
    if(json['result']['options'] != null) {
      options = json['result']['options']
          .map((each) => talkOptionToModel(each)).toList();
    }
    final result = GameTalkResult(
        type:  GameTalkResultType.fromString(json['result']['type']),
        options: options);

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