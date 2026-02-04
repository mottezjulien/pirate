import 'dart:async';
import 'package:flutter/material.dart';
import '../config/router.dart';

enum GameNotificationType {
  info,
  warning,
  error;

  static GameNotificationType fromString(String? type) {
    switch (type?.toLowerCase()) {
      case 'warning':
        return GameNotificationType.warning;
      case 'error':
        return GameNotificationType.error;
      case 'info':
      default:
        return GameNotificationType.info;
    }
  }

  Color get color {
    switch (this) {
      case GameNotificationType.info:
        return Colors.blueAccent;
      case GameNotificationType.warning:
        return Colors.orangeAccent;
      case GameNotificationType.error:
        return Colors.redAccent;
    }
  }

  IconData get icon {
    switch (this) {
      case GameNotificationType.info:
        return Icons.info_outline;
      case GameNotificationType.warning:
        return Icons.warning_amber_rounded;
      case GameNotificationType.error:
        return Icons.error_outline;
    }
  }
}

class GameNotification {
  static void show({
    required String message,
    String? type,
  }) {
    final context = AppRouter.navigatorKey.currentContext;
    if (context == null) return;

    final notificationType = GameNotificationType.fromString(type);
    final overlay = Overlay.of(context);
    
    late OverlayEntry overlayEntry;

    overlayEntry = OverlayEntry(
      builder: (context) => _NotificationWidget(
        message: message,
        type: notificationType,
        onDismiss: () => overlayEntry.remove(),
      ),
    );

    overlay.insert(overlayEntry);
  }
}

class _NotificationWidget extends StatefulWidget {
  final String message;
  final GameNotificationType type;
  final VoidCallback onDismiss;

  const _NotificationWidget({
    required this.message,
    required this.type,
    required this.onDismiss,
  });

  @override
  State<_NotificationWidget> createState() => _NotificationWidgetState();
}

class _NotificationWidgetState extends State<_NotificationWidget> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<Offset> _offsetAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 500),
      vsync: this,
    );

    _offsetAnimation = Tween<Offset>(
      begin: const Offset(0.0, -1.5),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOutBack,
    ));

    _controller.forward();

    // Auto dismiss after 4 seconds
    Timer(const Duration(seconds: 4), () => _hide());
  }

  void _hide() async {
    if (mounted) {
      await _controller.reverse();
      widget.onDismiss();
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Positioned(
      top: MediaQuery.of(context).padding.top + 10,
      left: 10,
      right: 10,
      child: SlideTransition(
        position: _offsetAnimation,
        child: Material(
          color: Colors.transparent,
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: widget.type.color.withOpacity(0.95),
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.2),
                  blurRadius: 8,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Row(
              children: [
                Icon(widget.type.icon, color: Colors.white),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    widget.message,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.white70, size: 20),
                  onPressed: _hide,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
