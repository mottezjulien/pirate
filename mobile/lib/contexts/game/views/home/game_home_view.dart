import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../generic/config/router.dart';
import '../../../../style/style.dart';

class GameHomeView extends StatelessWidget {

  const GameHomeView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Aventure Pirate'),
        backgroundColor: Style.color.background,
      ),
      body: Stack(
        children: [
          // Image de fond
          Container(
            decoration: BoxDecoration(
              image: DecorationImage(
                image: AssetImage('assets/pouet/background.jpg'), // Exemple d'image
                fit: BoxFit.cover,
              ),
            ),
          ),
          // Boutons centraux
          Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  icon: Icon(Icons.map),
                  iconSize: 80,
                  color: Colors.white,
                  onPressed: () {
                    context.pushNamed(AppRouter.gameMapName);
                  },
                ),
                Text('Carte :)', style: TextStyle(color: Colors.white)),
                SizedBox(height: 20),
                IconButton(
                  icon: Icon(Icons.flag),
                  iconSize: 80,
                  color: Colors.white,
                  onPressed: () {
                    context.pushNamed(AppRouter.gameGoalName);
                  },
                ),
                Text('Objectifs :)', style: TextStyle(color: Colors.white)),
              ],
            ),
          ),
          // Perroquet en bas à droite
          Positioned(
            right: 20,
            bottom: 20,
            child: GestureDetector(
              onTap: () {
                // Afficher l'aide
              },
              child: Image.asset('assets/pouet/parrot.jpg', // Exemple d'image
                width: 100,
                height: 100,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
