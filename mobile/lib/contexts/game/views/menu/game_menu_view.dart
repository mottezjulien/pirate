import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../generic/config/router.dart';
import '../../domain/game_session.dart';
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
                onPressed: () => _viewModel.start()
                    .then((value) => context.goNamed(AppRouter.gameHomeName)),
                child: const Text('Démarrer :)')),
          if (_viewModel.hasGame)
            TextButton(
                style: buttonStyle(colorScheme),
                onPressed: () => context.goNamed(AppRouter.gameHomeName),
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

  final GameSessionUseCase sessionUseCase = GameSessionUseCase();

  bool get hasGame => GameSessionCurrent.hasSession;

  Future<void> start() async {
    await sessionUseCase.start();
  }

}
