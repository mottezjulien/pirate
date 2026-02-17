import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:geolocator/geolocator.dart';
import 'package:web_socket_channel/io.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

import '../../../../generic/config/server.dart';
import '../../../../generic/components/dialog.dart';
import '../../../../generic/app_current.dart';
import '../../../../generic/components/notification.dart';
import '../../../config/board/board.dart';
import '../../../config/board/board_repository.dart';
import '../../../geo/domain/model/coordinate.dart';
import '../../data/game_repository.dart';
import '../../image/game_image_dialog.dart';
import '../../talk/views/game_session_talk_dialog.dart';
import 'game_session_move_usecase.dart';

class GameSession {

  final String _id;
  final String _playerId;
  final String _state;
  final GameLocation gameLocation = GameLocation();
  final GameEventListener eventListener = GameEventListener();

  GameSession({required String id, required String playerId, required String state}) : _id = id, _playerId = playerId, _state = state;

  String get id => _id;
  String get playerId => _playerId;
  String get state => _state;

  Future<void> init() async {
    final Board board = Board(spaces: await _loadBoardSpaces());
    await Future.wait([
      gameLocation.init(board: board),
      eventListener.init(_id)
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

  Future<List<BoardSpace>> _loadBoardSpaces() async {
    final GameConfigBoardRepository repository = GameConfigBoardRepository();
    return await repository.findBoardSpaces();
  }

}

class GameLocation {

  late Stream<Position> _streamPosition;
  late StreamSubscription<Position> _streamSubscription;

  //final List<BoardSpace> _allBoardSpaces = [];
  Coordinate? _lastCoordinate;
  final List<BoardSpace> _lastSpaces = [];


  Future<void> init({required Board board}) async {
    final Completer<void> readyCompleter = Completer<void>();

    LocationSettings locationSettings;
    if (defaultTargetPlatform == TargetPlatform.android) {
      locationSettings = AndroidSettings(
        accuracy: LocationAccuracy.bestForNavigation,
      );
    } else if (defaultTargetPlatform == TargetPlatform.iOS || defaultTargetPlatform == TargetPlatform.macOS) {
      locationSettings = AppleSettings(
        accuracy: LocationAccuracy.bestForNavigation,
        allowBackgroundLocationUpdates: true,
        pauseLocationUpdatesAutomatically: false,
        showBackgroundLocationIndicator: true,
      );
    } else {
      locationSettings = const LocationSettings(
        accuracy: LocationAccuracy.bestForNavigation,
      );
    }

    _streamPosition = Geolocator.getPositionStream(locationSettings: locationSettings);
    _streamSubscription = _streamPosition.listen((position) {
      if (!readyCompleter.isCompleted) {
        readyCompleter.complete();
      }
      onMove(board: board, current: Coordinate(lat: position.latitude, lng: position.longitude));
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


  void onMove({ required Board board, required Coordinate current }) {
    if(_lastCoordinate == null || _lastCoordinate != current) {
      List<BoardSpace> currentSpaces = _fireMove(board: board, current: current);
      _lastCoordinate = current;
      _lastSpaces.clear();
      _lastSpaces.addAll(currentSpaces);
    }
  }


  List<BoardSpace> _fireMove({required Board board, required Coordinate current}) {
    final GameSessionMoveUseCase useCase = new GameSessionMoveUseCase(board: board);
    final GameSessionMoveResult result = useCase.apply(currentCoordinate: current, lastSpaces: _lastSpaces);
    if(result is GameSessionMoveResultUpdate) {
      GameSessionMoveResultUpdate resultUpdate = result;
      // Send position with spaceIds to the server
      final GameSessionRepository repository = GameSessionRepository();
      repository.move(resultUpdate.current);
      return resultUpdate.current;
    }
    return _lastSpaces;
  }

  Coordinate get coordinate => _lastCoordinate!;

  /*
  Stream<Coordinate> get stream => _streamPosition
      .map((position) => Coordinate(lat: position.latitude, lng: position.longitude));
   */

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
    final String wsUrl = "${Server.wsAPI}/ws/games/instances?token=${AppCurrent.gameSessionToken}&sessionId=$sessionId";
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
      final String origin = (data['origin'] ?? '').toUpperCase();
      final String type = (data['type'] ?? '').toUpperCase();

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

      if((data['vibrant'] ?? 'false').toBoolean()) {
        HapticFeedback.vibrate();
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
