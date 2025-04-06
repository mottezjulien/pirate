import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

import 'generic/config/router.dart' as router;

class App extends StatelessWidget {

  const App({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'The Plop 2 App',

      locale: context.locale,
      supportedLocales: context.supportedLocales,
      localizationsDelegates: context.localizationDelegates,

      themeMode: ThemeMode.dark,
      theme: _themeData(Brightness.light),
      darkTheme: _themeData(Brightness.dark),

      routerConfig: router.AppRouter.create()
    );
  }

  ThemeData _themeData(Brightness brightness) {
    return ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepOrange, brightness: brightness),
        useMaterial3: true,
        brightness: brightness
    );
  }

}
