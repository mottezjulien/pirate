import 'package:flutter/material.dart';
import 'package:easy_localization/easy_localization.dart';
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
          title: Text('config.select.title'.tr()),
        ),
        body: Column(
          children: [
            TabBar(
              tabs: [
                Tab(child: Text('config.select.code_tab'.tr())),
                Tab(child: Text('config.select.map_tab'.tr())),
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
