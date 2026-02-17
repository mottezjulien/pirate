
import '../contexts/connect/auth.dart';
import '../contexts/user/user.dart';
import 'config/language.dart';

class AppCurrent {

  static final _Instance _instance = _Instance();

  static set userAuth(Auth auth) => _instance._userAuth = auth;

  static get userToken => _instance._userAuth!.token;

  static bool get hasUser => _instance._user != null;

  static set user(User user) => _instance._user = user;

  static Language get language {
    if(hasUser) {
      return _instance._user!.language;
    }
    return Language.byDefault();
  }

  static set gameSessionAuth(Auth auth) => _instance._gameSessionAuth = auth;

  static get gameSessionToken => _instance._gameSessionAuth!.token;

  static String get templateId =>  _instance._templateId!;

  static set templateId(String templateId) => _instance._templateId = templateId;

}

class _Instance {

  Auth? _userAuth;

  Auth? _gameSessionAuth;

  User? _user;

  String? _templateId;

}