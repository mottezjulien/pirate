
import 'dart:convert';

import 'package:http/http.dart' as http;
import '../../../generic/config/server.dart';
import '../../../generic/repository/http_headers.dart';
import 'game_config_template.dart';

class GameConfigTemplateRepository {

  static const resourcePath = '/templates';

  Future<List<GameConfigTemplateSimple>> findByCode(String code) async {
    var uri = Server.config('$resourcePath/search?code=$code');
    final http.Response response = await http.get(uri,
        headers: Headers.userAuth()
    );

    final List<GameConfigTemplateSimple> templates = [];
    jsonDecode(response.body).forEach((jsonMap) {
      templates.add(GameConfigTemplateSimple.fromJson(jsonMap));
    });
    return templates;
  }

  Future<GameConfigTemplateDetails> findById(String id) async {
    var uri = Server.config('$resourcePath/$id');
    final http.Response response = await http.get(uri,
        headers: Headers.userAuth()
    );
    if(response.statusCode == 200){
      return GameConfigTemplateDetails.fromJson(jsonDecode(response.body));
    }else{
      throw Exception('Error getting template details');
    }

  }


}