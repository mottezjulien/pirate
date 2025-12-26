import 'dart:async';

import 'package:geolocator/geolocator.dart';
import 'package:web_socket_channel/io.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

import '../../../../generic/config/server.dart';
import '../../../../generic/components/dialog.dart';
import '../../../../generic/app_current.dart';
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

  Future<void> init() {
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
    //REST -> WebSockets ???
    GameSessionRepository repository = GameSessionRepository();
    repository.move(Coordinate(lat: position.latitude, lng: position.longitude));
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
    var upperCase = message.toString().toUpperCase();
    if(upperCase.contains('SYSTEM:MOVE')) {
      fireOnMoveListeners();
    }
    if(upperCase.contains('SYSTEM:GOAL')) {
      fireOnGoalListeners();
    }
    if(upperCase.contains('SYSTEM:MESSAGE')) {
      String bodyMessage = message.toString().substring('SYSTEM:MESSAGE:'.length);
      Dialog dialog = Dialog();
      dialog.showMessage(message: bodyMessage);
    }
    if(upperCase.contains('SYSTEM:TALK')) {
      String talkId = message.toString().substring('SYSTEM:TALK:'.length);
      GameSessionTalkDialog talkDialog = GameSessionTalkDialog();
      talkDialog.start(talkId: talkId);
    }
    if(upperCase.contains('SYSTEM:IMAGE')) {
      String imageId = message.toString().substring('SYSTEM:IMAGE:'.length);
      GameSessionImageDialog imageDialog = GameSessionImageDialog();
      imageDialog.start(imageId: imageId);
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
