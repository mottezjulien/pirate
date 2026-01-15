
import 'package:flutter/material.dart';

class GameAppBar extends AppBar {
  GameAppBar({super.key}): super(
          toolbarHeight: 64,
      flexibleSpace: Image.asset('assets/default/icon.png'),
      actions: [
        IconButton(
            iconSize: 48,
            icon: Icon(Icons.support), onPressed: () {})
      ]
  );
}
