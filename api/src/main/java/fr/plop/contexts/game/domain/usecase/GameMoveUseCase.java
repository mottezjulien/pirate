package fr.plop.contexts.game.domain.usecase;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.board.domain.model.BoardSpace;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.event.domain.GameEvent;
import fr.plop.contexts.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.domain.GameException;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.generic.tools.ListTools;

import java.util.List;
import java.util.Optional;

public class GameMoveUseCase {

    public record Request(BoardSpace.Point position) {

    }

    public interface OutPort {
        Optional<GamePlayer> findByGameIdAndUserId(Game.Id gameId, ConnectUser.Id userId);
        Optional<Board> findByGameId(Game.Id gameId);
        void savePosition(Game.Id gameId, Board board, GamePlayer.Id playerId);
    }

    private final OutPort outPort;

    private final GameEventBroadCast broadCast;

    public GameMoveUseCase(OutPort outPort, GameEventBroadCast broadCast) {
        this.outPort = outPort;
        this.broadCast = broadCast;
    }

    public void apply(Game.Id gameId, ConnectUser.Id userId, Request request) throws GameException {

        GamePlayer player = outPort.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new GameException(GameException.Type.GAME_NOT_FOUND));

        Board board = outPort.findByGameId(gameId)
                .orElseThrow(() -> new GameException(GameException.Type.GAME_NOT_FOUND));

        List<BoardSpace> current = board.spacesByPlayerId(player.id());
        List<BoardSpace> next = board.spacesByPosition(request.position()).toList();
        List<BoardSpace> removed = ListTools.removed(current, next);
        removed.forEach(space -> broadCast.fire(new GameEvent.GoOut(gameId, player.id(), space)));
        List<BoardSpace> added = ListTools.added(current, next);
        added.forEach(space -> broadCast.fire(new GameEvent.GoIn(gameId, player.id(), space)));
        board.putPositions(player.id(), next);

        outPort.savePosition(gameId,board, player.id());
    }

}
