import 'package:flutter/material.dart';

import '../../game_current.dart';

class Carousel<CarouselItems> extends StatelessWidget {
  final PageController _pageController = PageController();
  final ValueNotifier<int> _currentIndex = ValueNotifier<int>(0);

  final List<CarouselItems> items;
  final Widget? Function(CarouselItems index) itemWidget;

  Carousel({super.key, required this.items, required this.itemWidget});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: PageView.builder(
              controller: _pageController,
              itemCount: items.length,
              physics: const BouncingScrollPhysics(),
              onPageChanged: (index) => _currentIndex.value = index,
              itemBuilder: (context, index) => itemWidget(items[index])),
        ),
        Container(
          padding: EdgeInsets.symmetric(vertical: GameCurrent.style.dimension.large, horizontal: GameCurrent.style.dimension.extraLarge),
          decoration: BoxDecoration(
            color: Colors.grey.shade100,
            border: Border(top: BorderSide(color: Colors.grey.shade300)),
          ),
          child: ValueListenableBuilder<int>(
            valueListenable: _currentIndex,
            builder: (context, currentIndex, _) {
              return Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  CarouselDots(
                    currentIndex: currentIndex,
                    totalItems: items.length),
                  SizedBox(height: GameCurrent.style.dimension.medium),
                  Text('${currentIndex + 1} / ${items.length}',
                      style: Theme.of(context).textTheme.bodySmall
                          ?.copyWith(fontWeight: FontWeight.w600)),
                ],
              );
            }
          )
        )
      ]
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
          duration: GameCurrent.style.duration.default_,
          height: GameCurrent.style.dimension.medium,
          margin: EdgeInsets.symmetric(
              horizontal: GameCurrent.style.dimension.small),
          width: currentIndex == index ? GameCurrent.style.dimension.extraLarge : GameCurrent.style.dimension.medium,
          decoration: BoxDecoration(
            color: currentIndex == index
                ? GameCurrent.style.color.primary
                : GameCurrent.style.color.lightGrey,
            borderRadius: BorderRadius.circular(GameCurrent.style.dimension.extraLarge),
          ),
        ),
      ),
    );
  }
}
