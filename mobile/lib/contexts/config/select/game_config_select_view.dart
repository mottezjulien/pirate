import 'package:flutter/material.dart';
import 'widgets/game_config_select_map_tab_widget.dart';
import 'widgets/game_config_select_text_input_code_tab_widget.dart';

class GameConfigSelectView extends StatelessWidget {
  const GameConfigSelectView({super.key});

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('sélectionner votre jeux'),
        ),
        body: const Column(
          children: [
            TabBar(
              tabs: [
                Tab(text: 'Code'),
                Tab(text: 'Carte'),
              ],
            ),
            Expanded(
              child: TabBarView(
                children: [
                  GameConfigSelectTextInputCodeTabWidget(),
                  GameConfigSelectMapTabWidget(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
