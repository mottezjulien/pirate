import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart' as latlong2;
import '../../../presentation/game_presentation.dart';
import '../../../presentation/game_presentation_repository.dart';
import '../../../presentation/widgets/game_presentation_card_widget.dart';

class GameConfigSelectMapTabWidget extends StatefulWidget {
  const GameConfigSelectMapTabWidget({super.key});

  @override
  State<GameConfigSelectMapTabWidget> createState() => _GameConfigSelectMapTabWidgetState();
}

class _GameConfigSelectMapTabWidgetState extends State<GameConfigSelectMapTabWidget> {
  final MapController _mapController = MapController();
  final GamePresentationRepository _repository = GamePresentationRepository();
  List<GamePresentationSimple> _presentations = [];
  bool _isLoading = false;

  Future<void> _fetchPresentations() async {
    if (_isLoading) return;
    
    setState(() {
      _isLoading = true;
    });

    try {
      final bounds = _mapController.camera.visibleBounds;
      final results = await _repository.searchByLocation(
        bounds.southWest.latitude,
        bounds.southWest.longitude,
        bounds.northEast.latitude,
        bounds.northEast.longitude,
      );

      setState(() {
        _presentations = results;
      });
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Erreur lors de la récupération des présentations')),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        FlutterMap(
          mapController: _mapController,
          options: MapOptions(
            initialCenter: const latlong2.LatLng(46.2276, 2.2137),
            initialZoom: 6,
            onMapReady: () {
              _fetchPresentations();
            },
          ),
          children: [
            TileLayer(
              urlTemplate: 'https://maps.geoapify.com/v1/tile/carto/{z}/{x}/{y}.png?&apiKey=22c7977e6042490dbc09eb5d6537fa30',
              userAgentPackageName: 'mobile',
            ),
            MarkerLayer(
              markers: _presentations.map((presentation) {
                return Marker(
                  point: latlong2.LatLng(
                    presentation.departurePoint.lat,
                    presentation.departurePoint.lng,
                  ),
                  width: 40,
                  height: 40,
                  child: GestureDetector(
                    onTap: () {
                      showModalBottomSheet(
                        context: context,
                        backgroundColor: Colors.transparent,
                        builder: (context) => Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: GamePresentationCardWidget(presentation: presentation),
                        ),
                      );
                    },
                    child: Container(
                      decoration: const BoxDecoration(shape: BoxShape.circle, color: Colors.red),
                      child: const Icon(Icons.location_on, color: Colors.white, size: 24),
                    ),
                  ),
                );
              }).toList(),
            ),
          ],
        ),
        Positioned(
          top: 16,
          right: 16,
          child: FloatingActionButton(
            mini: true,
            onPressed: _isLoading ? null : _fetchPresentations,
            child: _isLoading
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                  )
                : const Icon(Icons.refresh),
          ),
        ),
      ],
    );
  }
}
