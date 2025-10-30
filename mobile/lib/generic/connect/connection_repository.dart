
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:mobile/generic/connect/auth.dart';

import '../config/server.dart';
import '../repository/http_headers.dart';
import '../repository/repository_exception.dart';

class ConnectionRepository {

  static const String path = '/connect';

  Future<Auth> byDeviceId({
    required String deviceId
  }) async {

    try {
      var uri = Server.uri(path);
      var headers = Headers.noAuth();
      var body = jsonEncode({
            'deviceId': deviceId
          });

      final http.Response response = await http.post(uri,
          headers: headers,
          body: body
      );
      if(response.statusCode >= 400) {
        throw RepositoryException(response.statusCode, response.body);
      }
      return toModel(jsonDecode(response.body));
    } catch(e) {
      print(e);
      rethrow;
    }

  }

  Auth toModel(Map<String, dynamic> json) {
    return Auth(token: json['token']);
  }


}