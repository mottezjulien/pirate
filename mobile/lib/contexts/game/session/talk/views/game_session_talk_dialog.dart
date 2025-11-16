
import 'package:easy_localization/easy_localization.dart';

import '../../../../../generic/config/router.dart';
import '../../../../../generic/dialog.dart';
import '../../../game_current.dart';
import '../data/game_session_talk_repository.dart';
import '../game_talk.dart';
import 'package:flutter/material.dart' hide Dialog;

class GameSessionTalkDialog {

  final GameSessionTalkRepository repository = GameSessionTalkRepository();
  final Dialog dialog = Dialog();
  final ValueNotifier<GameTalk?> notifier = ValueNotifier(null);

  void start({required String talkId}) {
    final BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context != null) {
      notifier.value = null;
      final AlertDialog alertDialog = AlertDialog(
        content: ValueListenableBuilder<GameTalk?>(
            valueListenable: notifier,
            builder: (BuildContext context, GameTalk? talk, Widget? child) {
              return build(context, talk);
            }
        ),
      );

      dialog.showWidget(dialog: alertDialog, paramContext: context);
      repository.findById(talkId).then((talk) => notifier.value = talk);
    }


  }

  Widget build(BuildContext context, GameTalk? talk) {
    if (talk == null) {
      return Center(child: CircularProgressIndicator());
    }

    List<Widget> buttonChildren = [];
    switch(talk.result) {
      case GameTalkResultSimple():
        buttonChildren.add(TextButton(child: Text('default.close'.tr()),
          onPressed: () => Navigator.of(context).pop()));
        break;
      case GameTalkResultContinue(:final nextId):
        buttonChildren.add(TextButton(child: Text('continue pouet'.tr()),
            onPressed: () {
              notifier.value = null;
              repository.findById(nextId).then((talk) => notifier.value = talk);
            })
        );
        break;
      case GameTalkResultMultiple(:final options):
        for (var option in options) {
          buttonChildren.add(
            TextButton(style: TextButton.styleFrom(
              foregroundColor: GameCurrent.style.color.primary,
              padding: EdgeInsets.symmetric(vertical: 12),
            ),
              child: Text(
                option.value,
                style: TextStyle(fontSize: 16),
              ),
              onPressed: () {
                notifier.value = null;
                repository.selectOption(talkId: talk.id, optionId: option.id)
                    .then((nextTalk) {
                  if(nextTalk != null) {
                    notifier.value = nextTalk;
                  } else {
                    Navigator.of(context).pop();
                  }
                });
              }
            )
          );
          buttonChildren.add(SizedBox(height: 8));
        }
        break;
    }

    return SingleChildScrollView(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            talk.value,
            style: TextStyle(fontSize: 16),
          ),
          SizedBox(height: 24),
          ...buttonChildren,
        ],
      ),
    );
  }

}
