
import 'package:easy_localization/easy_localization.dart';

import '../../../../../generic/config/router.dart';
import '../../../../../generic/dialog.dart';
import '../../../game_current.dart';
import '../data/game_session_talk_repository.dart';
import 'package:flutter/material.dart' hide Dialog;

import '../game_talk.dart';

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
      return SizedBox(
        height: 80,
        width: 80,
        child: Center(child: CircularProgressIndicator()),
      );
    }
    return SingleChildScrollView(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: 100,
                height: 120,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(8),
                  color: Colors.grey[200],
                ),
                child: _buildCharacterImage(talk.character.image),
              ),
              SizedBox(width: 16),
              Expanded(
                child: Padding(
                  padding: EdgeInsets.symmetric(vertical: 8),
                  child: Text(
                    talk.value,
                    style: TextStyle(fontSize: 16, height: 1.5),
                  ),
                ),
              ),
            ],
          ),
          SizedBox(height: 24),
          ...buttons(talk, context),
        ],
      ),
    );
  }

  Widget? _buildCharacterImage(GameImage image) {
    return ClipRRect(
        borderRadius: BorderRadius.circular(8),
        child: image.toWidget(fit: BoxFit.cover));
  }

  List<Widget> buttons(GameTalk talk, BuildContext context) {
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
    return buttonChildren;
  }



}
