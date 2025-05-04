

import '../config/language.dart';
import '../connect/connection_current.dart';

class Headers {

  static Map<String, String> noAuth() {
    return {
      'Content-Type': 'application/json; charset=UTF-8',
      'Accept': 'application/json',
      'Language': Language.byDefault().toString() // TODO
    };
  }

  static Map<String, String> auth() {
    return {
      'Content-Type': 'application/json; charset=UTF-8',
      'Accept': 'application/json',
      'Language': Language.byDefault().toValue(), // TODO
      'Authorization': ConnectionCurrent.token
    };
  }

}