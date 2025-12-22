import 'package:flutter/material.dart';

import '../models/config_models.dart';

class ConfigView extends StatefulWidget {
  const ConfigView({super.key});

  @override
  State<ConfigView> createState() => _ConfigViewState();
}

class _ConfigViewState extends State<ConfigView> {
  late ConfigImage configImage;

  @override
  void initState() {
    super.initState();
    configImage = ConfigImage(assetPath: 'assets/other_app_config_image.png');
  }

  void _showCoordinatesDialog(double top, double left) {
    showDialog(
      context: context,
      barrierDismissible: true,
      builder: (context) => _CoordinatesDialog(top: top, left: left),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Config Editor'),
        centerTitle: true,
      ),
      body: Center(
        child: LayoutBuilder(
          builder: (context, constraints) {
            return GestureDetector(
              onTapDown: (details) {
                final top = details.localPosition.dy / constraints.maxHeight;
                final left = details.localPosition.dx / constraints.maxWidth;
                _showCoordinatesDialog(
                  top.clamp(0.0, 1.0),
                  left.clamp(0.0, 1.0),
                );
              },
              child: Image.asset(
                configImage.assetPath,
                fit: BoxFit.contain,
              ),
            );
          },
        ),
      ),
    );
  }
}

class _CoordinatesDialog extends StatelessWidget {
  final double top;
  final double left;

  const _CoordinatesDialog({
    required this.top,
    required this.left,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => Navigator.of(context).pop(),
      child: Dialog(
        backgroundColor: Colors.white,
        child: GestureDetector(
          onTap: () {},
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  'Coordonnées',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 20),
                Text(
                  'top: ${(top * 100).toStringAsFixed(2)}%',
                  style: Theme.of(context).textTheme.bodyLarge,
                ),
                const SizedBox(height: 12),
                Text(
                  'left: ${(left * 100).toStringAsFixed(2)}%',
                  style: Theme.of(context).textTheme.bodyLarge,
                ),
                const SizedBox(height: 24),
                Text(
                  'Cliquez n\'importe où pour fermer',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Colors.grey,
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
