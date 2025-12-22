
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/src/foundation/change_notifier.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/generic/app_current.dart';
import 'package:mobile/generic/config/language.dart';

import '../../../generic/config/router.dart';
import '../user/data/user_repository.dart';

class OnboardingViewModel {

  final ValueNotifier<int> _currentIndex = ValueNotifier<int>(0);

  final PageController pageController = PageController();

  final List<OnboardingPage> pages = [
    OnboardingPage(
      title: 'onboarding.welcome.title'.tr(),
      description: 'onboarding.welcome.description'.tr(),
      icon: '👋',
    ),
    OnboardingPage(
      title: 'onboarding.development.title'.tr(),
      description: 'onboarding.development.description'.tr(),
      icon: '🚀',
    ),
    OnboardingPage(
      title: 'onboarding.help.title'.tr(),
      description: 'onboarding.help.description'.tr(),
      icon: '🛟',
    ),
  ];

  ValueListenable<int> get index => _currentIndex;

  set indexValue(int index) {
    _currentIndex.value = index;
  }

  void setUser({required Language language, String? nickName, String? email}) {
    final UserRepository userRepository = UserRepository();
    userRepository.onBoard(language: language, nickName: nickName, email: email)
    .then((user) => AppCurrent.user = user);
  }

  void next() {
    if (isLastPage()) {
      _completeOnboarding();
    } else {
      pageController.nextPage(duration: Duration(milliseconds: 300), curve: Curves.ease);
    }
  }

  bool isLastPage() => index.value == pages.length - 1;

  void _completeOnboarding() {
    BuildContext? context = AppRouter.navigatorKey.currentContext;
    context!.goNamed(AppRouter.homeName);
  }



}

class OnboardingPage {
  final String title;
  final String description;
  final String icon;

  OnboardingPage({
    required this.title,
    required this.description,
    required this.icon,
  });
}