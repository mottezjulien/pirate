
import 'package:flutter/cupertino.dart';
import 'package:go_router/go_router.dart';

import '../../contexts/config/select/game_config_select_view.dart';
import '../../contexts/onboarding/views/onboarding_screen.dart';
import '../../contexts/session/views/home/game_home_view.dart';
import '../../contexts/session/views/menu/game_menu_view.dart';
import '../app_current.dart';

class AppRouter {

  static final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

  static const homeName = "home";
  static const homePath = "/";

  static const onboardingName = "onboarding";
  static const onboardingPath = "/onboarding";

  static const selectGameName = "selectGame";
  static const selectGamePath = "/selectGame";

  static const gameMenuName = "session-menu";
  static const gameMenuPath = "/session/menu";

  static const gameHomeName = "session-home";
  static const gameHomePath = "/session/home";


  static GoRouter create() {
    String initialLocation = selectGamePath;//homePath;
    if(!AppCurrent.hasUser) {
      initialLocation = onboardingPath;
    }
    return GoRouter(
      navigatorKey: navigatorKey,
      initialLocation: initialLocation,
      redirect: (context, state) {
        return null;
      },
      routes: [
        ShellRoute(
            builder: (BuildContext context, GoRouterState state, Widget child) {
              return SafeArea(
                child: child,
              );
            },
            routes: [
              GoRoute(
                name: onboardingName,
                path: onboardingPath,
                builder: (context, state) => OnboardingScreen(),
              ),
              GoRoute(
                name: selectGameName,
                path: selectGamePath,
                builder: (context, state) => GameConfigSelectView(),
              ),
              GoRoute(
                name: homeName,
                path: homePath,
                builder: (context, state) => GameMenuView(),
              ),
              GoRoute(
                name: gameMenuName,
                path: gameMenuPath,
                builder: (context, state) => GameMenuView(),
              ),
              GoRoute(
                name: gameHomeName,
                path: gameHomePath,
                builder: (context, state) => GameHomeView(),
              )
          ]
        )
      ]
    );
  }

}