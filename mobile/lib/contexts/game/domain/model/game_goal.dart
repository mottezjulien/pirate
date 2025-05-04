
class GameGoal {

  //String id, String label, String state, List<GameTargetSimpleResponseDTO> targets

  final String id;
  final String label;
  final GameGoalState state;
  final List<GameTarget> targets;

  GameGoal({required this.id, required this.label, required this.state, required this.targets});

}

enum GameGoalState {
  ACTIVE, SUCCESS, FAILURE
}

class GameTarget {

  final String id;
  final String label;

  GameTarget({required this.id, required this.label});

}