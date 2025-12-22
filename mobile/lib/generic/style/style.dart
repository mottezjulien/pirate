

import 'package:flutter/material.dart';

class Style {

  static final _Instance _instance = _Instance();

  static get themes => _instance.themes;

}

class _Instance {

  final _Themes themes = _Themes();

}

class _Themes {

  final _Theme default_ = _DefaultTheme();

}

abstract class _Theme {

  ThemeMode get mode;

  Color get primaryColor;

  ThemeData value(Brightness brightness);

}

class _DefaultTheme extends _Theme {

  @override
  ThemeMode get mode => ThemeMode.dark;

  @override
  Color get primaryColor => Colors.blueAccent;

  @override
  ThemeData value(Brightness brightness) {
    return ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: primaryColor, brightness: brightness),
        primaryColor: primaryColor,
        useMaterial3: true,
        brightness: brightness
    );
  }

}