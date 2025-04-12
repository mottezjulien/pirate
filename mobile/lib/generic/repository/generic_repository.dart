
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:mobile/generic/repository/repository_exception.dart';

import '../config/server.dart';
import 'http_headers.dart';

class GenericRepository {

  dynamic post({
    required String resourcePath,
    Map<String, dynamic>? body
  }) async {
    final http.Response response = await http.post(Server.uri(resourcePath),
        headers: Headers.auth(),
        body: jsonEncode(body)
    );
    if(response.statusCode >= 400) {
      throw RepositoryException(response.statusCode, response.body);
    }
    return jsonDecode(response.body);
  }

}