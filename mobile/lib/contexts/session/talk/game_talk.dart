

import 'package:flutter/material.dart';

class GameTalk {


  final String id;
  final String text;
  final GameTalkCharacter character;
  final GameTalkNext next;
  GameTalk({required this.id, required this.text,
    required this.character, required this.next});
}

class GameTalkCharacter {
  final String name;
  final GameImage image;
  GameTalkCharacter({required this.name, required this.image});
}


sealed class GameTalkNext {}

class GameTalkResultEmpty extends GameTalkNext {

}

class GameTalkResultContinue extends GameTalkNext {
  final String nextId;
  GameTalkResultContinue({required this.nextId});
}

class GameTalkResultMultiple extends GameTalkNext {
  final List<GameTalkResultOption> options;
  GameTalkResultMultiple({required this.options});
}

class GameTalkResultInputText extends GameTalkNext {
  final GameTalkInputTextType type;
  final int? size;
  final String? hint;
  GameTalkResultInputText({required this.type, this.size, this.hint});
}

enum GameTalkInputTextType {
  NUMERIC, ALPHANUMERIC;
  static GameTalkInputTextType fromString(String str) {
    return GameTalkInputTextType.values.singleWhere((each) => each.name == str);
  }
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

