
class GameGoal {
  final String id;
  final String label;
  final GameGoalState state;
  final List<GameTarget> targets;

  GameGoal({required this.id, required this.label, required this.state, required this.targets});

}

enum GameGoalState {
  active, success, failure;
  factory GameGoalState.fromJson(String json) {
    String value = json.toLowerCase();
    return GameGoalState.values.where((e) => e.name == value).firstOrNull ?? GameGoalState.active;
  }
}

class GameTarget {

  final String id;
  final String label;
  final bool done;
  final bool optional;

  GameTarget({required this.id, required this.label,
    this.done = false, this.optional = false});

}