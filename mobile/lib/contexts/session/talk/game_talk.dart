

import 'package:flutter/material.dart';

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
  final GameImage image;
  GameTalkCharacter({required this.name, required this.image});
}


sealed class GameTalkResult {}

class GameTalkResultSimple extends GameTalkResult {

}

class GameTalkResultContinue extends GameTalkResult {
  final String nextId;
  GameTalkResultContinue({required this.nextId});
}

class GameTalkResultMultiple extends GameTalkResult {
  final List<GameTalkResultOption> options;
  GameTalkResultMultiple({required this.options});
}

class GameTalkResultOption {
  final String id;
  final String value;
  GameTalkResultOption({required this.id, required this.value});
}

class GameImage {
  final GameImageType type;
  final String value;
  GameImage({required this.type, required this.value});

  Widget? toWidget({BoxFit? fit}) {
    switch(type) {
      case GameImageType.ASSET:
        return Image.asset(value.startsWith("/") ? "assets$value" : "assets/$value", fit: fit);
      case GameImageType.WEB:
        return Image.network(value, fit: fit,
          errorBuilder: (context, error, stackTrace) {
            return Center(child: Icon(Icons.broken_image, size: 20));
          },
        );
    }
  }
}

enum GameImageType {
  ASSET, WEB;
  static GameImageType fromString(String str) {
    return GameImageType.values.singleWhere((each) => each.name == str);
  }
}

