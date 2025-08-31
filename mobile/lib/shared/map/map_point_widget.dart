import 'package:flutter/material.dart';

/// Widget partagé pour afficher un point sur une carte
class MapPointWidget extends StatelessWidget {
  final String label;
  final VoidCallback? onTap;
  final bool isSelected;
  final Color color;
  final Color selectedColor;
  final double size;
  final IconData icon;
  final bool showShadow;
  
  const MapPointWidget({
    super.key,
    required this.label,
    this.onTap,
    this.isSelected = false,
    this.color = Colors.orange,
    this.selectedColor = Colors.red,
    this.size = 24,
    this.icon = Icons.place,
    this.showShadow = true,
  });
  
  @override
  Widget build(BuildContext context) {
    final effectiveColor = isSelected ? selectedColor : color;
    final halfSize = size / 2;
    
    return Positioned(
      left: -halfSize, // Centré sur la position
      top: -halfSize,
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          width: size,
          height: size,
          decoration: BoxDecoration(
            color: effectiveColor,
            shape: BoxShape.circle,
            border: Border.all(
              color: Colors.white,
              width: 2,
            ),
            boxShadow: showShadow ? [
              BoxShadow(
                color: Colors.black.withOpacity(0.3),
                blurRadius: 4,
                offset: const Offset(0, 2),
              ),
            ] : null,
          ),
          child: Icon(
            icon,
            color: Colors.white,
            size: size * 0.6, // Icône proportionnelle
          ),
        ),
      ),
    );
  }
}

/// Widget partagé pour afficher une zone sur une carte
class MapZoneWidget extends StatelessWidget {
  final String label;
  final VoidCallback? onTap;
  final bool isSelected;
  final Color color;
  final Color selectedColor;
  final bool showLabel;
  
  const MapZoneWidget({
    super.key,
    required this.label,
    this.onTap,
    this.isSelected = false,
    this.color = Colors.blue,
    this.selectedColor = Colors.red,
    this.showLabel = true,
  });
  
  @override
  Widget build(BuildContext context) {
    final effectiveColor = isSelected ? selectedColor : color;
    
    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          border: Border.all(color: effectiveColor, width: 2),
          color: effectiveColor.withOpacity(0.2),
        ),
        child: showLabel ? Center(
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: effectiveColor,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              label,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 12,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ) : null,
      ),
    );
  }
}