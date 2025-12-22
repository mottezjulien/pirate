
import '../app_current.dart';

class Headers {

  static Map<String, String> noAuth() {
    return {
      'Content-Type': 'application/json; charset=UTF-8',
      'Accept': 'application/json'
    };
  }

  static Map<String, String> userAuth() {
    return {
      'Content-Type': 'application/json; charset=UTF-8',
      'Accept': 'application/json',
      'Language': AppCurrent.language.toValue(),
      'Authorization': AppCurrent.userToken
    };
  }

  static Map<String, String> gameSessionAuth() {
    return {
      'Content-Type': 'application/json; charset=UTF-8',
      'Accept': 'application/json',
      'Language': AppCurrent.language.toValue(),
      'Authorization': AppCurrent.gameSessionToken
    };
  }

}