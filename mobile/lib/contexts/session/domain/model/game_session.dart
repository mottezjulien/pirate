import 'dart:async';
import 'dart:convert';

import 'package:geolocator/geolocator.dart';
import 'package:web_socket_channel/io.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

import '../../../../generic/config/server.dart';
import '../../../../generic/components/dialog.dart';
import '../../../../generic/app_current.dart';
import '../../../../generic/components/notification.dart';
import '../../../geo/domain/model/coordinate.dart';
import '../../data/game_repository.dart';
import '../../image/game_image_dialog.dart';
import '../../talk/views/game_session_talk_dialog.dart';

class GameSession {

  final String _id;
  final GameLocation gameLocation = GameLocation();
  final GameEventListener eventListener = GameEventListener();

  GameSession({required String id}) : _id = id;

  String get id => _id;

  Future<void> init() async {
    await Future.wait([
      eventListener.init(_id),
      gameLocation.init(),
    ]);
  }

  void stop() {
    eventListener.stop();
    gameLocation.stop();
  }

  Coordinate get coordinate => gameLocation.coordinate;

  void addOnMoveListener(onMoveListener) => eventListener.addOnMoveListener(onMoveListener);

  void removeOnMoveListener(onMoveListener) => eventListener.removeOnMoveListener(onMoveListener);

  void addOnGoalListener(onGoalListener) => eventListener.addOnGoalListener(onGoalListener);

  void removeOnGoalListener(onGoalListener) => eventListener.removeOnGoalListener(onGoalListener);

  void dispose() {
    eventListener.dispose();
  }

}

class GameLocation {

  late Stream<Position> _streamPosition;
  late StreamSubscription<Position> _streamSubscription;

  Position? last;
  List<BoardSpace> _boardSpaces = [];

  Future<void> init() async {
    // Load board spaces first
    await _loadBoardSpaces();

    final Completer<void> readyCompleter = Completer<void>();
    const LocationSettings locationSettings = LocationSettings(accuracy: LocationAccuracy.bestForNavigation);
    _streamPosition = Geolocator.getPositionStream(locationSettings: locationSettings);
    _streamSubscription = _streamPosition.listen((position) {
      if (!readyCompleter.isCompleted) {
        readyCompleter.complete();
      }
      onMove(position);
    },
    onError: (e) {
      if (!readyCompleter.isCompleted) {
        readyCompleter.completeError(e);
      }
    });
    return readyCompleter.future;
  }

  Future<void> _loadBoardSpaces() async {
    try {
      GameSessionRepository repository = GameSessionRepository();
      _boardSpaces = await repository.findBoardSpaces();
    } catch (e) {
      // If loading fails, continue with empty board spaces
      _boardSpaces = [];
    }
  }

  void stop() {
    _streamSubscription.cancel();
  }


  void onMove(Position position) {
    if(last == null || last != position) { //TODO IF MOVE IS ENOUGH
      last = position;
      _fireMove(position);
    }
  }

  void _fireMove(Position position) {
    final coordinate = Coordinate(lat: position.latitude, lng: position.longitude);

    // Calculate which board spaces the player is currently in
    final List<String> spaceIds = _boardSpaces
        .where((space) => space.contains(coordinate))
        .map((space) => space.id)
        .toList();

    // Send position with spaceIds to the server
    GameSessionRepository repository = GameSessionRepository();
    repository.move(coordinate, spaceIds);
  }

  Coordinate get coordinate => Coordinate(lat: last!.latitude, lng: last!.longitude);

  Stream<Coordinate> get stream => _streamPosition
      .map((position) => Coordinate(lat: position.latitude, lng: position.longitude));



}

class GameEventListener {

  bool running = false;

  late WebSocketChannel _channel;
  late StreamSubscription<dynamic> _streamSubscription;
  final List<OnMoveListener> onMoveListeners = [];
  final List<OnGoalListener> onGoalListeners = [];

  Future<void> init(String sessionId) {
    running = true;
    return connect(sessionId);
  }

  Future<void> connect(String sessionId) {
    final Completer<void> connectionCompleter = Completer<void>();
    final String wsUrl = "${Server.wsAPI}/ws/games/sessions?token=${AppCurrent.gameSessionToken}&sessionId=$sessionId";
    _channel = IOWebSocketChannel.connect(wsUrl);
    _streamSubscription = _channel.stream.listen((message) {
        if (!connectionCompleter.isCompleted) {
          connectionCompleter.complete();
        }
        _do(message.toString());
      },
      onDone: () => running ? connect(sessionId): () {}, // Reconnexion automatique
      onError: (e) {
        if (!connectionCompleter.isCompleted) {
          connectionCompleter.completeError(e);
        }
      },
    );
    return connectionCompleter.future;
  }

  void _do(String message) {
    try {
      final Map<String, dynamic> data = jsonDecode(message);
      final String origin = (data['origin'] ?? '').toString().toUpperCase();
      final String type = (data['type'] ?? '').toString().toUpperCase();

      if (origin == 'SYSTEM') {
        switch (type) {
          case 'MOVE':
            fireOnMoveListeners();
            break;
          case 'GOAL':
            fireOnGoalListeners();
            break;
          case 'MESSAGE':
            Dialog().showMessage(message: data['message'] ?? '');
            break;
          case 'TALK':
            GameSessionTalkDialog().start(talkId: data['talkId'] ?? '');
            break;
          case 'IMAGE':
            GameSessionImageDialog().start(imageId: data['imageId'] ?? '');
            break;
          case 'NOTIFICATION':
            GameNotification.show(
              message: data['message'] ?? '',
              type: data['notificationType'],
            );
            break;
          case 'CONFIRM':
            _handleConfirm(data['confirmId'] ?? '', data['message'] ?? '');
            break;
        }
      }
    } catch (e) {
      // Ignorer les messages non JSON ou malformés
    }
  }

  Future<void> _handleConfirm(String confirmId, String message) async {
    Dialog dialog = Dialog();
    bool? answer = await dialog.showConfirm(message: message);
    if (answer != null) {
      GameSessionRepository repository = GameSessionRepository();
      await repository.confirmAnswer(confirmId: confirmId, answer: answer);
    }
  }

  void stop() {
    running = false;
    _streamSubscription.cancel();
  }

  void addOnMoveListener(OnMoveListener listener) {
    onMoveListeners.add(listener);
  }

  void removeOnMoveListener(OnMoveListener listener) {
    onMoveListeners.remove(listener);
  }

  void fireOnMoveListeners() {
    for (var listener in onMoveListeners) {
      listener.onMove();
    }
  }

  void addOnGoalListener(OnGoalListener listener) {
    onGoalListeners.add(listener);
  }

  void removeOnGoalListener(OnGoalListener listener) {
    onGoalListeners.remove(listener);
  }

  void fireOnGoalListeners() {
    for (var listener in onGoalListeners) {
      listener.onUpdateGoal();
    }
  }

  void dispose() {
    running = false;
    _channel.sink.close();
    onMoveListeners.clear();
    onGoalListeners.clear();
  }

}

abstract class OnMoveListener {
  void onMove();
}

abstract class OnGoalListener {
  void onUpdateGoal();
}
