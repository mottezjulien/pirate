import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

import '../../../../generic/config/language.dart';

class LanguageSelectionDialog extends StatelessWidget {

  const LanguageSelectionDialog({super.key});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('onboarding.language.select'.tr()),
      actions: [
        SizedBox(
          width: double.infinity,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              for (final Language language in Language.values)
                selectLanguage(context, language)
            ],
          ),
        )
      ],
    );
  }

  Widget selectLanguage(BuildContext context, Language language) {
    return Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ElevatedButton.icon(
            onPressed: () async {
              //TODO Envoyer la langue de l'utilisateur au back
              //await context.setLocale(language.toLocale());
              Navigator.of(context).pop(language);
            },
            icon: Text(language.icon),
            label: Text(language.label),
          ),
          const SizedBox(height: 8)
        ]
    );
  }

}