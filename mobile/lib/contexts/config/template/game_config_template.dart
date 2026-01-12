
class GameConfigTemplateSimple {

  final String id, label;
  final int level;
  final String departureAddress;

  GameConfigTemplateSimple({required this.id, required this.label, required this.level, required this.departureAddress});

  factory GameConfigTemplateSimple.fromJson(Map<String, dynamic> json) {
    return GameConfigTemplateSimple(
      id: json['id'] as String,
      label: json['label'] as String,
      level: json['level'] as int,
      departureAddress: json['departureAddress'] as String,
    );
  }

}

class GameConfigTemplateDetails {
  final String id, label, description;
  final int level;
  final Location departure;

  GameConfigTemplateDetails({required this.id, required this.label, required this.description,
    required this.level, required this.departure});

  factory GameConfigTemplateDetails.fromJson(Map<String, dynamic> json) {
    return GameConfigTemplateDetails(
      id: json['id'] as String,
      label: json['label'] as String,
      description: json['description'] as String,
      level: json['level'] as int,
      departure: Location.fromJson(json['departure'])
    );
  }

}

class Location {
  final String address;
  final Point bottomLeft, topRight;
  Location({required this.address, required this.bottomLeft, required this.topRight});

  factory Location.fromJson(Map<String, dynamic> json) {
    return Location(
        address: json['address'] as String,
        bottomLeft: Point.fromJson(json['bottomLeft']),
        topRight: Point.fromJson(json['topRight']));
  }
}

class Point {
  final double lat, lng;
  Point({required this.lat, required this.lng});
  factory Point.fromJson(Map<String, dynamic> json) {
    return Point(lat: json['lat'] as double, lng: json['lng'] as double);
  }
}