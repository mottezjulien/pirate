

import 'package:flutter/cupertino.dart';

import 'game_image_repository.dart';

class ImageObjectWidget extends StatelessWidget {

  final String? imageId;
  final ImageObject object;
  final BoxConstraints constraints;

  const ImageObjectWidget({super.key, this.imageId,
    required this.object,
    required this.constraints});

  @override
  Widget build(BuildContext context) {
    void onAction() {
      if(imageId != null) {
        final GameImageRepository imageRepository = new GameImageRepository();
        imageRepository.clickObject(object.id, object.id);
      }
    }
    if (object.type == 'POINT') {
      return Positioned(
        top: object.position.top * constraints.maxHeight,
        left: object.position.left * constraints.maxWidth,
        child: GestureDetector(
          onDoubleTap: onAction,
          child: Container(
            width: 12,
            height: 12,
            decoration: BoxDecoration(
              color: object.point!.color,
              shape: BoxShape.circle,
            ),
          ),
        ),
      );
    }
    if (object.type == 'IMAGE') {
      return Positioned(
        top: object.position.top * constraints.maxHeight,
        left: object.position.left * constraints.maxWidth,
        child: GestureDetector(
          onDoubleTap: onAction,
          child: Image.asset(
            object.image!.value,
            width: 30,
            height: 30,
            fit: BoxFit.contain,
          ),
        ),
      );
    }
    return SizedBox.shrink();
  }
}