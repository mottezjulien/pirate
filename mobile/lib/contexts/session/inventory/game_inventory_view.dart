
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

import 'game_inventory.dart';
import 'game_inventory_repository.dart';

class GameInventoryTabView extends StatefulWidget {
  const GameInventoryTabView({super.key});

  @override
  State<GameInventoryTabView> createState() => _GameInventoryTabViewState();
}

class _GameInventoryTabViewState extends State<GameInventoryTabView> {

  final GameInventoryRepository _repository = GameInventoryRepository();
  final ValueNotifier<List<GameInventorySimple>?> _itemsNotifier = ValueNotifier(null);
  final ValueNotifier<bool> _loadingAction = ValueNotifier(false);

  @override
  void initState() {
    super.initState();
    _loadItems();
  }

  Future<void> _loadItems() async {
    try {
      final items = await _repository.list();
      _itemsNotifier.value = items;
    } catch (e) {
      _itemsNotifier.value = [];
    }
  }

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<List<GameInventorySimple>?>(
      valueListenable: _itemsNotifier,
      builder: (context, items, _) {
        if (items == null) {
          return const Center(child: CircularProgressIndicator());
        }
        if (items.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.inventory_2_outlined, size: 64, color: Colors.grey),
                const SizedBox(height: 16),
                Text('inventory.empty'.tr(), style: const TextStyle(fontSize: 18, color: Colors.grey)),
              ],
            ),
          );
        }
        return _buildGrid(context, items);
      },
    );
  }

  Widget _buildGrid(BuildContext context, List<GameInventorySimple> items) {
    return GridView.builder(
      padding: const EdgeInsets.all(12),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: 8,
        mainAxisSpacing: 8,
        childAspectRatio: 0.85,
      ),
      itemCount: items.length,
      itemBuilder: (context, index) => _buildItemCard(context, items[index]),
    );
  }

  Widget _buildItemCard(BuildContext context, GameInventorySimple item) {
    return GestureDetector(
      onTap: () => _showItemDetail(context, item),
      child: Card(
        elevation: 2,
        child: Padding(
          padding: const EdgeInsets.all(8),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Expanded(
                child: item.buildImage(fit: BoxFit.contain),
              ),
              const SizedBox(height: 4),
              Text(
                item.label,
                style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
                textAlign: TextAlign.center,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              if (item.count > 1)
                Container(
                  margin: const EdgeInsets.only(top: 4),
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                  decoration: BoxDecoration(
                    color: Theme.of(context).primaryColor.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    'x${item.count}',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).primaryColor,
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  void _showItemDetail(BuildContext context, GameInventorySimple item) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => _ItemDetailSheet(
        item: item,
        repository: _repository,
        loadingNotifier: _loadingAction,
        onActionCompleted: () {
          Navigator.pop(context);
          _loadItems();
        },
      ),
    );
  }
}

class _ItemDetailSheet extends StatelessWidget {
  final GameInventorySimple item;
  final GameInventoryRepository repository;
  final ValueNotifier<bool> loadingNotifier;
  final VoidCallback onActionCompleted;

  const _ItemDetailSheet({
    required this.item,
    required this.repository,
    required this.loadingNotifier,
    required this.onActionCompleted,
  });

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<bool>(
      valueListenable: loadingNotifier,
      builder: (context, isLoading, _) {
        return Container(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Handle bar
              Container(
                width: 40,
                height: 4,
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                  color: Colors.grey[300],
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              // Image
              SizedBox(
                height: 120,
                child: item.buildImage(fit: BoxFit.contain),
              ),
              const SizedBox(height: 12),
              // Label
              Text(
                item.label,
                style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
              if (item.count > 1)
                Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    '${'inventory.quantity'.tr()}: ${item.count}',
                    style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                  ),
                ),
              const SizedBox(height: 20),
              // Action buttons
              if (isLoading)
                const CircularProgressIndicator()
              else
                _buildActionButtons(context),
              const SizedBox(height: 16),
            ],
          ),
        );
      },
    );
  }

  Widget _buildActionButtons(BuildContext context) {
    final List<Widget> buttons = [];

    if (item.hasAction(GameInventoryAction.USE)) {
      buttons.add(_buildActionButton(
        context: context,
        icon: Icons.touch_app,
        label: 'inventory.action.use'.tr(),
        color: Colors.blue,
        onPressed: () => _performAction(context, () => repository.use(item.id)),
      ));
    }

    if (item.hasAction(GameInventoryAction.CONSUME)) {
      buttons.add(_buildActionButton(
        context: context,
        icon: Icons.restaurant,
        label: 'inventory.action.consume'.tr(),
        color: Colors.green,
        onPressed: () => _performAction(context, () => repository.consume(item.id)),
      ));
    }

    if (item.hasAction(GameInventoryAction.EQUIP)) {
      buttons.add(_buildActionButton(
        context: context,
        icon: Icons.shield,
        label: 'inventory.action.equip'.tr(),
        color: Colors.orange,
        onPressed: () => _performAction(context, () => repository.equip(item.id)),
      ));
    }

    if (item.hasAction(GameInventoryAction.UNEQUIP)) {
      buttons.add(_buildActionButton(
        context: context,
        icon: Icons.shield_outlined,
        label: 'inventory.action.unequip'.tr(),
        color: Colors.orange,
        onPressed: () => _performAction(context, () => repository.unequip(item.id)),
      ));
    }

    if (item.hasAction(GameInventoryAction.USE_EQUIP)) {
      buttons.add(_buildActionButton(
        context: context,
        icon: Icons.flash_on,
        label: 'inventory.action.use_equip'.tr(),
        color: Colors.purple,
        onPressed: () => _performAction(context, () => repository.useEquip(item.id)),
      ));
    }

    if (item.hasAction(GameInventoryAction.DROP)) {
      buttons.add(_buildActionButton(
        context: context,
        icon: Icons.delete_outline,
        label: 'inventory.action.drop'.tr(),
        color: Colors.red,
        onPressed: () => _confirmDrop(context),
      ));
    }

    if (buttons.isEmpty) {
      return Text(
        'inventory.no_actions'.tr(),
        style: TextStyle(color: Colors.grey[500]),
      );
    }

    return Wrap(
      spacing: 8,
      runSpacing: 8,
      alignment: WrapAlignment.center,
      children: buttons,
    );
  }

  Widget _buildActionButton({
    required BuildContext context,
    required IconData icon,
    required String label,
    required Color color,
    required VoidCallback onPressed,
  }) {
    return ElevatedButton.icon(
      onPressed: onPressed,
      icon: Icon(icon, size: 18),
      label: Text(label),
      style: ElevatedButton.styleFrom(
        foregroundColor: Colors.white,
        backgroundColor: color,
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      ),
    );
  }

  void _confirmDrop(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('inventory.drop_confirm_title'.tr()),
        content: Text('inventory.drop_confirm_message'.tr(args: [item.label])),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text('default.no'.tr()),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(ctx);
              _performAction(context, () => repository.drop(item.id));
            },
            child: Text('default.yes'.tr()),
          ),
        ],
      ),
    );
  }

  Future<void> _performAction(BuildContext context, Future<void> Function() action) async {
    loadingNotifier.value = true;
    try {
      await action();
      onActionCompleted();
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('inventory.action_error'.tr())),
        );
      }
    } finally {
      loadingNotifier.value = false;
    }
  }
}
