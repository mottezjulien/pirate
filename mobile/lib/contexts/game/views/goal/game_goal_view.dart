

import 'package:flutter/material.dart';

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
          ],
        ),
      ),
    );
  }
}

class _GameGoalViewModel {

}