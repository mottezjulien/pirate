import 'package:flutter/material.dart';
import 'package:shimmer/shimmer.dart';
import '../../template/game_config_template.dart';
import '../../template/template_repository.dart';

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
            SnackBar(content: Text('Erreur: $error')),
          );
        }
      });
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Veuillez entrer un code')),
      );
    }
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
              labelText: 'Code',
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              hintText: 'Entrez le code du jeu',
            ),
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: _onValidate,
            child: const Text('Valider'),
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
          onTap: () async {
            _isLoadingNotifier.value = false;
            GameConfigTemplateDetails details = await _repository.findById(template.id);
          },
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
