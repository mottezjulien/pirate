import 'package:flutter/material.dart';
import 'package:easy_localization/easy_localization.dart';

import '../../../../generic/components/organisms/game_app_bar.dart';
import '../../map/game_map_view.dart';
import '../goal/game_goal_view.dart';

class GameHomeView extends StatelessWidget {

  GameHomeView({super.key});

  final ValueNotifier<int> _selectedIndex = ValueNotifier<int>(0);

  static const List<Widget> _widgetOptions = <Widget>[
    GameGoalTabView(),
    GameMapTabView()
  ];

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<int>(
        valueListenable: _selectedIndex,
        builder: (context, currentIndex, _) => _buildWith(context, currentIndex)
    );
  }

  Widget _buildWith(BuildContext context, int currentIndex) {
    return Scaffold(
      appBar: GameAppBar(),
      body: _widgetOptions.elementAt(currentIndex),
      bottomNavigationBar: BottomNavigationBar(
          items: <BottomNavigationBarItem>[
            BottomNavigationBarItem(icon: const Icon(Icons.checklist), label: 'navigation.objectives'.tr()),
            BottomNavigationBarItem(icon: const Icon(Icons.map), label: 'navigation.maps'.tr()),
          ],
          currentIndex: currentIndex,
          selectedItemColor: Colors.amber[800],
          onTap: (index) => _selectedIndex.value = index),
    );
  }

}


