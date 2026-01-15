import 'package:flutter/material.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:go_router/go_router.dart';
import 'package:shimmer/shimmer.dart';
import '../../template/game_config_template.dart';
import '../../template/template_repository.dart';
import 'package:mobile/generic/app_current.dart';
import 'package:mobile/generic/config/router.dart';

class GameConfigSelectTextInputCodeTabWidget extends StatefulWidget {
  const GameConfigSelectTextInputCodeTabWidget({super.key});

  @override
  State<GameConfigSelectTextInputCodeTabWidget> createState() =>
      _GameConfigSelectTextInputCodeTabWidgetState();
}

class _GameConfigSelectTextInputCodeTabWidgetState
    extends State<GameConfigSelectTextInputCodeTabWidget> {
  final GameConfigTemplateRepository _repository = GameConfigTemplateRepository();
  final TextEditingController _codeController = TextEditingController();
  final ValueNotifier<List<GameConfigTemplateSimple>> _templatesNotifier =
      ValueNotifier<List<GameConfigTemplateSimple>>([]);
  final ValueNotifier<bool> _isLoadingNotifier = ValueNotifier<bool>(false);

  @override
  void dispose() {
    _codeController.dispose();
    _templatesNotifier.dispose();
    _isLoadingNotifier.dispose();
    super.dispose();
  }

  void _onValidate() {
    if (_codeController.text.isNotEmpty) {
      _isLoadingNotifier.value = true;
      _repository.findByCode(_codeController.text).then((value) {
        _templatesNotifier.value = value;
        _isLoadingNotifier.value = false;
      }).catchError((error) {
        _isLoadingNotifier.value = false;
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('${'config.select.error'.tr()}: $error')),
          );
        }
      });
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('config.select.error_empty_code'.tr())),
      );
    }
  }

  void _showTemplateDetailsDialog(GameConfigTemplateSimple template) async {
    final details = await _repository.findById(template.id);
    if (!mounted) return;
    
    final parentContext = context;
    
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
            child: Text('${'config.select.template_details.cancel'.tr()}'),
          ),
          ElevatedButton(
            onPressed: () {
              AppCurrent.templateId = details.id;
              Navigator.of(dialogContext).pop();
              parentContext.goNamed(AppRouter.homeName);
            },
            child: Text('${'config.select.template_details.select'.tr()}'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const SizedBox(height: 16),
          TextField(
            controller: _codeController,
            decoration: InputDecoration(
              labelText: 'config.select.code_label'.tr(),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              hintText: 'config.select.code_hint'.tr(),
            ),
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: _onValidate,
            child: Text('config.select.validate'.tr()),
          ),
          const SizedBox(height: 16),
          Expanded(
            child: ValueListenableBuilder<bool>(
              valueListenable: _isLoadingNotifier,
              builder: (context, isLoading, _) {
                if (isLoading) {
                  return _buildShimmerLoading(); //TODO
                }
                return ValueListenableBuilder<List<GameConfigTemplateSimple>>(
                  valueListenable: _templatesNotifier,
                  builder: (context, templates, _) {
                    return _buildTemplatesList(templates);
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildShimmerLoading() {
    return Shimmer.fromColors(
      baseColor: Colors.grey[300]!,
      highlightColor: Colors.grey[100]!,
      child: ListView.builder(
        itemCount: 3,
        itemBuilder: (context, index) => Padding(
          padding: const EdgeInsets.only(bottom: 12.0),
          child: Container(
            height: 80,
            decoration: BoxDecoration(
              color: Colors.grey[300],
              borderRadius: BorderRadius.circular(8),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTemplatesList(List<GameConfigTemplateSimple> templates) {
    return ListView.builder(
      itemCount: templates.length,
      itemBuilder: (context, index) => _buildTemplateItem(templates[index]),
    );
  }

  Widget _buildTemplateItem(GameConfigTemplateSimple template) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Container(
        decoration: BoxDecoration(
          border: Border.all(color: Colors.grey[300]!),
          borderRadius: BorderRadius.circular(8),
        ),
        child: GestureDetector(
          onTap: () async => _showTemplateDetailsDialog(template),
          child: ListTile(
            title: Text(template.label),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 4),
                Text(
                  template.departureAddress,
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
                      starIndex < template.level ? Icons.star : Icons.star_outline,
                      size: 16,
                      color: Colors.amber,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
