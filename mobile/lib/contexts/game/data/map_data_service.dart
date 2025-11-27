import 'dart:convert';
import 'package:flutter/services.dart';
import '../domain/model/map_models.dart';

class MapDataService {

  static const String _jsonPath = 'assets/game/tres/test-config.json';

  /// Charge les données des cartes depuis le fichier JSON
  Future<List<GameMapData>> loadMaps() async {
    try {
      final String jsonString = await rootBundle.loadString(_jsonPath);
      final dynamic jsonData = json.decode(jsonString);
      
      // Gérer les deux formats : objet unique ou tableau d'objets
      if (jsonData is List) {
        // Format tableau (ancien format)
        return jsonData
            .map((json) => GameMapData.fromJson(json))
            .toList();
      } else if (jsonData is Map<String, dynamic>) {
        // Format objet unique (nouveau format de l'éditeur)
        return [GameMapData.fromJson(jsonData)];
      } else {
        throw Exception('Format JSON non supporté');
      }
    } catch (e) {
      throw Exception('Erreur lors du chargement des cartes: $e');
    }
  }
}