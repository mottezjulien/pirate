import 'package:flutter/material.dart';
import '../../contexts/game/domain/model/map_models.dart';

/// Widget partagé pour afficher une carte avec ses positions
/// Utilisé à la fois dans l'éditeur et dans le viewer pour garantir
/// un comportement identique
class MapViewerWidget extends StatelessWidget {
  final GameMapData map;
  final bool showZones;
  final bool showPoints;
  final Function(GameMapPosition)? onPositionTap;
  final double? width;
  final double? height;
  final bool isEditor;

  const MapViewerWidget({
    super.key,
    required this.map,
    this.showZones = true,
    this.showPoints = true,
    this.onPositionTap,
    this.width,
    this.height,
    this.isEditor = false,
  });

  @override
  Widget build(BuildContext context) {
    // Utiliser les dimensions fournies ou calculer automatiquement
    final containerWidth = width ?? 430.0;
    final containerHeight = height ?? 238.8;

    return SizedBox(
      width: containerWidth,
      height: containerHeight,
      child: Stack(
        children: [
          // Image de fond
          Positioned.fill(
            child: map.image.type == 'ASSET'
                ? Image.asset(map.image.path, fit: BoxFit.contain)
                : Image.network(map.image.path, fit: BoxFit.contain),
          ),
          // Positions (zones et points)
          ...map.positions.map((position) => _buildPositionWidget(
                position,
                containerWidth,
                containerHeight,
              )),
        ],
      ),
    );
  }

  Widget _buildPositionWidget(
    GameMapPosition position,
    double containerWidth,
    double containerHeight,
  ) {
    // FORCER exactement les mêmes dimensions que l'éditeur pour éviter les différences d'échelle
    // Ces valeurs sont calculées dans l'éditeur et doivent être identiques
    const double imageWidth = 318.5;  // EXACTEMENT comme dans l'éditeur
    const double imageHeight = 238.8; // EXACTEMENT comme dans l'éditeur
    const double offsetX = 55.8;      // EXACTEMENT comme dans l'éditeur
    const double offsetY = 0.0;       // EXACTEMENT comme dans l'éditeur

    if (position.isZone && showZones) {
      return _buildZoneWidget(position, offsetX, offsetY, imageWidth, imageHeight);
    } else if (position.isPoint && showPoints) {
      return _buildPointWidget(position, offsetX, offsetY, imageWidth, imageHeight);
    }

    return const SizedBox.shrink();
  }

  Widget _buildZoneWidget(
    GameMapPosition position,
    double offsetX,
    double offsetY,
    double imageWidth,
    double imageHeight,
  ) {
    final bounds = position.bounds!;
    final scaleX = imageWidth / map.image.size.width;
    final scaleY = imageHeight / map.image.size.height;

    final scaledX = (bounds.left * map.image.size.width * scaleX);
    final scaledY = (bounds.top * map.image.size.height * scaleY);
    final scaledWidth = bounds.width * map.image.size.width * scaleX;
    final scaledHeight = bounds.height * map.image.size.height * scaleY;

    return Positioned(
      left: offsetX + scaledX,
      top: offsetY + scaledY,
      width: scaledWidth,
      height: scaledHeight,
      child: GestureDetector(
        onTap: onPositionTap != null ? () => onPositionTap!(position) : null,
        child: Container(
          decoration: BoxDecoration(
            color: Colors.blue.withValues(alpha: 0.3),
            border: Border.all(color: Colors.blue, width: 2),
          ),
          child: Center(
            child: Text(
              position.label,
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 12,
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPointWidget(
    GameMapPosition position,
    double offsetX,
    double offsetY,
    double imageWidth,
    double imageHeight,
  ) {
    final pos = position.position!;

    // Convertir les coordonnées relatives vers l'image originale
    final originalCenterX = pos.dx * map.image.size.width;
    final originalCenterY = pos.dy * map.image.size.height;

    // Scale vers l'image affichée
    final scaleX = imageWidth / map.image.size.width;
    final scaleY = imageHeight / map.image.size.height;

    final displayCenterX = originalCenterX * scaleX;
    final displayCenterY = originalCenterY * scaleY;

    // CORRECTION: Position finale SANS offset (comme dans l'éditeur)
    // L'éditeur sauvegarde les positions relatives à l'image, pas au container
    final scaledX = displayCenterX;
    final scaledY = displayCenterY;

    print('🎯 WIDGET PARTAGÉ ${position.label}:');
    print('  Coordonnées relatives: (${pos.dx.toStringAsFixed(3)}, ${pos.dy.toStringAsFixed(3)})');
    print('  Centre original: (${originalCenterX.toStringAsFixed(1)}, ${originalCenterY.toStringAsFixed(1)})');
    print('  Image size: ${imageWidth.toStringAsFixed(1)} x ${imageHeight.toStringAsFixed(1)}');
    print('  Offset: (${offsetX.toStringAsFixed(1)}, ${offsetY.toStringAsFixed(1)})');
    print('  Position dans image: (${displayCenterX.toStringAsFixed(1)}, ${displayCenterY.toStringAsFixed(1)})');
    print('  Position finale: (${scaledX.toStringAsFixed(1)}, ${scaledY.toStringAsFixed(1)})');
    print('  Position picto RÉELLE: (${(offsetX + scaledX - 12).toStringAsFixed(1)}, ${(offsetY + scaledY - 12).toStringAsFixed(1)})');

    return Positioned(
      left: offsetX + scaledX - 12,
      top: offsetY + scaledY - 12,
      child: GestureDetector(
        onTap: onPositionTap != null ? () => onPositionTap!(position) : null,
        child: Container(
          width: 24,
          height: 24,
          decoration: BoxDecoration(
            color: Colors.orange,
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 2),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.3),
                blurRadius: 4,
                offset: const Offset(0, 2),
              ),
            ],
          ),
        ),
      ),
    );
  }
}