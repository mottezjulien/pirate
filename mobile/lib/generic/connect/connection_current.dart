import 'auth.dart';

class ConnectionCurrent {

  static final _ConnectionCurrent _instance = _ConnectionCurrent();

  static set auth(Auth auth) => _instance._auth = auth;

  static get token => _instance._auth!.token;

}

class _ConnectionCurrent {

  Auth? _auth;

}