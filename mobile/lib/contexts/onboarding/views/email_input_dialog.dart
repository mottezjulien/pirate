import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

class EmailInputDialog extends StatelessWidget {

  final TextEditingController _controller = TextEditingController();

  EmailInputDialog({super.key});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('onboarding.email.title'.tr()),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text('onboarding.email.description'.tr()),
          const SizedBox(height: 16),
          TextField(
            controller: _controller,
            keyboardType: TextInputType.emailAddress,
            decoration: InputDecoration(
              hintText: 'onboarding.email.hint'.tr(),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
            ),
          ),
        ],
      ),
      actions: [
        TextButton(
          child: Text('onboarding.skip'.tr()),
          onPressed: () => Navigator.of(context).pop()),
        ElevatedButton(
          child: Text('onboarding.confirm'.tr()),
          onPressed: () => Navigator.of(context).pop(_controller.text),
        )
      ],
    );
  }

}
