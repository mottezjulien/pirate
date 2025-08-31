/*import 'package:flutter/material.dart';
import 'package:mobile/contexts/game/data/game_repository.dart';

import '../../../../style/style.dart';
import '../../domain/model/game_session.dart';
import '../../game_current.dart';

class OldGameMapView extends StatefulWidget {

  const OldGameMapView({super.key});

  @override
  State<OldGameMapView> createState() => _OldGameMapViewState();

}

class _OldGameMapViewState extends State<OldGameMapView> implements OnMoveListener {
  static const double max_height = 0.7;
  final GameMapViewModel _viewModel = GameMapViewModel();

  @override
  initState() {
    super.initState();
    GameSessionCurrent.addOnMoveListener(this);
  }

  @override
  dispose() {
    super.dispose();
    GameSessionCurrent.removeOnMoveListener(this);
  }

  @override
  void onMove() {
    _viewModel.refresh();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Carte :)')),
      body: FutureBuilder<List<GameMap>>(
        future: _viewModel.maps,
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            return _buildMap(context, snapshot.data!);
          } else if (snapshot.hasError) {
            return Center(child: Text('Error : ${snapshot.error}'));
          } else {
            return const Center(child: CircularProgressIndicator());
          }
        },
      ),
    );
  }

  Widget _buildMap(BuildContext context, List<GameMap> maps) {
    return Column(
      children: [
        SizedBox(
          height: MediaQuery.of(context).size.height * max_height,
          child: PageView.builder(
            controller: PageController(viewportFraction: 1.0),
            itemCount: maps.length,
            physics: const BouncingScrollPhysics(),
            onPageChanged: (index) =>
                _viewModel.currentIndex(index), //_currentIndex.value = index,
            itemBuilder: (BuildContext context, int index) {
              return slide(context, map: maps[index]);
            },
          ),
        ),
        const SizedBox(height: 16),
        ValueListenableBuilder<int>(
          valueListenable: _viewModel.currentIndexValueListenable(),
          builder: (BuildContext context, int currentIndex, Widget? _) {
            return CarouselDots(
              totalItems: maps.length,
              currentIndex: currentIndex,
            );
          },
        ),
      ],
    );
  }

  Widget slide(BuildContext context, {required GameMap map}) {
    return Container(
        margin: const EdgeInsets.symmetric(horizontal: 8),
        child: Stack(children: [
          Positioned.fill(
              child: Stack(children: [
            if (map.definition.type == 'ASSET')
              Positioned.fill(
                  child:
                      Image.asset(map.definition.value, fit: BoxFit.contain)),
            if (map.definition.type == 'WEB')
              Positioned.fill(
                  child:
                      Image.network(map.definition.value, fit: BoxFit.contain)),
            ValueListenableBuilder(
                valueListenable: _viewModel.positionValueListener(map),
                builder: (BuildContext context,
                    GameMapPositionPourcent? position, Widget? _) {
                  if (position != null) {
                    return Positioned(
                      left: (MediaQuery.of(context).size.width * position.x) - 24,
                      top: ((MediaQuery.of(context).size.height * max_height) * position.y) - 24,
                      child: Image.asset('assets/generic/map/pointeur.png', width: 48, height: 48),
                    );
                  }
                  return const SizedBox.shrink();
                })
          ]))
        ]));
  }
}

class GameMapViewModel {
  final GameSessionRepository _repository = GameSessionRepository();
  final ValueNotifier<int> _currentIndex = ValueNotifier<int>(0);
  final Map<String, ValueNotifier<GameMapPositionPourcent?>> positions = {};

  Future<List<GameMap>> get maps => _repository.findMaps();

  ValueNotifier<int> currentIndexValueListenable() {
    return _currentIndex;
  }

  void currentIndex(int index) {
    _currentIndex.value = index;
  }

  ValueNotifier<GameMapPositionPourcent?> positionValueListener(GameMap map) {
    ValueNotifier<GameMapPositionPourcent?>? position = positions[map.id];
    if (position != null) {
      return position;
    }
    position = ValueNotifier<GameMapPositionPourcent?>(map.position);
    positions[map.id] = position;
    return position;
  }

  void refresh() {
    _repository.findMaps().then((maps) {
      for (var map in maps) {
        ValueNotifier<GameMapPositionPourcent?>? valueNotifier =
            positions[map.id];
        if (valueNotifier != null) {
          valueNotifier.value = map.position;
        }
      }
    });
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
          duration: Style.duration.default_,
          height: 8,
          margin: EdgeInsets.symmetric(horizontal: Style.dimension.small),
          width: currentIndex == index ? 16 : 8,
          decoration: BoxDecoration(
            color: currentIndex == index
                ? Style.color.oneFirstPurple
                : Style.color.lightGrey,
            borderRadius: BorderRadius.circular(Style.dimension.extraLarge),
          ),
        ),
      ),
    );
  }
}

class GameMap {
  final String id;
  final String label;
  final GameMapDefinition definition;
  final int priority;
  final GameMapPositionPourcent? position;

  GameMap(
      {required this.id,
      required this.label,
      required this.priority,
      required this.definition,
      required this.position});
}

class GameMapDefinition {
  final String type;
  final String value;
  GameMapDefinition({required this.type, required this.value});
}

class GameMapPositionPourcent {
  final double x;
  final double y;
  GameMapPositionPourcent({required this.x, required this.y});
}
*/