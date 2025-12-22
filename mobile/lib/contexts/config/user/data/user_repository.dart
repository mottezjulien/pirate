
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:mobile/generic/config/language.dart';

import '../../../../generic/config/server.dart';
import '../../../../generic/repository/http_headers.dart';
import '../user.dart';


class UserRepository {

  static const String resourcePath = '/users';

  Future<User?> find() async {
    var uri = Server.config(resourcePath);
    final http.Response response = await http.get(uri, headers: Headers.userAuth());
    if(response.statusCode == 404) {
      return null;
    }
    return _toModel(jsonDecode(response.body));
  }

  Future<User> onBoard({required Language language, String? nickName, String? email}) async {
    var uri = Server.config("$resourcePath/onboard");
    final http.Response response = await http.post(uri,
        headers: Headers.userAuth(),
        body: jsonEncode({
          'language': language.toValue(),
          'nickName': nickName,
          'email': email
        })
    );
    return _toModel(jsonDecode(response.body));
  }

  User _toModel(Map<String, dynamic> json) {
    return User(id: json['id'],
        type: UserType.valueOf(json['type']) ?? UserType.none,
        language: Language.valueOf(json['language']) ?? Language.byDefault(),
        nickname: json['nickname'],
        email: json['email']);
  }


}