
import 'package:go_router/go_router.dart';

import '../../contexts/game/views/menu/game_menu_view.dart';

class AppRouter {

  static const nameHome = "home";
  static const pathHome = "/";

  static const nameGameMenu = "game-menu";
  static const pathGameMenu = "/game/menu";

  static GoRouter create() {
    return GoRouter(
      redirect: (context, state) {
        return null;
      },
      routes: [
        GoRoute(
          name: nameHome,
          path: pathHome,
          builder: (context, state) => GameMenuView(),
        ),
        GoRoute(
          name: nameGameMenu,
          path: pathGameMenu,
          builder: (context, state) => GameMenuView(),
        )
      ],
    );
  }

}