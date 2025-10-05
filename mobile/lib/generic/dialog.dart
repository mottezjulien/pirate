import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

import '../contexts/game/game_current.dart';
import 'config/router.dart';

class Dialog {

  Future<void> showMessage({required String message}) async {
    return await showWidget(widget: Text(message));
    /*final BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context == null) return;
    return showDialog<void>(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Row(
            children: [
              Icon(Icons.message, color: GameCurrent.style.color.primary),
              const SizedBox(width: 8),
              const Text('Pouet Message'), //TODO
            ],
          ),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text(message, style: TextStyle(fontSize: GameCurrent.style.dimension.extraLarge)),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              style: TextButton.styleFrom(
                foregroundColor: GameCurrent.style.color.primary),
              child: Text('default.close'.tr()),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );*/
  }

  Future<void> showWidget({String? title, required Widget widget}) {
    final BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context != null) {
      return showDialog<void>(
        context: context,
        barrierDismissible: true,
        builder: (BuildContext context) {
          return AlertDialog(
            title: title != null ? buildTitle(title) : null,
            content: SingleChildScrollView(child: ListBody(children: <Widget>[widget])),
            actions: <Widget>[
              TextButton(
                  style: TextButton.styleFrom(foregroundColor: GameCurrent.style.color.primary),
                  child: Text('default.close'.tr()),
                  onPressed: () => Navigator.of(context).pop()
              ),
            ],
          );
        },
      );
    }
    return Future.value();
  }



  Widget buildTitle(String title) {
    return Row(
      children: [
        Icon(Icons.message, color: GameCurrent.style.color.primary),
        SizedBox(width: GameCurrent.style.dimension.medium), Text(title)
      ],
    );
  }
}