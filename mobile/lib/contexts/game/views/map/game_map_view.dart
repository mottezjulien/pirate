

import 'package:flutter/material.dart';
import 'package:mobile/contexts/game/data/game_repository.dart';
import 'package:mobile/contexts/geo/domain/model/coordinate.dart';

import '../../game_current.dart';

class GameMapView extends StatelessWidget {

  final GameSessionRepository _repository = GameSessionRepository();

  final ValueNotifier<int> _currentIndex = ValueNotifier<int>(0);



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


  Widget _buildMap(BuildContext context, List<GameMap> maps) {

    Stream<Coordinate> _streamCoordinate = GameSessionCurrent.streamCoordinate;

    // Contrôleur pour suivre la page actuelle
    PageController _pageController = PageController();
    ValueNotifier<String> currentLabelNotifier = ValueNotifier<String>(maps[0].label);

    return Column(
      children: [
        // Texte dynamique : affichage du label de la carte en cours
        ValueListenableBuilder<String>(
          valueListenable: currentLabelNotifier,
          builder: (context, label, child) {
            return Padding(
              padding: const EdgeInsets.all(16.0),
              child: Text(
                label,
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
            );
          },
        ),
        // Slider des cartes
        Expanded(
          child: PageView.builder(
            controller: _pageController,

            itemCount: maps.length,
            physics: const BouncingScrollPhysics(),
            onPageChanged: (index) {
              currentLabelNotifier.value = maps[index].label;
              _currentIndex.value = index;
            },
            itemBuilder: (context, index) {
              final map = maps[index];
              return Stack(
                children: [
                  if(map.definition.startsWith("url:"))
                    Positioned.fill(child: Image.network(map.definition.substring("url:".length), fit: BoxFit.contain)),
                  if(map.definition.startsWith("asset:"))
                    Positioned.fill(child: Image.asset(map.definition.substring("asset:".length), fit: BoxFit.contain)),
                  // Pointeur : Superposé sur la carte
                  StreamBuilder(stream: _streamCoordinate, builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      final userCoordinate = snapshot.data!;
                      return Positioned(left: _calculatePointerX(
                          userCoordinate, map.bottomLeft, map.topRight,
                          context),
                        top: _calculatePointerY(
                            userCoordinate, map.bottomLeft, map.topRight,
                            context),
                        child: Image.asset('assets/generic/map/pointeur.png',
                            width: 24, height: 24),
                      );
                    }
                    return SizedBox.shrink();
                  })
                ],
              );
            },
          ),
        ),
        const SizedBox(height: 16),
        ValueListenableBuilder<int>(
          valueListenable: _currentIndex,
          builder: (context, currentIndex, _) {
            return CarouselDots(
              totalItems: widget.items.length,
              currentIndex: currentIndex,
            );
          },
        ),
      ],
    );
  }

  double _calculatePointerX(Coordinate userPosition, Coordinate bottomLeft, Coordinate topRight, BuildContext context) {
    // Calculer l'offset horizontal du pointeur en fonction des coordonnées
    double mapWidth = topRight.lng - bottomLeft.lng;
    double relativeLng = userPosition.lng - bottomLeft.lng;

    // Normaliser en fonction de la largeur de la carte (0 < x < 1)
    double normalizedX = relativeLng / mapWidth;

    // Calculer la position réelle en pixels
    double screenWidth = MediaQuery.of(context).size.width;
    return screenWidth * normalizedX;
  }

  double _calculatePointerY(
      Coordinate userPosition, Coordinate bottomLeft, Coordinate topRight, BuildContext context) {
    // Calculer l'offset vertical du pointeur en fonction des coordonnées
    double mapHeight = topRight.lat - bottomLeft.lat;
    double relativeLat = topRight.lat - userPosition.lat;

    // Normaliser en fonction de la hauteur de la carte (0 < y < 1)
    double normalizedY = relativeLat / mapHeight;

    // Calculer la position réelle en pixels (hauteur de l'écran)
    double screenHeight = MediaQuery.of(context).size.width; // Basé sur la largeur
    return screenHeight * normalizedY;
  }

}

class GameMap {

  final String id;
  final String definition;
  final String label;
  final Coordinate bottomLeft;
  final Coordinate topRight;

  GameMap({required this.id,
    required this.definition, required this.label
    , required this.bottomLeft, required this.topRight});

}

