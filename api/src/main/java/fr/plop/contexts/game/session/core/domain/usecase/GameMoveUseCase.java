package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.generic.position.Point;
import fr.plop.generic.tools.ListTools;

import java.util.List;

public class GameMoveUseCase {

    public record Request(Point position) {

    }

    public interface OutPort {

        BoardConfig boardBySessionId(GameSession.Id gameId) throws GameException;
        void savePosition(GamePlayer.Id playerId, List<BoardSpace.Id> spaceIds) throws GameException;

    }

    private final OutPort outPort;

    private final GameEventBroadCast broadCast;

    public GameMoveUseCase(OutPort outPort, GameEventBroadCast broadCast) {
        this.outPort = outPort;
        this.broadCast = broadCast;
    }

    public void apply(GameSession.Id sessionId, GamePlayer player, Request request) throws GameException {

        BoardConfig board = outPort.boardBySessionId(sessionId);

        List<BoardSpace.Id> currentIds = player.spaceIds();
        List<BoardSpace> next = board.spacesByPoint(request.position()).toList();
        List<BoardSpace.Id> nextIds = next.stream().map(BoardSpace::id).toList();
        if(!currentIds.equals(nextIds)) {
            outPort.savePosition(player.id(), nextIds);

            List<BoardSpace.Id> removed = ListTools.removed(currentIds, nextIds);
            removed.forEach(space -> broadCast.fire(new GameEvent.GoOut(sessionId, player.id(), space)));

            List<BoardSpace.Id> added = ListTools.added(currentIds, nextIds);
            added.forEach(space -> broadCast.fire(new GameEvent.GoIn(sessionId, player.id(), space)));
        }

    }

}
