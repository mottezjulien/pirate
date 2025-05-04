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
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Objectifs :)'),
            FutureBuilder<List<GameGoal>>(
                future: _viewModel.findGoals(),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    //return Text(snapshot.data.toString());
                    return display(snapshot.data!);
                  } else if (snapshot.hasError) {
                    return Text('Error : ${snapshot.error}');
                  } else {
                    return const Text('Loading...');
                  }
                }),
          ],
        ),
      ),
    );
  }

  Widget display(List<GameGoal> data) {
    return ListView.builder(
        itemCount: data.length,
        itemBuilder: (context, index) {
          return Column(
              children: [
                Text(data[index].label),
                Text(data[index].state.toString()),
                ListView.builder(
                    itemCount: data[index].targets.length,
                    itemBuilder: (context, index) {
                      return Text(data[index].targets[index].label);
                    })
              ]);
        });
  }

}

class _GameGoalViewModel {

  final GameSessionRepository repository = GameSessionRepository();

  Future<List<GameGoal>> findGoals() async {
    return repository.findGoals();
  }


}