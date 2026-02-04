import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:upgrader/upgrader.dart';

import 'generic/style/style.dart';

class App extends StatelessWidget {

  final GoRouter router;

  const App({super.key, required this.router});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Locked Out',

      locale: context.locale,
      supportedLocales: context.supportedLocales,
      localizationsDelegates: context.localizationDelegates,

      themeMode: Style.themes.default_.mode,
      theme: Style.themes.default_.value(Brightness.light),
      darkTheme: Style.themes.default_.value(Brightness.dark),
      builder: (context, child) {
        return UpgradeAlert(child: child);
      },
      routerConfig: router);
  }

}
