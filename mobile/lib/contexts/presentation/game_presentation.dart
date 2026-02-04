
class GamePresentationSimple {

  final String id, gameId, label;
  final int level;
  final String departureAddress;
  final Point departurePoint;
  final double rating;

  GamePresentationSimple({required this.id, required this.gameId, required this.label, required this.level, required this.departureAddress, required this.departurePoint, required this.rating});

  factory GamePresentationSimple.fromJson(Map<String, dynamic> json) {
    return GamePresentationSimple(
      id: json['presentationId'] as String,
      gameId: json['gameId'] as String,
      label: json['label'] as String,
      level: json['level'] as int,
      departureAddress: json['departureAddress'] as String,
      departurePoint: Point.fromJson(json['departurePoint']),
      rating: (json['rating'] as num).toDouble(),
    );
  }

}

class GamePresentationDetails {
  final String id, label, description;
  final int level;
  final Location departure;
  final double rating;

  GamePresentationDetails({required this.id, required this.label, required this.description,
    required this.level, required this.departure, required this.rating});

  factory GamePresentationDetails.fromJson(Map<String, dynamic> json) {
    return GamePresentationDetails(
      id: json['id'] as String,
      label: json['label'] as String,
      description: json['description'] as String,
      level: json['level'] as int,
      departure: Location.fromJson(json['departure']),
      rating: (json['rating'] as num).toDouble(),
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
    return Point(lat: (json['lat'] as num).toDouble(), lng: (json['lng'] as num).toDouble());
  }
}
