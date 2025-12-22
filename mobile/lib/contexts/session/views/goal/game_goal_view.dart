import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import '../../data/game_repository.dart';
import '../../domain/model/game_goal.dart';
import '../../domain/model/game_session.dart';
import '../../game_current.dart';

class GameGoalTabView extends StatefulWidget {

  const GameGoalTabView({super.key});

  @override
  State<GameGoalTabView> createState() => _GameGoalTabViewState();

}

class _GameGoalTabViewState extends State<GameGoalTabView> implements OnGoalListener {
  final _GameGoalViewModel _viewModel = _GameGoalViewModel();

  @override
  initState() {
    super.initState();
    _viewModel.setGoals();
    GameCurrent.addOnGoalListener(this);
  }

  @override
  dispose() {
    super.dispose();
    GameCurrent.removeOnGoalListener(this);
  }

  @override
  void onUpdateGoal() => _viewModel.setGoals();

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<List<GameGoal>>(
        valueListenable: _viewModel.valueNotifier,
        builder: (BuildContext context, List<GameGoal> goals, Widget? _) {
          if (goals.isNotEmpty) {
            return _buildGoalList(context, goals);
          } else {
            return const Center(child: CircularProgressIndicator());
          }
        }
    );
  }

  Widget _buildGoalList(BuildContext context, List<GameGoal> goals) {
    // Separate active and completed goals
    List<GameGoal> activeGoals =
    goals.where((goal) => goal.state == GameGoalState.active).toList();
    activeGoals.sort((a, b) => a.label.compareTo(b.label));

    List<GameGoal> completedGoals =
    goals.where((goal) => goal.state != GameGoalState.active).toList();

    return ListView(
      padding: const EdgeInsets.all(8.0),
      children: [
        ...activeGoals.map((goal) => _buildGoalItem(context, goal)),
        if (completedGoals.isNotEmpty) _buildCompletedSection(context, completedGoals),
      ],
    );
  }

  Widget _buildCompletedSection(BuildContext context, List<GameGoal> completedGoals) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Padding(
          padding: EdgeInsets.symmetric(vertical: 16.0),
          child: Text('scenario.goal.finished'.tr(),
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
          ),
        ),
        ...completedGoals.map((goal) => _buildGoalItem(context, goal)),
      ],
    );
  }

  Widget _buildGoalItem(BuildContext context, GameGoal goal) {
    return Card(
      elevation: 2,
      margin: const EdgeInsets.symmetric(vertical: 8.0),
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: EdgeInsets.symmetric(vertical: 8.0),
              child: Text(
                goal.label,
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
            ),
            const SizedBox(height: 8.0),
            _buildTargetList(goal.targets),
          ],
        ),
      ),
    );
  }

  Widget _buildTargetList(List<GameTarget> targets) {
    return Column(
      children: targets.map((target) {
        return ListTile(
          leading: Icon(
            target.done ? Icons.check_circle_outline : Icons.radio_button_unchecked,
            color: target.done ? Colors.green : Colors.grey,
          ),
          title: Text(
            target.label + (target.optional ? ' (Optionnel)' : ''),
            style: TextStyle(
              decoration: target.done ? TextDecoration.lineThrough : TextDecoration.none,
            ),
          ),
        );
      }).toList(),
    );
  }
}

class _GameGoalViewModel {

  final ValueNotifier<List<GameGoal>> valueNotifier = ValueNotifier([]);

  final GameSessionRepository repository = GameSessionRepository();

  Future<void> setGoals() async {
    valueNotifier.value = await repository.findGoals();
  }
}