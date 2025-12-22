
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';

import '../../../../generic/config/language.dart';
import '../onboarding_viewmodel.dart';
import 'email_input_dialog.dart';
import 'language_selection_dialog.dart';
import 'nickname_input_dialog.dart';

class OnboardingScreen extends StatefulWidget {

  OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {

  final OnboardingViewModel _viewModel = OnboardingViewModel();

  @override
  void initState() {
    super.initState();
    SchedulerBinding.instance.addPostFrameCallback((_) async {
      Language? language = await showDialog<Language>(context: context, barrierDismissible: false,
          builder: (context) => const LanguageSelectionDialog());
      await context.setLocale(language!.toLocale()); //force easy localization to reload

      var nickName = await showDialog<String>(context: context, barrierDismissible: false,
          builder: (context) => NickNameInputDialog());
      var email = await showDialog<String>(context: context, barrierDismissible: false,
          builder: (context) => EmailInputDialog());
      _viewModel.setUser(language: language, nickName: nickName, email: email);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Expanded(
            child: PageView.builder(
              controller: _viewModel.pageController,
              itemCount: _viewModel.pages.length,
              physics: const NeverScrollableScrollPhysics(),
              onPageChanged: (index) => _viewModel.indexValue = index,
              itemBuilder: (context, index) => _buildPage(_viewModel.pages[index]),
            )
          ),
          Padding(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              children: [
                ValueListenableBuilder(valueListenable: _viewModel.index, builder: (context, currentIndex, _) {
                  return Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(
                      _viewModel.pages.length,
                          (index) => Container(
                        margin: const EdgeInsets.symmetric(horizontal: 4),
                        width: currentIndex == index ? 16 : 8,
                        height: 8,
                        decoration: BoxDecoration(
                          color: currentIndex == index
                              ? Theme.of(context).primaryColor
                              : Colors.grey[300],
                          borderRadius: BorderRadius.circular(4),
                        ),
                      ),
                    ),
                  );
                })

                ,
                const SizedBox(height: 24),
                Row(
                  children: [
                    Expanded(
                      child: ElevatedButton(
                        onPressed: () => _viewModel.next(),
                        child: Text(_viewModel.isLastPage()
                              ? 'onboarding.finish'.tr()
                              : 'onboarding.next'.tr()),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPage(OnboardingPage page) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(page.icon,
          style: const TextStyle(fontSize: 80),
        ),
        const SizedBox(height: 32),
        Text(page.title,
          style: const TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.bold,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 16),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24.0),
          child: Text(page.description,
            style: const TextStyle(fontSize: 16,
              color: Colors.grey,
              height: 1.5,
            ),
            textAlign: TextAlign.center)
        ),
      ],
    );
  }
}


