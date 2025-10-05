
class GameTalk {
  final String id;
  final String value;
  final GameTalkCharacter character;
  final GameTalkResult result;
  GameTalk({required this.id, required this.value,
    required this.character, required this.result});
}

class GameTalkCharacter {
  final String name;
  final Image image;
  GameTalkCharacter({required this.name, required this.image});
}

class GameTalkResult {
  final GameTalkResultType type;
  final List<GameTalkResultOption> options;
  GameTalkResult({required this.type, required this.options});
}

enum GameTalkResultType {
  NEXT, OPTIONS, END;

  static GameTalkResultType fromString(String str) {
    switch(str) {
      case 'NEXT': return GameTalkResultType.NEXT;
      case 'OPTIONS': return GameTalkResultType.OPTIONS;
      default: throw Exception('Unknown GameTalkResultType');
    }
  }

}

class GameTalkResultOption {
  final String id;
  final String value;
  GameTalkResultOption({required this.id, required this.value});
}

class Image {
  final ImageType type;
  final String value;
  Image({required this.type, required this.value});
}

enum ImageType {
  ASSET, WEB;
  static ImageType fromString(String str) {
    switch(str) {
      case 'ASSET': return ImageType.ASSET;
      case 'WEB': return ImageType.WEB;
      default: throw Exception('Unknown ImageType');
    }
  }
}

