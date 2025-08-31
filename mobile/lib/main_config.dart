import 'package:flutter/material.dart';
import 'config/map/map_zone_editor.dart';

/// Application de configuration pour définir les zones sur les cartes
/// Lancez cette app pour créer/modifier les zones de vos images de cartes
void main() {
  runApp(const MapConfigApp());
}

class MapConfigApp extends StatelessWidget {
  const MapConfigApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Configuration des Cartes - Zones',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      home: const MapConfigHome(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class MapConfigHome extends StatefulWidget {
  const MapConfigHome({super.key});

  @override
  State<MapConfigHome> createState() => _MapConfigHomeState();
}

class _MapConfigHomeState extends State<MapConfigHome> {
  final TextEditingController _imagePathController = TextEditingController();
  final TextEditingController _mapIdController = TextEditingController();
  final TextEditingController _mapNameController = TextEditingController();
  bool _isUrlMode = false;

  @override
  void dispose() {
    _imagePathController.dispose();
    _mapIdController.dispose();
    _mapNameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('🗺️ Configuration des Cartes'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Colors.blue.shade50,
              Colors.white,
            ],
          ),
        ),
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // En-tête
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  children: [
                    Icon(
                      Icons.map_outlined,
                      size: 64,
                      color: Colors.blue,
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'Éditeur de Zones de Cartes',
                      style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: Colors.blue,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Définissez des zones rectangulaires sur vos images de cartes pour un positionnement précis des joueurs',
                      textAlign: TextAlign.center,
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: Colors.grey,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            
            const SizedBox(height: 24),
            
            // Sélection d'image
            Text(
              '🖼️ Sélectionner une image',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            // Formulaire de sélection
            _buildImageSelector(context),
            
            const SizedBox(height: 32),
            
            // Instructions
            Card(
              color: Colors.amber.withOpacity(0.1),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.info_outline, color: Colors.amber),
                        const SizedBox(width: 8),
                        Text(
                          'Comment utiliser',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 16,
                            color: Colors.amber,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    const Text('1. Sélectionnez une carte à configurer'),
                    const Text('2. Cliquez et glissez pour définir des zones rectangulaires'),
                    const Text('3. Nommez chaque zone (ex: "Port", "Cocotier", "Bateau")'),
                    const Text('4. Ajustez la position du pointeur (point rouge)'),
                    const Text('5. Sauvegardez pour générer le JSON'),
                    const SizedBox(height: 8),
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.amber.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: const Text(
                        '💡 Le JSON généré sera copié dans le presse-papiers et affiché pour intégration dans votre app',
                        style: TextStyle(fontStyle: FontStyle.italic),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildImageSelector(BuildContext context) {
    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Toggle Asset/URL
            Row(
              children: [
                Text('Source:', style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(width: 16),
                SegmentedButton<bool>(
                  segments: const [
                    ButtonSegment<bool>(
                      value: false,
                      label: Text('Asset'),
                      icon: Icon(Icons.folder),
                    ),
                    ButtonSegment<bool>(
                      value: true,
                      label: Text('URL'),
                      icon: Icon(Icons.link),
                    ),
                  ],
                  selected: {_isUrlMode},
                  onSelectionChanged: (Set<bool> selection) {
                    setState(() {
                      _isUrlMode = selection.first;
                      _imagePathController.clear();
                    });
                  },
                ),
              ],
            ),
            
            const SizedBox(height: 16),
            
            // Chemin de l'image ou URL
            TextField(
              controller: _imagePathController,
              decoration: InputDecoration(
                labelText: _isUrlMode ? 'URL de l\'image' : 'Chemin de l\'image',
                hintText: _isUrlMode 
                    ? 'https://example.com/image.png'
                    : 'assets/game/tres/map/ile.png',
                prefixIcon: Icon(_isUrlMode ? Icons.link : Icons.image),
                border: const OutlineInputBorder(),
              ),
            ),
            
            const SizedBox(height: 16),
            
            // ID de la carte
            TextField(
              controller: _mapIdController,
              decoration: const InputDecoration(
                labelText: 'ID de la carte',
                hintText: 'ile',
                prefixIcon: Icon(Icons.tag),
                border: OutlineInputBorder(),
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Nom de la carte
            TextField(
              controller: _mapNameController,
              decoration: const InputDecoration(
                labelText: 'Nom de la carte',
                hintText: 'Île au Trésor',
                prefixIcon: Icon(Icons.title),
                border: OutlineInputBorder(),
              ),
            ),
            
            const SizedBox(height: 20),
            
            // Boutons d'exemples (seulement en mode Asset)
            if (!_isUrlMode) 
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: [
                  _buildExampleButton(
                    '🏝️ Île',
                    'assets/game/tres/map/ile.png',
                    'ile',
                    'Île au Trésor',
                  ),
                  _buildExampleButton(
                    '⚓ Port',
                    'assets/game/tres/map/port.png',
                    'port',
                    'Zone du Port',
                  ),
                  _buildExampleButton(
                    '🥥 Cocotier',
                    'assets/game/tres/map/coco.png',
                    'coco',
                    'Zone du Cocotier',
                  ),
                ],
              ),
            
            // Info pour mode URL
            if (_isUrlMode)
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.blue.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.blue.withOpacity(0.3)),
                ),
                child: Row(
                  children: [
                    Icon(Icons.info_outline, color: Colors.blue, size: 20),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'L\'image sera téléchargée depuis l\'URL fournie',
                        style: TextStyle(
                          color: Colors.blue,
                          fontSize: 14,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            
            const SizedBox(height: 20),
            
            // Bouton d'ouverture de l'éditeur
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: _canOpenEditor() ? _openEditor : null,
                icon: const Icon(Icons.edit),
                label: const Text('Ouvrir l\'éditeur'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.all(16),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildExampleButton(String label, String imagePath, String mapId, String mapName) {
    return OutlinedButton(
      onPressed: () {
        setState(() {
          _imagePathController.text = imagePath;
          _mapIdController.text = mapId;
          _mapNameController.text = mapName;
        });
      },
      child: Text(label),
    );
  }

  bool _canOpenEditor() {
    return _imagePathController.text.trim().isNotEmpty &&
           _mapIdController.text.trim().isNotEmpty &&
           _mapNameController.text.trim().isNotEmpty;
  }

  void _openEditor() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => MapZoneEditor(
          imagePath: _imagePathController.text.trim(),
          mapId: _mapIdController.text.trim(),
          mapName: _mapNameController.text.trim(),
          isUrl: _isUrlMode,
        ),
      ),
    );
  }


}