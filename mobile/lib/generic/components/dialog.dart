import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

import '../../contexts/session/game_current.dart';
import '../config/router.dart';

class Dialog {

  BuildContext? showMessage({required String message, bool isClosable = true }) {
    BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context == null) {
      return null;
    }
    showWidget(dialog: AlertDialog(
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
    return context;
  }

  Future<void> showWidget({required AlertDialog dialog, BuildContext? paramContext }) {
    final BuildContext? context = paramContext ?? AppRouter.navigatorKey.currentContext;
    if (context != null) {
      return showDialog<void>(
        context: context,
        barrierDismissible: false,
        builder: (BuildContext context) {
          return dialog;
        },
      );
    }
    return Future.value();
  }

  Future<bool?> showConfirm({required String message}) async {
    BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context == null) {
      return null;
    }
    return showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext context) {
        return AlertDialog(
          content: SingleChildScrollView(child: ListBody(children: [Text(message)])),
          actions: [
            TextButton(
              style: TextButton.styleFrom(foregroundColor: GameCurrent.style.color.primary),
              child: Text('default.no'.tr()),
              onPressed: () => Navigator.of(context).pop(false),
            ),
            TextButton(
              style: TextButton.styleFrom(foregroundColor: GameCurrent.style.color.primary),
              child: Text('default.yes'.tr()),
              onPressed: () => Navigator.of(context).pop(true),
            ),
          ],
        );
      },
    );
  }

}