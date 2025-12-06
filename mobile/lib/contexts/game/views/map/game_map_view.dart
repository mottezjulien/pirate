import 'package:flutter/material.dart';
import '../../data/game_map_repository.dart';
import '../../domain/model/game_session.dart';
import '../../game_current.dart';

import '../../../../generic/dialog.dart' as CustomDialog;

class GameMapView extends StatefulWidget {

  const GameMapView({super.key});

  @override
  State<GameMapView> createState() => _GameMapViewState();

}

class _GameMapViewState extends State<GameMapView> implements OnMoveListener {

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
    CustomDialog.Dialog dialog = CustomDialog.Dialog();
    dialog.showMessage(message: "onMove");
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
        //child: plop(),
        child: ValueListenableBuilder<List<GameMap>>(
            valueListenable: _valueNotifierMaps,
            builder: (context, currentIndex, _) => plop2(context, currentIndex))
      )
    );
  }

  Widget plop2(BuildContext context, List<GameMap> maps) {
    return Column(
      children: [
        Expanded(
          child: PageView.builder(
            controller: _pageController,
            itemCount: maps.length,
            physics: const BouncingScrollPhysics(),
            onPageChanged: (index) {
              _currentIndex.value = index;
            },
            itemBuilder: (context, index) {
              return _buildMapSlide(maps[index]);
            },
          ),
        ),
        Container(
          padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
          decoration: BoxDecoration(
            color: Colors.grey.shade100,
            border: Border(top: BorderSide(color: Colors.grey.shade300)),
          ),
          child: ValueListenableBuilder<int>(
            valueListenable: _currentIndex,
            builder: (context, currentIndex, _) {
              return Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  CarouselDots(
                    totalItems: maps.length,
                    currentIndex: currentIndex,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${currentIndex + 1} / ${maps.length}',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              );
            },
          ),
        ),
      ],
    );
  }

  FutureBuilder<List<GameMap>> plop() {
    return FutureBuilder<List<GameMap>>(
        future: _repository.get(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            final maps = snapshot.data!;
            return Column(
              children: [
                Expanded(
                  child: PageView.builder(
                    controller: _pageController,
                    itemCount: maps.length,
                    physics: const BouncingScrollPhysics(),
                    onPageChanged: (index) {
                      _currentIndex.value = index;
                    },
                    itemBuilder: (context, index) {
                      return _buildMapSlide(maps[index]);
                    },
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
                  decoration: BoxDecoration(
                    color: Colors.grey.shade100,
                    border: Border(top: BorderSide(color: Colors.grey.shade300)),
                  ),
                  child: ValueListenableBuilder<int>(
                    valueListenable: _currentIndex,
                    builder: (context, currentIndex, _) {
                      return Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          CarouselDots(
                            totalItems: maps.length,
                            currentIndex: currentIndex,
                          ),
                          const SizedBox(height: 8),
                          Text(
                            '${currentIndex + 1} / ${maps.length}',
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      );
                    },
                  ),
                ),
              ],
            );
          } else if (snapshot.hasError) {
            return Center(child: Text('Erreur : ${snapshot.error}'));
          } else {
            return const Center(child: CircularProgressIndicator());
          }
        },
      );
  }

  Widget _buildMapSlide(GameMap map) {
    return GestureDetector(
      onDoubleTap: () => _showImageZoomDialog(map),
      child: Center(
        child: LayoutBuilder(
          builder: (context, constraints) {
            return Stack(
              fit: StackFit.passthrough,
              children: [
                Image.asset(map.imageValue, fit: BoxFit.contain),
                if(map.pointer != null)
                  ImageObjectWidget(imageObject: map.pointer!.toImageObject(), constraints: constraints),
                for (final imageObject in map.imageObjects)
                  ImageObjectWidget(imageObject: imageObject, constraints: constraints),
              ],
            );
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
        IconButton(icon: Icon(Icons.support),
          tooltip: 'Help :)',
          onPressed: () { })
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
  final TransformationController _transformationController = TransformationController();

  @override
  void initState() {
    super.initState();
    Future.microtask(() {
      _transformationController.value = Matrix4.identity()..scale(1.05);
    });
  }

  @override
  void dispose() {
    _transformationController.dispose();
    super.dispose();
  }

  void _handleSwipe(DragEndDetails details) {
    if (details.velocity.pixelsPerSecond.dy > 300) {
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onVerticalDragEnd: _handleSwipe,
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
                    return Stack(
                      fit: StackFit.passthrough,
                      children: [
                        Image.asset(
                          widget.map.image.value,
                          fit: BoxFit.contain,
                        ),
                        if(widget.map.pointer != null)
                          ImageObjectWidget(imageObject: widget.map.pointer!.toImageObject(), constraints: constraints),
                        for (final imageObject in widget.map.imageObjects)
                          ImageObjectWidget(imageObject: imageObject, constraints: constraints)
                      ],
                    );
                  },
                ),
              ),
            ),
            Positioned(
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
            ),
          ],
        ),
      ),
    );
  }

}

class ImageObjectWidget extends StatelessWidget {

  final ImageObject imageObject;
  final BoxConstraints constraints;

  const ImageObjectWidget({super.key,
    required this.imageObject,
    required this.constraints});

  @override
  Widget build(BuildContext context) {
    if (imageObject.type == 'POINT') {
      return Positioned(
        top: imageObject.position.top * constraints.maxHeight,
        left: imageObject.position.left * constraints.maxWidth,
        child: Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            color: imageObject.point!.color,
            shape: BoxShape.circle,
          ),
        ),
      );
    }
    if (imageObject.type == 'IMAGE') {
      return Positioned(
        top: imageObject.position.top * constraints.maxHeight,
        left: imageObject.position.left * constraints.maxWidth,
        child: Image.asset(
          imageObject.image!.value,
          width: 30,
          height: 30,
          fit: BoxFit.contain,
        ),
      );
    }
    return SizedBox.shrink();
  }
}

class CarouselDots extends StatelessWidget {
  const CarouselDots({
    super.key,
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
          margin: EdgeInsets.symmetric(horizontal: GameCurrent.style.dimension.small),
          width: currentIndex == index ? 16 : 8,
          decoration: BoxDecoration(
            color: currentIndex == index
                ? GameCurrent.style.color.primary
                : GameCurrent.style.color.lightGrey,
            borderRadius: BorderRadius.circular(GameCurrent.style.dimension.extraLarge),
          ),
        ),
      ),
    );
  }
}

