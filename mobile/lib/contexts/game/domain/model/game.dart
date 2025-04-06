class Game {

  final String _id, _label;

  Game({required String id, required String label})
      : _id = id, _label = label;

  String get id => _id;

  String get label => _label;

}