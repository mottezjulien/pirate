import 'package:flutter/material.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:go_router/go_router.dart';

import '../../../../generic/config/router.dart';
import '../../domain/game_session_usecase.dart';
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
            title: Text('menu.title'.tr())),
        body: Center(
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
          if (!_viewModel.hasGame)
            TextButton(
                style: buttonStyle(colorScheme),
                onPressed: () => _viewModel.start()
                    .then((value) => context.goNamed(AppRouter.gameHomeName)),
                child: Text('menu.start'.tr())),
          if (_viewModel.hasGame)
            TextButton(
                style: buttonStyle(colorScheme),
                onPressed: () => context.goNamed(AppRouter.gameHomeName),
                child: Text('menu.continue'.tr())),
          TextButton(onPressed: () {}, child: Text('menu.configuration'.tr())),
          TextButton(onPressed: () {}, child: Text('menu.exit'.tr())),
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

  bool get hasGame => GameCurrent.hasSession;

  Future<void> start() async {
    await sessionUseCase.start();
  }

}
