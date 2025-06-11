
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:mobile/generic/connect/auth.dart';

import '../config/server.dart';
import '../repository/http_headers.dart';
import '../repository/repository_exception.dart';

class ConnectionRepository {

  static const String path = '/connect';

  Future<Auth> byDeviceId({
    required String deviceId,
    required String firebaseToken
  }) async {

    final http.Response response = await http.post(Server.uri(path),
        headers: Headers.noAuth(),
        body: jsonEncode({
          'deviceId': deviceId,
          'firebaseToken': firebaseToken
        })
    );
    if(response.statusCode >= 400) {
      throw RepositoryException(response.statusCode, response.body);
    }
    return toModel(jsonDecode(response.body));
  }

  Auth toModel(Map<String, dynamic> json) {
    return Auth(token: json['token']);
  }


}