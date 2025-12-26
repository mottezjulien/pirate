
import '../../../../../generic/config/router.dart';
import '../../../../../generic/components/dialog.dart';
import 'package:flutter/material.dart' hide Dialog;

import 'game_image_repository.dart';

class GameSessionImageDialog {

  final GameImageRepository repository = GameImageRepository();
  final Dialog dialog = Dialog();
  final ValueNotifier<ImageDetails?> notifier = ValueNotifier(null);

  void start({required String imageId}) {
    final BuildContext? context = AppRouter.navigatorKey.currentContext;
    if (context != null) {
      notifier.value = null;
      final AlertDialog alertDialog = AlertDialog(
        content: ValueListenableBuilder<ImageDetails?>(
            valueListenable: notifier,
            builder: (BuildContext context, ImageDetails? image, Widget? child) {
              return build(context, image);
            }
        ),
      );

      dialog.showWidget(dialog: alertDialog, paramContext: context);
      repository.findById(imageId).then((talk) => notifier.value = talk);
    }
  }

  Widget build(BuildContext context, ImageDetails? image) {
    if (image == null) {
      return SizedBox(
        height: 80,
        width: 80,
        child: Center(child: CircularProgressIndicator()),
      );
    }
    return SingleChildScrollView(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SizedBox(width: 16),
              Expanded(
                child: Padding(
                  padding: EdgeInsets.symmetric(vertical: 8),
                  child: Text(
                    image.value,
                    style: TextStyle(fontSize: 16, height: 1.5),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

}
