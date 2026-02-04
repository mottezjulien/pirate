import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../generic/app_current.dart';
import '../../../generic/config/router.dart';
import '../game_presentation.dart';
import '../game_presentation_repository.dart';

class GamePresentationCardWidget extends StatelessWidget {
  final GamePresentationSimple presentation;
  final GamePresentationRepository _repository = GamePresentationRepository();

  GamePresentationCardWidget({super.key, required this.presentation});

  void _showTemplateDetailsDialog(BuildContext context) async {
    final details = await _repository.findById(presentation.id);
    if (!context.mounted) return;

    showDialog(
      context: context,
      builder: (BuildContext dialogContext) => AlertDialog(
        title: Text(details.label),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                'config.select.template_details.description'.tr(),
                style: Theme.of(dialogContext).textTheme.titleSmall,
              ),
              const SizedBox(height: 8),
              Text(details.description),
              const SizedBox(height: 16),
              Text(
                'config.select.template_details.departure'.tr(),
                style: Theme.of(dialogContext).textTheme.titleSmall,
              ),
              const SizedBox(height: 8),
              Text(details.departure.address),
              const SizedBox(height: 16),
              Text(
                'config.select.template_details.level'.tr(),
                style: Theme.of(dialogContext).textTheme.titleSmall,
              ),
              const SizedBox(height: 8),
              Row(
                children: List.generate(
                  5,
                  (starIndex) => Icon(
                    starIndex < details.level ? Icons.star : Icons.star_outline,
                    size: 20,
                    color: Colors.amber,
                  ),
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(),
            child: Text('config.select.template_details.cancel'.tr()),
          ),
          ElevatedButton(
            onPressed: () {
              AppCurrent.templateId = details.id;
              Navigator.of(dialogContext).pop();
              context.goNamed(AppRouter.homeName);
            },
            child: Text('config.select.template_details.select'.tr()),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).cardColor,
        border: Border.all(color: Colors.grey[300]!),
        borderRadius: BorderRadius.circular(8),
      ),
      child: GestureDetector(
        onTap: () => _showTemplateDetailsDialog(context),
        child: ListTile(
          title: Text(presentation.label),
          subtitle: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 4),
              Text(
                presentation.departureAddress,
                style: const TextStyle(
                  fontSize: 12,
                  color: Colors.grey,
                  fontStyle: FontStyle.italic,
                ),
              ),
              const SizedBox(height: 8),
              Row(
                children: List.generate(
                  5,
                  (starIndex) => Icon(
                    starIndex < presentation.level ? Icons.star : Icons.star_outline,
                    size: 16,
                    color: Colors.amber,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
