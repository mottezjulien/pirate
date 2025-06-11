
import 'package:flutter/material.dart';

class Style {
  //static const String fontFamily = 'Roboto';

  BuildContext _context;

  Style({required BuildContext context}): _context = context;

  static _Color color = _Color();

  static _Dimension dimension = _Dimension();

  static _Duration duration = _Duration();

}

class _Color {

  Color background = Colors.blueAccent;

  Color oneFirstPurple = Colors.purple;

  Color get lightGrey => Color(0x61D1D1D1);

}

class _Dimension {

  final double small = 4.0;
  final double medium = 8.0;
  final double large = 12.0;
  final double extraLarge = 16.0;
  final double xxLarge = 20.0;
  final double xxxLarge = 24.0;

}

class _Duration {

  Duration get default_ => Duration(milliseconds: 300);

}