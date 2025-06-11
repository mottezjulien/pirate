

import 'package:flutter/material.dart';
import 'package:mobile/contexts/game/data/game_repository.dart';
import 'package:mobile/contexts/geo/domain/model/coordinate.dart';

import '../../../../style/style.dart';
import '../../game_current.dart';

class GameMapView extends StatelessWidget {

  final GameSessionRepository _repository = GameSessionRepository();

  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Carte :)'),
      ),
      body: FutureBuilder<List<GameMap>>(
        future: _repository.findMaps(),
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

  var max_height = 0.7;


  Widget _buildMap(BuildContext context, List<GameMap> maps) {

    final PageController _controller = PageController(viewportFraction: 1.0);
    final ValueNotifier<int> _currentIndex = ValueNotifier<int>(0);

    return Column(
      children: [
        SizedBox(
          height: MediaQuery.of(context).size.height * max_height,
          child: PageView.builder(
            controller: _controller,
            itemCount: maps.length,
            physics: const BouncingScrollPhysics(),
            onPageChanged: (index) => _currentIndex.value = index,
            itemBuilder: (BuildContext context, int index) {
              return slide(context, map: maps[index]);
            },
          ),
        ),
        const SizedBox(height: 16),
        ValueListenableBuilder<int>(
          valueListenable: _currentIndex,
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
      child: Stack(
        children: [
          Positioned.fill(
            child: Stack(
              children: [
                if(map.definition.type == 'ASSET')
                  Positioned.fill(child: Image.asset(map.definition.value, fit: BoxFit.contain)),
                if(map.definition.type == 'WEB')
                  Positioned.fill(child: Image.network(map.definition.value, fit: BoxFit.contain)),
                if(map.position != null)
                  Positioned(
                    left: (MediaQuery.of(context).size.width * map.position!.x) - 24,
                    top: ((MediaQuery.of(context).size.height * max_height) * map.position!.y) - 24,
                    child: Image.asset('assets/generic/map/pointeur.png', width: 48, height: 48),
                  )
              ],
            ),
          ),
          /*Positioned(
            top: 12,
            left: 12,
            child: Container(
              decoration: BoxDecoration(
                color: Colors.black.withValues(alpha: 0.3),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.info_outline,
                color: Colors.white,
                size: 32,
              ),
            ),
          ),*/
        ],
      ),
    );
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
            borderRadius:
            BorderRadius.circular(Style.dimension.extraLarge),
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

  GameMap({required this.id, required this.label, required this.priority,
    required this.definition, required this.position});

}

class GameMapDefinition {
  final String type;
  final String value;
  GameMapDefinition({required this.type, required this.value });
}

class GameMapPositionPourcent {
  final double x;
  final double y;
  GameMapPositionPourcent({required this.x, required this.y});
}
