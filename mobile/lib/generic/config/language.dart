import 'dart:ui';

enum Language {
  en,
  fr;

  static Language? valueOf(String value) {
    for(Language element in Language.values) {
      if(element.toString() == value) {
        return element;
      }
    }
    return null;
  }

  static Language byDefault() {
    return Language.fr;
  }

  Locale toLocale() {
    switch(this) {
      case Language.en:
        return const Locale('en');
      case Language.fr:
        return const Locale('fr');
    }
  }

  String toValue() {
    switch(this) {
      case Language.en:
        return 'EN';
      case Language.fr:
        return 'FR';
    }
  }

  String get icon => switch(this) {
    Language.en => '🇬🇧',
    Language.fr => '🇫🇷',
  };

  String get label => switch(this) {
    Language.en => 'English',
    Language.fr => 'Français',
  };



}