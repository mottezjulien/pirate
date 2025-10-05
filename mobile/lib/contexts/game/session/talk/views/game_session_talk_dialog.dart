
import '../../../../../generic/dialog.dart';
import '../data/game_session_talk_repository.dart';
import '../game_talk.dart';
import 'package:flutter/material.dart' hide Dialog;

class GameSessionTalkDialog {

  final GameSessionTalkRepository repository = GameSessionTalkRepository();
  final Dialog dialog = Dialog();

  void start({required String talkId}) {
    final ValueNotifier<GameTalk?> notifier = ValueNotifier(null);
    final builder = ValueListenableBuilder<GameTalk?>(valueListenable: notifier,
    builder: (BuildContext context, GameTalk? talk, Widget? _) {
      if (talk == null) {
        return CircularProgressIndicator();
      } else {
        return Text(talk.value);
      }
    });
    dialog.showWidget(widget: builder);
    repository.findById(talkId).then((talk) => notifier.value = talk);
  }

}