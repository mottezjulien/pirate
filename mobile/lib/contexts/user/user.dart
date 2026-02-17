

import '../../../generic/config/language.dart';

class User {

  final String id;
  final UserType type;
  final Language language;
  final String? nickname, email;

  User({required this.id, required this.type, required this.language, this.nickname, this.email});

}

enum UserType {
  none, onBoarded;

  static UserType? valueOf(String value) {
    for(UserType element in UserType.values) {
      if(element.toString().toUpperCase() == value.toUpperCase()) {
        return element;
      }
    }
    return null;
  }

}