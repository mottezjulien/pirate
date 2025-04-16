
import 'package:go_router/go_router.dart';

import '../../contexts/game/views/goal/game_goal_view.dart';
import '../../contexts/game/views/home/game_home_view.dart';
import '../../contexts/game/views/menu/game_menu_view.dart';

class AppRouter {

  static const homeName = "home";
  static const homePath = "/";

  static const gameMenuName = "game-menu";
  static const gameMenuPath = "/game/menu";

  static const gameHomeName = "game-home";
  static const gameHomePath = "/game/home";

  static const gameGoalName = "game-goal";
  static const gameGoalPath = "/game/goal";

  static GoRouter create() {
    return GoRouter(
      redirect: (context, state) {
        return null;
      },
      routes: [
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
        ),
        GoRoute(
          name: gameGoalName,
          path: gameGoalPath,
          builder: (context, state) => GameGoalView(),
        ),


      ],
    );
  }

}