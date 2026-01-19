
import 'package:flutter/material.dart';

import '../image/game_image_repository.dart';

class GameInventorySimple {

  final String id;
  final String label;
  final ImageData image;
  final List<GameInventoryAction> actions;
  final int count;

  GameInventorySimple({required this.id, required this.label,
    required this.image, required this.actions, required this.count});

  factory GameInventorySimple.fromJson(Map<String, dynamic> json) {
    final List<GameInventoryAction> actions = [];
    json['actions'].forEach((jsonActions) {
      actions.add(GameInventoryAction.fromString(jsonActions));
    });
    return GameInventorySimple(
        id: json['id'] as String,
        label: json['label'] as String,
        image: ImageData.fromJson(json['image'] as Map<String,dynamic>),
        actions: actions,
        count: json['count'] as int);
  }

  bool hasAction(GameInventoryAction action) => actions.contains(action);

  Widget buildImage({BoxFit fit = BoxFit.contain}) {
    if (image.type == 'ASSET') {
      final path = image.value.startsWith("/") ? "assets${image.value}" : "assets/${image.value}";
      return Image.asset(path, fit: fit, errorBuilder: (_, __, ___) => const Icon(Icons.inventory_2, size: 48));
    } else if (image.type == 'WEB') {
      return Image.network(image.value, fit: fit, errorBuilder: (_, __, ___) => const Icon(Icons.inventory_2, size: 48));
    }
    return const Icon(Icons.inventory_2, size: 48);
  }
}

class GameInventoryDetail extends GameInventorySimple {

  final String? description;
  final GameInventoryAvailability availability;

  GameInventoryDetail({
    required super.id,
    required super.label,
    required super.image,
    required super.actions,
    required super.count,
    this.description,
    required this.availability,
  });

  factory GameInventoryDetail.fromJson(Map<String, dynamic> json) {
    final List<GameInventoryAction> actions = [];
    json['actions'].forEach((jsonActions) {
      actions.add(GameInventoryAction.fromString(jsonActions));
    });
    return GameInventoryDetail(
        id: json['id'] as String,
        label: json['label'] as String,
        image: ImageData.fromJson(json['image'] as Map<String,dynamic>),
        actions: actions,
        count: json['count'] as int,
        description: json['description'] as String?,
        availability: GameInventoryAvailability.fromString(json['availability'] as String));
  }
}

enum GameInventoryAction {
  DROP, EQUIP, UNEQUIP, MERGE, CONSUME, USE, USE_EQUIP;

  static GameInventoryAction fromString(String str) {
    return GameInventoryAction.values.singleWhere(
      (each) => each.name == str,
      orElse: () => DROP, // fallback
    );
  }
}

enum GameInventoryAvailability {
  FREE, EQUIP, UNAVAILABLE;

  static GameInventoryAvailability fromString(String str) {
    return GameInventoryAvailability.values.singleWhere(
      (each) => each.name == str,
      orElse: () => FREE,
    );
  }
}