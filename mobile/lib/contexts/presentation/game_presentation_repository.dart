
import 'dart:convert';

import 'package:http/http.dart' as http;
import '../../../generic/config/server.dart';
import '../../../generic/repository/http_headers.dart';
import 'game_presentation.dart';

class GamePresentationRepository {

  static const resourcePath = '/presentation';

  Future<List<GamePresentationSimple>> findByCode(String code) async {
    var uri = Server.config('$resourcePath/search/code?code=$code');
    final http.Response response = await http.get(uri,
        headers: Headers.userAuth()
    );

    final List<GamePresentationSimple> templates = [];
    jsonDecode(response.body).forEach((jsonMap) {
      templates.add(GamePresentationSimple.fromJson(jsonMap));
    });
    return templates;
  }

  Future<List<GamePresentationSimple>> searchByLocation(double bottomLeftLat, double bottomLeftLng, double topRightLat, double topRightLng) async {
    var uri = Server.config('$resourcePath/search/location?bottomLeftLat=$bottomLeftLat&bottomLeftLng=$bottomLeftLng&topRightLat=$topRightLat&topRightLng=$topRightLng');
    final http.Response response = await http.get(uri,
        headers: Headers.userAuth()
    );

    final List<GamePresentationSimple> templates = [];
    jsonDecode(response.body).forEach((jsonMap) {
      templates.add(GamePresentationSimple.fromJson(jsonMap));
    });
    return templates;
  }

  Future<GamePresentationDetails> findById(String id) async {
    var uri = Server.config('$resourcePath/$id');
    final http.Response response = await http.get(uri,
        headers: Headers.userAuth()
    );
    if(response.statusCode == 200){
      return GamePresentationDetails.fromJson(jsonDecode(response.body));
    }else{
      throw Exception('Error getting template details');
    }

  }


}
