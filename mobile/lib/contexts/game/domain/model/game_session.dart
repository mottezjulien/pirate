import 'package:geolocator/geolocator.dart';
import 'package:web_socket_channel/io.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

import '../../../../generic/config/server.dart';
import '../../../../generic/connect/connection_current.dart';
import '../../../../generic/dialog.dart';
import '../../../geo/domain/model/coordinate.dart';
import '../../data/game_repository.dart';
import '../../session/talk/views/game_session_talk_dialog.dart';

class GameSession {

  final String _id, _label;
  final GameLocation gameLocation = GameLocation();
  final GameEventListener eventListener = GameEventListener();

  GameSession({required String id, required String label})
      : _id = id, _label = label;

  String get id => _id;

  String get label => _label;

  void init() {
    gameLocation.init();
    eventListener.init(_id);
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
  Position? last;

  void init() {
    const LocationSettings locationSettings = LocationSettings(accuracy: LocationAccuracy.bestForNavigation);
    _streamPosition = Geolocator.getPositionStream(locationSettings: locationSettings);
    _streamPosition.listen((position) {
      onMove(position);
    });
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

  WebSocketChannel? _channel;
  List<OnMoveListener> onMoveListeners = [];
  List<OnGoalListener> onGoalListeners = [];

  void init(String sessionId) {
    running = true;
    connect(sessionId);
  }

  void connect(String sessionId) {
    final String wsUrl = "${Server.wsAPI}/ws/games/sessions?token=${ConnectionCurrent.token}&sessionId=$sessionId";
    _channel = IOWebSocketChannel.connect(wsUrl);
    _channel!.stream.listen((message) {
        _do(message.toString());
      },
      onDone: () => running ? connect(sessionId): () {}, // Reconnexion automatique
      onError: (e) => print('Erreur WebSocket: $e'),
    );
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
    _channel?.sink.close();
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

