import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart' as latlong2;

class GameConfigSelectMapTabWidget extends StatelessWidget {
  const GameConfigSelectMapTabWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return FlutterMap(
      options: MapOptions(
        initialCenter: const latlong2.LatLng(46.2276, 2.2137),
        initialZoom: 6,
      ),
      children: [
        TileLayer(
          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          userAgentPackageName: 'mobile',
        ),
        MarkerLayer(
          markers: [
            Marker(
              point: const latlong2.LatLng(48.8566, 2.3522),
              width: 40,
              height: 40,
              child: Container(
                decoration: const BoxDecoration(shape: BoxShape.circle, color: Colors.red),
                child: const Icon(Icons.location_on, color: Colors.white, size: 24),
              ),
            ),
          ],
        ),
      ],
    );
  }
}
