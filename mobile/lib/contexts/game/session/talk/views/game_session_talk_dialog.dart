
import 'package:easy_localization/easy_localization.dart';

import '../../../../../generic/dialog.dart';
import '../../../game_current.dart';
import '../data/game_session_talk_repository.dart';
import '../game_talk.dart';
import 'package:flutter/material.dart' hide Dialog;

class GameSessionTalkDialog {

  final GameSessionTalkRepository repository = GameSessionTalkRepository();
  final Dialog dialog = Dialog();

  void start({required String talkId}) {
    final ValueNotifier<GameTalk?> notifier = ValueNotifier(null);
    
    final widgetBuilder = ValueListenableBuilder<GameTalk?>(
      valueListenable: notifier,
      builder: (BuildContext context, GameTalk? talk, Widget? _) {
        if (talk == null) {
          return Center(child: CircularProgressIndicator());
        }

        List<Widget> buttonChildren = [];
        
        // Ajouter les options si multiple
        switch(talk.result) {
          todo
        }


        if (talk.result.isMultiple()) {
          talk.result.options.forEach((option) {
            buttonChildren.add(
              TextButton(
                style: TextButton.styleFrom(
                  foregroundColor: GameCurrent.style.color.primary,
                  padding: EdgeInsets.symmetric(vertical: 12),
                ),
                child: Text(
                  option.value,
                  style: TextStyle(fontSize: 16),
                ),
                onPressed: () {
                  print(option.id);
                  notifier.value = null;
                  repository.selectOption(talkId: talkId, optionId: option.id)
                      .then((nextTalk) {
                    if(nextTalk != null) {
                      notifier.value = nextTalk;
                    } else {
                      Navigator.of(context).pop();
                    }
                  });
                },
              ),
            );
            buttonChildren.add(SizedBox(height: 8));
          });
        }

        return SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                talk.value,
                style: TextStyle(fontSize: 16),
              ),
              if (talk.result.isMultiple()) ...[
                SizedBox(height: 24),
                ...buttonChildren,
              ],
            ],
          ),
        );
      },
    );

    final AlertDialog alertDialog = AlertDialog(
      content: widgetBuilder,
      actions: [
        TextButton(
          style: TextButton.styleFrom(
            foregroundColor: GameCurrent.style.color.primary,
          ),
          child: Text('default.close'.tr()),
          onPressed: () {
            // Navigator.of(context).pop();
          },
        )
      ],
    );

    dialog.showWidget(dialog: alertDialog);
    repository.findById(talkId).then((talk) => notifier.value = talk);
  }

}