import 'package:flutter/material.dart';

import '../../domain/game_settings_usecase.dart';
import '../../game_current.dart';

class GameMenuView extends StatelessWidget {

  final GameMenuViewModel _viewModel = GameMenuViewModel();

  GameMenuView({super.key});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Scaffold(
        appBar: AppBar(
            backgroundColor: colorScheme.inversePrimary,
            title: Text("Game Menu :)")),
        body: Center(
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
          if (!_viewModel.hasGame)
            TextButton(
                style: buttonStyle(colorScheme),
                onPressed: () => _viewModel.start(),
                child: const Text('Démarrer :)')),
          if (_viewModel.hasGame)
            TextButton(
                style: buttonStyle(colorScheme),
                onPressed: () => _viewModel.continueGame(),
                child: const Text('Continuer :)')),
          TextButton(onPressed: () {}, child: const Text('Configuration :)')),
          TextButton(onPressed: () {}, child: const Text('Sortir :)')),
        ])));
  }

  ButtonStyle buttonStyle(ColorScheme colorScheme) {
    return TextButton.styleFrom(
                shape: RoundedRectangleBorder(
                    borderRadius: const BorderRadius.all(Radius.circular(16)),
                    side: BorderSide(color: colorScheme.primary, width: 3)));
  }
}

class GameMenuViewModel {

  bool get hasGame => GameCurrent.hasGame;

  void start() {
    GameSettingsUseCase().apply();
  }

  void continueGame() {

  }

}
