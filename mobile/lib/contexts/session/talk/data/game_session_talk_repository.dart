
import '../../../../../generic/repository/generic_repository.dart';
import '../../game_current.dart';
import '../game_talk.dart';
class GameSessionTalkRepository {

  Future<GameTalk> findById(String talkId) async {
    final GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    final responseBody = await genericRepository.get(path: resources(talkId));
    return toModel(responseBody);
  }

  Future<GameTalk?> selectOption({required String talkId, required String optionId}) async {
    final GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    final responseBody = await genericRepository.post(path: "${resources(talkId)}/options/$optionId/");
    if(responseBody == GenericGameSessionRepository.noContent) {
      return null;
    }
    return toModel(responseBody);
  }

  Future<void> submitInputText({required String talkId, required String value}) async {
    final GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    await genericRepository.post(
      path: "${resources(talkId)}/inputtext/",
      body: {"value": value},
    );
  }

  String resources(String talkId) => "/instances/${GameCurrent.sessionId}/talks/$talkId";

  GameTalk toModel(Map<String, dynamic> json) {
    final GameImage image = GameImage(
        type: GameImageType.fromString(json['character']['image']['type']),
        value: json['character']['image']['value']);
    final character = GameTalkCharacter(
        name: json['character']['name'],
        image: image
    );
    return GameTalk(
        id: json['id'],
        text: json['text'],
        character: character,
        next: toModelNext(json['next']));
  }

  GameTalkResultOption talkOptionToModel(Map<String, dynamic> json) {
    return GameTalkResultOption(id: json['id'], value: json['value']);
  }

  GameTalkNext toModelNext(Map<String, dynamic> jsonNext) {
    switch(jsonNext['type']) {
      case "CONTINUE":
        return GameTalkResultContinue(nextId: jsonNext['nextId']);
      case "MULTIPLE":
        List<GameTalkResultOption> options = [];
        if(jsonNext['options'] != null) {
          options = jsonNext['options']
              .map((each) => talkOptionToModel(each)).toList().cast<GameTalkResultOption>();
        }
        return GameTalkResultMultiple(options: options);
      case "INPUTTEXT":
      case "INPUT_TEXT":
        final parameters = jsonNext['parameters'] as Map<String, dynamic>?;
        final typeStr = parameters?['type'] as String? ?? 'ALPHANUMERIC';
        final sizeStr = parameters?['size'] as String?;
        final hint = parameters?['hint'] as String?;
        return GameTalkResultInputText(
          type: GameTalkInputTextType.fromString(typeStr),
          size: sizeStr != null ? int.tryParse(sizeStr) : null,
          hint: hint,
        );
        default:
          return GameTalkResultEmpty();
    }
  }


}
