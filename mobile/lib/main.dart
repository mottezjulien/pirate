import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/generic/app_current.dart';

import 'app.dart';
import 'contexts/config/user/user_repository.dart';
import 'contexts/config/user/user.dart';
import 'contexts/connect/connection_repository.dart';
import 'generic/config/language.dart';

import 'generic/config/router.dart' as router;

import 'package:mobile/generic/components/dialog.dart' as generic_dialog;

Future<void> main() async {

  WidgetsFlutterBinding.ensureInitialized();
  await EasyLocalization.ensureInitialized();
  try {

    final ConnectionRepository connectRepository = ConnectionRepository();
    AppCurrent.userAuth = await connectRepository.createByDeviceId();

    final UserRepository userRepository = UserRepository();
    final User? nullableUser = await userRepository.find();
    if(nullableUser != null) {
      AppCurrent.user = nullableUser;
    }

    final GoRouter _router = router.AppRouter.create();

    runApp(
        EasyLocalization(
          supportedLocales: Language.values
              .map((language) => language.toLocale()).toList(),
          path: 'assets/translations',
          fallbackLocale: Language.byDefault().toLocale(),
          startLocale: Language.byDefault().toLocale(),
          child: App(router: _router),
        )
    );
  } catch(e) {
    generic_dialog.Dialog dialog = generic_dialog.Dialog();
    dialog.showMessage(message: 'Error d\'initialisation'); //TODO
  }

}