import 'package:flutter/material.dart';

import '../config/router.dart';

class GlobalDialogService {


  static Future<void> showMessageDialog({
    required String title,
    required String message,
    String buttonText = 'OK',
  }) async {
    final BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context == null) return;

    return showDialog<void>(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text(message),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: Text(buttonText),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  static Future<void> showCustomMessageDialog({
    required String message,
  }) async {
    final BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context == null) return;

    return showDialog<void>(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Row(
            children: [
              Icon(Icons.message, color: Colors.deepOrange),
              SizedBox(width: 8),
              Text('Message du jeu'),
            ],
          ),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text(
                  message,
                  style: const TextStyle(fontSize: 16),
                ),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              style: TextButton.styleFrom(
                foregroundColor: Colors.deepOrange,
              ),
              child: const Text('Fermer'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }
}