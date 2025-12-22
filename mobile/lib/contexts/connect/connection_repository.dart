
import 'dart:convert';

import 'package:http/http.dart' as http;

import '../../generic/config/device.dart';
import '../../generic/config/server.dart';
import '../../generic/repository/http_headers.dart';
import '../../generic/repository/repository_exception.dart';
import 'auth.dart';

class ConnectionRepository {

  static const String path = '/connect';

  Future<Auth> createByDeviceId() async {

    final String deviceId = await Device.id();
    var uri = Server.auth(path);
    var headers = Headers.noAuth();
    var body = jsonEncode({'deviceId': deviceId });

    final http.Response response = await http.post(uri,
        headers: headers,
        body: body
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