import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

import '../contexts/game/game_current.dart';
import 'config/router.dart';

class Dialog {

  Future<void> showMessage({required String message,
    bool isClosable = true }) async {
    final BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context == null) {
      return Future.value();
    }
    return await showWidget(dialog: AlertDialog(
      content: SingleChildScrollView(child: ListBody(children: [Text(message)])),
      actions: [
        if(isClosable)
          TextButton(
              style: TextButton.styleFrom(foregroundColor: GameCurrent.style.color.primary),
              child: Text('default.close'.tr()),
              onPressed: () => Navigator.of(context).pop()
          )
      ],
    ));
  }

  Future<void> showWidget({required AlertDialog dialog, BuildContext? paramContext }) {
    final BuildContext? context = paramContext ?? AppRouter.navigatorKey.currentContext;
    if (context != null) {
      return showDialog<void>(
        context: context,
        barrierDismissible: true,
        builder: (BuildContext context) {
          return dialog;
        },
      );
    }
    return Future.value();
  }

}