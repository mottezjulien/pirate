import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

import 'app.dart';
import 'generic/config/language.dart';
import 'generic/connect/connection_start_usecase.dart';

Future<void> main() async {

  WidgetsFlutterBinding.ensureInitialized();
  await EasyLocalization.ensureInitialized();

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