import 'package:flutter/material.dart';
import '../../data/game_repository.dart';
import '../../domain/model/game_goal.dart';

class GameGoalView extends StatelessWidget {
  final _GameGoalViewModel _viewModel = _GameGoalViewModel();

  GameGoalView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Objectifs :)'),
      ),
      body: FutureBuilder<List<GameGoal>>(
        future: _viewModel.findGoals(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            return _buildGoalList(context, snapshot.data!);
          } else if (snapshot.hasError) {
            return Center(child: Text('Error : ${snapshot.error}'));
          } else {
            return const Center(child: CircularProgressIndicator());
          }
        },
      ),
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
        ...activeGoals.map((goal) => _buildGoalItem(context, goal)).toList(),
        if (completedGoals.isNotEmpty) _buildCompletedSection(context, completedGoals),
      ],
    );
  }

  Widget _buildCompletedSection(BuildContext context, List<GameGoal> completedGoals) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const Padding(
          padding: EdgeInsets.symmetric(vertical: 16.0),
          child: Text(
            'Terminé',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
          ),
        ),
        ...completedGoals.map((goal) => _buildGoalItem(context, goal)).toList(),
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
    // Sort targets: not done first, then done, and optional indication
    targets.sort((a, b) {
      if (a.done == b.done) return a.optional ? 1 : -1;
      return a.done ? 1 : -1;
    });

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
  final GameSessionRepository repository = GameSessionRepository();

  Future<List<GameGoal>> findGoals() async {
    return repository.findGoals();
  }
}