import 'package:flutter/material.dart';

import '../../data/game_image_repository.dart';
import '../../data/game_map_repository.dart';
import '../../domain/model/game_session.dart';
import '../../game_current.dart';

class GameMapView extends StatefulWidget {
  const GameMapView({super.key});

  @override
  State<GameMapView> createState() => _GameMapViewState();
}

class _GameMapViewState extends State<GameMapView> implements OnMoveListener {
  final PageController _pageController = PageController();
  final ValueNotifier<int> _currentIndex = ValueNotifier<int>(0);
  final ValueNotifier<List<GameMap>> _valueNotifierMaps =
      ValueNotifier<List<GameMap>>([]);
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
    return Scaffold(
        appBar: appBar(),
        body: SafeArea(
            child: ValueListenableBuilder<List<GameMap>>(
                valueListenable: _valueNotifierMaps,
                builder: (context, currentIndex, _) =>
                    _buildWithMaps(context, currentIndex))));
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

  AppBar appBar() {
    return AppBar(
        title: Image.asset('assets/pouet/icon.png'),
        centerTitle: true,
        actions: [
          IconButton(
              iconSize: 48, //TODO
              icon: Icon(Icons.support), tooltip: 'Help :)', onPressed: () {})
        ]);
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
    return Stack(
      fit: StackFit.passthrough,
      children: [
        Positioned.fill(
            child: Image.asset(map.imageValue, fit: BoxFit.contain)),
        if (map.pointer != null)
          ImageObjectWidget(
              object: map.pointer!.toImageObject(),
              constraints: constraints),
        for (final imageObject in map.imageObjects)
          ImageObjectWidget(imageId: map.image.id, object: imageObject, constraints: constraints)
      ],
    );
  }

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

class ImageObjectWidget extends StatelessWidget {

  final String? imageId;
  final ImageObject object;
  final BoxConstraints constraints;

  const ImageObjectWidget({super.key, this.imageId,
    required this.object,
    required this.constraints});

  @override
  Widget build(BuildContext context) {
    void onAction() {
      if(imageId != null) {
        final GameImageRepository imageRepository = new GameImageRepository();
        imageRepository.clickObject(object.id, object.id);
      }
    }
    if (object.type == 'POINT') {
      return Positioned(
        top: object.position.top * constraints.maxHeight,
        left: object.position.left * constraints.maxWidth,
        child: GestureDetector(
          onDoubleTap: onAction,
          child: Container(
            width: 12,
            height: 12,
            decoration: BoxDecoration(
              color: object.point!.color,
              shape: BoxShape.circle,
            ),
          ),
        ),
      );
    }
    if (object.type == 'IMAGE') {
      return Positioned(
        top: object.position.top * constraints.maxHeight,
        left: object.position.left * constraints.maxWidth,
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
    return SizedBox.shrink();
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
