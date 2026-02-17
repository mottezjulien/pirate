import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

class NickNameInputDialog extends StatelessWidget {

  final TextEditingController _controller = TextEditingController();

  NickNameInputDialog({super.key});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('onboarding.nickname.title'.tr()),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text('onboarding.nickname.description'.tr()),
          const SizedBox(height: 16),
          TextField(
            controller: _controller,
            decoration: InputDecoration(
              hintText: 'onboarding.nickname.hint'.tr(),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
              ),
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
