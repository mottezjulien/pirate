import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';

import '../domain/model/game_session.dart';
import '../game_current.dart';
import '../image/game_image_repository.dart';
import 'game_map_repository.dart';

class GameMapTabView extends StatefulWidget {
  const GameMapTabView({super.key});

  @override
  State<GameMapTabView> createState() => _GameMapTabViewState();
}

class _GameMapTabViewState extends State<GameMapTabView> implements OnMoveListener {
  final PageController _pageController = PageController();
  final ValueNotifier<int> _currentIndex = ValueNotifier<int>(0);
  final ValueNotifier<List<GameMap>> _valueNotifierMaps = ValueNotifier<List<GameMap>>([]);
  final GameMapRepository _repository = GameMapRepository();

  @override
  void initState() {
    super.initState();
    GameCurrent.addOnMoveListener(this);
    findMaps();
  }

  @override
  void dispose() {
    _pageController.dispose();
    _currentIndex.dispose();
    GameCurrent.removeOnMoveListener(this);
    super.dispose();
  }

  @override
  void onMove() {
    findMaps();
  }

  void findMaps() {
    _repository.get().then((maps) => _valueNotifierMaps.value = maps);
  }

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<List<GameMap>>(
        valueListenable: _valueNotifierMaps,
        builder: (context, currentIndex, _) => _buildWithMaps(context, currentIndex));
  }

  Widget _buildWithMaps(BuildContext context, List<GameMap> maps) {
    return Column(
      children: [
        Expanded(
          child: PageView.builder(
            controller: _pageController,
            itemCount: maps.length,
            physics: const BouncingScrollPhysics(),
            onPageChanged: (index) => _currentIndex.value = index,
            itemBuilder: (context, index) => _buildOneMap(maps[index]),
          ),
        ),
        if (maps.length > 1)
          _GameMapWidgetFooter(
            totalItems: maps.length,
            currentIndex: _currentIndex,
          ),
      ],
    );
  }

  Widget _buildOneMap(GameMap map) {
    return GestureDetector(
      onDoubleTap: () => _showImageZoomDialog(map),
      child: Center(
        child: LayoutBuilder(
          builder: (context, constraints) {
            return _MapWidget(constraints: constraints, map: map);
          },
        ),
      ),
    );
  }



  void _showImageZoomDialog(GameMap map) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => ImageZoomDialog(
        map: map,
      ),
    );
  }
}

class _MapWidget extends StatelessWidget {

  final BoxConstraints constraints;
  final GameMap map;

  const _MapWidget({required this.constraints, required this.map});

  @override
  Widget build(BuildContext context) {
    return Stack(fit: StackFit.passthrough,
      children: [
        Positioned.fill(
            child: Image.asset(map.imageValue, fit: BoxFit.contain)),
        // Display player pointer if available
        if (map.pointer != null)
          _PlayerPointerWidget(
            bounds: map.bounds,
            constraints: constraints,
            pointerImage: map.pointer!,
          ),
        // Display map objects
        for (final mapObject in map.objects)
          _MapObjectWidget(
            mapId: map.id,
            object: mapObject,
            bounds: map.bounds,
            constraints: constraints,
          )
      ],
    );
  }
}

/// Widget that displays the player's current GPS position on the map
class _PlayerPointerWidget extends StatelessWidget {
  final MapBounds bounds;
  final BoxConstraints constraints;
  final MapImage pointerImage;

  const _PlayerPointerWidget({
    required this.bounds,
    required this.constraints,
    required this.pointerImage,
  });

  @override
  Widget build(BuildContext context) {
    // Get player's current position from GameCurrent
    final playerPosition = GameCurrent.currentPosition;
    if (playerPosition == null) {
      return const SizedBox.shrink();
    }

    // Convert GPS position to widget position
    final pos = _gpsToWidgetPosition(
      LatLng(playerPosition.lat, playerPosition.lng),
      bounds,
      constraints,
    );

    // Check if position is within bounds
    if (pos == null) {
      return const SizedBox.shrink();
    }

    return Positioned(
      top: pos.dy - 15, // Center the pointer (30/2)
      left: pos.dx - 15,
      child: Image.asset(
        pointerImage.value,
        width: 30,
        height: 30,
        fit: BoxFit.contain,
      ),
    );
  }
}

/// Widget that displays a single map object (point or image marker)
class _MapObjectWidget extends StatelessWidget {
  final String mapId;
  final MapObject object;
  final MapBounds bounds;
  final BoxConstraints constraints;

  const _MapObjectWidget({
    required this.mapId,
    required this.object,
    required this.bounds,
    required this.constraints,
  });

  @override
  Widget build(BuildContext context) {
    // Convert GPS position to widget position
    final pos = _gpsToWidgetPosition(object.position, bounds, constraints);

    // If position is outside bounds, don't display
    if (pos == null) {
      return const SizedBox.shrink();
    }

    void onAction() {
      final GameImageRepository imageRepository = GameImageRepository();
      imageRepository.clickObject(mapId, object.id);
    }

    if (object.isPoint) {
      return Positioned(
        top: pos.dy - 6, // Center the point (12/2)
        left: pos.dx - 6,
        child: GestureDetector(
          onDoubleTap: onAction,
          child: Container(
            width: 12,
            height: 12,
            decoration: BoxDecoration(
              color: _parseColor(object.color),
              shape: BoxShape.circle,
              border: Border.all(color: Colors.white, width: 2),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.3),
                  blurRadius: 4,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
          ),
        ),
      );
    }

    if (object.isImage && object.image != null) {
      return Positioned(
        top: pos.dy - 15, // Center the image (30/2)
        left: pos.dx - 15,
        child: GestureDetector(
          onDoubleTap: onAction,
          child: Image.asset(
            object.image!.value,
            width: 30,
            height: 30,
            fit: BoxFit.contain,
          ),
        ),
      );
    }

    return const SizedBox.shrink();
  }

  Color _parseColor(String? colorStr) {
    if (colorStr == null || colorStr.isEmpty) {
      return Colors.red;
    }
    switch (colorStr.toLowerCase()) {
      case 'red':
        return Colors.red;
      case 'blue':
        return Colors.blue;
      case 'green':
        return Colors.green;
      case 'yellow':
        return Colors.yellow;
      case 'orange':
        return Colors.orange;
      case 'purple':
        return Colors.purple;
      case 'white':
        return Colors.white;
      case 'black':
        return Colors.black;
      default:
        // Try to parse hex color
        if (colorStr.startsWith('#')) {
          try {
            return Color(int.parse(colorStr.substring(1), radix: 16) + 0xFF000000);
          } catch (_) {
            return Colors.red;
          }
        }
        return Colors.red;
    }
  }
}

/// Converts a GPS position to widget coordinates based on map bounds
/// Returns null if the position is outside the bounds
Offset? _gpsToWidgetPosition(
  LatLng position,
  MapBounds bounds,
  BoxConstraints constraints,
) {
  // Calculate relative position within bounds (0.0 to 1.0)
  final latRange = bounds.topRight.latitude - bounds.bottomLeft.latitude;
  final lngRange = bounds.topRight.longitude - bounds.bottomLeft.longitude;

  if (latRange == 0 || lngRange == 0) return null;

  final relativeX = (position.longitude - bounds.bottomLeft.longitude) / lngRange;
  // Note: Y is inverted because screen coordinates start from top
  final relativeY = 1.0 - (position.latitude - bounds.bottomLeft.latitude) / latRange;

  // Check if position is within bounds (with small margin for edge cases)
  if (relativeX < -0.05 || relativeX > 1.05 || relativeY < -0.05 || relativeY > 1.05) {
    return null;
  }

  // Convert to widget coordinates
  return Offset(
    relativeX.clamp(0.0, 1.0) * constraints.maxWidth,
    relativeY.clamp(0.0, 1.0) * constraints.maxHeight,
  );
}

class ImageZoomDialog extends StatefulWidget {
  final GameMap map;

  const ImageZoomDialog({
    super.key,
    required this.map,
  });

  @override
  State<ImageZoomDialog> createState() => _ImageZoomDialogState();
}

class _ImageZoomDialogState extends State<ImageZoomDialog> {
  final TransformationController _transformationController =
      TransformationController();
  double _currentScale = 1;

  @override
  void dispose() {
    _transformationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onScaleUpdate: (details) {
        _currentScale = details.scale;
        _transformationController.value = Matrix4.identity()
          ..scaleByDouble(_currentScale, _currentScale, _currentScale, 1.0);
      },
      onScaleEnd: (details) {
        if (_currentScale < 1.0) {
          _currentScale = 1.0;
          _transformationController.value = Matrix4.identity()..scaleByDouble(_currentScale, _currentScale, _currentScale, 1.0);
        }
      },
      child: Dialog(
        insetPadding: EdgeInsets.zero,
        backgroundColor: Colors.black,
        child: Stack(
          children: [
            InteractiveViewer(
              transformationController: _transformationController,
              boundaryMargin: const EdgeInsets.all(80),
              minScale: 1.0,
              maxScale: 4.0,
              child: Center(
                child: LayoutBuilder(
                  builder: (context, constraints) {
                    return _MapWidget(constraints: constraints, map: widget.map);
                  },
                ),
              ),
            ),
            _closeWidget(context)
          ],
        ),
      ),
    );
  }

  Positioned _closeWidget(BuildContext context) {
    return Positioned(
      top: 16,
      right: 16,
      child: GestureDetector(
        onTap: () => Navigator.of(context).pop(),
        child: Container(
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.3),
            shape: BoxShape.circle,
          ),
          padding: const EdgeInsets.all(8),
          child: const Icon(
            Icons.close,
            color: Colors.white,
          ),
        ),
      ),
    );
  }
}



class _GameMapWidgetFooter extends StatelessWidget {
  final int totalItems;
  final ValueNotifier<int> currentIndex;

  const _GameMapWidgetFooter({
    required this.totalItems,
    required this.currentIndex,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
      decoration: BoxDecoration(
        color: Colors.grey.shade100,
        border: Border(top: BorderSide(color: Colors.grey.shade300)),
      ),
      child: ValueListenableBuilder<int>(
        valueListenable: currentIndex,
        builder: (context, index, _) {
          return Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              _CarouselDots(
                totalItems: totalItems,
                currentIndex: index,
              ),
              const SizedBox(height: 8),
              Text(
                '${index + 1} / $totalItems',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _CarouselDots extends StatelessWidget {
  const _CarouselDots({
    required this.totalItems,
    required this.currentIndex,
  });

  final int totalItems;
  final int currentIndex;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(
        totalItems,
        (index) => AnimatedContainer(
          duration: GameCurrent.style.duration.default_,
          height: 8,
          margin: EdgeInsets.symmetric(
              horizontal: GameCurrent.style.dimension.small),
          width: currentIndex == index ? 16 : 8,
          decoration: BoxDecoration(
            color: currentIndex == index
                ? GameCurrent.style.color.primary
                : GameCurrent.style.color.lightGrey,
            borderRadius:
                BorderRadius.circular(GameCurrent.style.dimension.extraLarge),
          ),
        ),
      ),
    );
  }
}
