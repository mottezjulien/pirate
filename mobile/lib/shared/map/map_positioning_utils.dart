import 'dart:ui';

/// Utilitaires partagés pour le positionnement des éléments sur les cartes
class MapPositioningUtils {
  /// Calcule les dimensions et offsets d'une image dans un container
  static MapImageLayout calculateImageLayout({
    required Size containerSize,
    required Size originalImageSize,
  }) {
    final imageAspectRatio = originalImageSize.width / originalImageSize.height;
    final containerAspectRatio = containerSize.width / containerSize.height;
    
    print('🔍 LAYOUT ÉDITEUR:');
    print('  Container: ${containerSize.width.toStringAsFixed(1)} x ${containerSize.height.toStringAsFixed(1)}');
    print('  Image originale: ${originalImageSize.width} x ${originalImageSize.height}');
    print('  Aspect ratio image: ${imageAspectRatio.toStringAsFixed(3)}');
    print('  Aspect ratio container: ${containerAspectRatio.toStringAsFixed(3)}');
    
    double imageWidth, imageHeight, offsetX, offsetY;
    
    if (imageAspectRatio > containerAspectRatio) {
      // L'image est limitée par la largeur
      imageWidth = containerSize.width;
      imageHeight = containerSize.width / imageAspectRatio;
      offsetX = 0;
      offsetY = (containerSize.height - imageHeight) / 2;
      print('  Mode: Limitée par la largeur');
    } else {
      // L'image est limitée par la hauteur
      imageWidth = containerSize.height * imageAspectRatio;
      imageHeight = containerSize.height;
      offsetX = (containerSize.width - imageWidth) / 2;
      offsetY = 0;
      print('  Mode: Limitée par la hauteur');
    }
    
    print('  Image affichée: ${imageWidth.toStringAsFixed(1)} x ${imageHeight.toStringAsFixed(1)}');
    print('  Offset: (${offsetX.toStringAsFixed(1)}, ${offsetY.toStringAsFixed(1)})');
    
    return MapImageLayout(
      imageSize: Size(imageWidth, imageHeight),
      offset: Offset(offsetX, offsetY),
      containerSize: containerSize,
      originalImageSize: originalImageSize,
    );
  }
  
  /// Convertit les coordonnées du container vers les coordonnées de l'image
  static Offset convertContainerToImageCoordinates({
    required Offset containerPosition,
    required MapImageLayout layout,
  }) {
    print('🔍 CONVERSION DEBUG:');
    print('  Container position: (${containerPosition.dx.toStringAsFixed(1)}, ${containerPosition.dy.toStringAsFixed(1)})');
    print('  Layout offset: (${layout.offset.dx.toStringAsFixed(1)}, ${layout.offset.dy.toStringAsFixed(1)})');
    print('  Layout image size: ${layout.imageSize.width.toStringAsFixed(1)} x ${layout.imageSize.height.toStringAsFixed(1)}');
    
    // Convertir la position du container vers les coordonnées de l'image affichée
    final imageX = containerPosition.dx - layout.offset.dx;
    final imageY = containerPosition.dy - layout.offset.dy;
    
    print('  Image position (avant clamp): (${imageX.toStringAsFixed(1)}, ${imageY.toStringAsFixed(1)})');
    
    // Vérifier que le point est dans l'image et le clamper si nécessaire
    final clampedX = imageX.clamp(0.0, layout.imageSize.width);
    final clampedY = imageY.clamp(0.0, layout.imageSize.height);
    
    print('  Image position (après clamp): (${clampedX.toStringAsFixed(1)}, ${clampedY.toStringAsFixed(1)})');
    
    return Offset(clampedX, clampedY);
  }
  
  /// Convertit les coordonnées absolues en coordonnées relatives (0-1)
  static Offset convertToRelativeCoordinates({
    required Offset absolutePosition,
    required Size imageSize,
  }) {
    return Offset(
      absolutePosition.dx / imageSize.width,
      absolutePosition.dy / imageSize.height,
    );
  }
  
  /// Convertit les coordonnées relatives (0-1) en coordonnées absolues
  static Offset convertToAbsoluteCoordinates({
    required Offset relativePosition,
    required Size imageSize,
    required Offset imageOffset,
  }) {
    return Offset(
      imageOffset.dx + (relativePosition.dx * imageSize.width),
      imageOffset.dy + (relativePosition.dy * imageSize.height),
    );
  }
  
  /// Ajuste la position du tap pour centrer sur le doigt
  /// Cela compense le fait que le doigt cache une partie de l'écran
  static Offset adjustTapPositionForFinger({
    required Offset tapPosition,
    double fingerOffsetX = 0,
    double fingerOffsetY = -20, // Décalage vers le haut pour compenser le doigt
  }) {
    return Offset(
      tapPosition.dx + fingerOffsetX,
      tapPosition.dy + fingerOffsetY,
    );
  }
}

/// Représente la disposition d'une image dans un container
class MapImageLayout {
  final Size imageSize;        // Taille de l'image affichée
  final Offset offset;         // Offset de l'image dans le container
  final Size containerSize;    // Taille du container
  final Size originalImageSize; // Taille originale de l'image
  
  const MapImageLayout({
    required this.imageSize,
    required this.offset,
    required this.containerSize,
    required this.originalImageSize,
  });
  
  /// Facteurs d'échelle
  double get scaleX => imageSize.width / originalImageSize.width;
  double get scaleY => imageSize.height / originalImageSize.height;
  
  /// Ratios d'aspect
  double get imageAspectRatio => originalImageSize.width / originalImageSize.height;
  double get containerAspectRatio => containerSize.width / containerSize.height;
  
  @override
  String toString() {
    return 'MapImageLayout(imageSize: $imageSize, offset: $offset, containerSize: $containerSize, originalImageSize: $originalImageSize)';
  }
}