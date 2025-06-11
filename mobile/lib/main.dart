import 'package:easy_localization/easy_localization.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:mobile/resources/firebase/firebase_options.dart';

import 'app.dart';
import 'generic/config/language.dart';
import 'generic/connect/connection_start_usecase.dart';

Future<void> main() async {

  WidgetsFlutterBinding.ensureInitialized();
  await EasyLocalization.ensureInitialized();

  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);

  await FirebaseMessaging.instance.requestPermission();

  FirebaseMessaging.onMessage.listen((RemoteMessage message) {
    print('onMessage: ${message.messageId}');
  });

  FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
    print('onMessageOpenedApp: ${message.messageId}');
  });

  await FirebaseMessaging.instance.getInitialMessage().then((RemoteMessage? message) {
    print('getInitialMessage: ${message?.messageId}');
  });



  final ConnectionStartUseCase startUseCase = ConnectionStartUseCase();
  startUseCase.apply();


  runApp(
      EasyLocalization(
        supportedLocales: Language.values
            .map((language) => language.toLocale()).toList(),
        path: 'assets/translations',
        fallbackLocale: Language.byDefault().toLocale(),
        //startLocale: AuthSettings.language().toLocale(),
        startLocale: Language.byDefault().toLocale(),

        child: const App(),
      )
  );
}