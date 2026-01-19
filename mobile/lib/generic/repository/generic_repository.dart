
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:mobile/generic/repository/repository_exception.dart';

import '../config/server.dart';
import 'http_headers.dart';

class GenericGameSessionRepository {

  static final String noContent = "NO_CONTENT";


  dynamic post({
    required String path,
    Map<String, dynamic>? body,
    bool decode = true
  }) async {
    final http.Response response = await http.post(Server.session(path),
        headers: Headers.gameSessionAuth(),
        body: jsonEncode(body)
    );
    if(response.statusCode >= 400) {
      throw RepositoryException(response.statusCode, response.body);
    }
    if(response.statusCode == 204) {
      return Future.value(noContent);
    }
    if(decode) {
      return jsonDecode(response.body);
    }
  }

  dynamic get({
    required String path,
  }) async {
    final http.Response response = await http.get(Server.session(path),
        headers: Headers.gameSessionAuth());
    if(response.statusCode >= 400) {
      throw RepositoryException(response.statusCode, response.body);
    }
    return jsonDecode(response.body);
  }

  Future<void> delete({
    required String path,
  }) async {
    final http.Response response = await http.delete(Server.session(path),
        headers: Headers.gameSessionAuth());
    if(response.statusCode >= 400) {
      throw RepositoryException(response.statusCode, response.body);
    }
  }

}