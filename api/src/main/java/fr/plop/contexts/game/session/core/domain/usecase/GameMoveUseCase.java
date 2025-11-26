package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.generic.position.Point;
import fr.plop.generic.tools.ListTools;

import java.util.List;
import java.util.stream.Collectors;

public class GameMoveUseCase {

    public record Request(Point position) {

    }

    public interface OutPort {

        BoardConfig boardBySessionId(GameSession.Id gameId) throws GameException;

        void savePosition(GamePlayer.Id playerId, List<BoardSpace.Id> spaceIds) throws GameException;

    }

    private final OutPort outPort;

    private final GameEventBroadCast broadCast;

    private final PushPort pushPort;

    public GameMoveUseCase(OutPort outPort, GameEventBroadCast broadCast, PushPort pushPort) {
        this.outPort = outPort;
        this.broadCast = broadCast;
        this.pushPort = pushPort;
    }

    public void apply(GameSession.Id sessionId, GamePlayer player, Request request) throws GameException {
        GameContext context = new GameContext(sessionId, player.id());
        BoardConfig board = outPort.boardBySessionId(sessionId);

        List<BoardSpace.Id> spaceInIds = player.spaceIds();
        List<BoardSpace> next = board.spacesByPoint(request.position()).toList();
        System.out.println("MOVE: BoardSpace next: " + next.stream().map(boardSpace -> boardSpace.id().value()).collect(Collectors.joining()));
        List<BoardSpace.Id> nextIds = next.stream().map(BoardSpace::id).toList();
        if (!spaceInIds.equals(nextIds)) {
            outPort.savePosition(player.id(), nextIds);

            List<BoardSpace.Id> removed = ListTools.removed(spaceInIds, nextIds);
            removed.forEach(space -> broadCast.fire(new GameEvent.GoOut(space), context));

            List<BoardSpace.Id> added = ListTools.added(spaceInIds, nextIds);
            added.forEach(space -> broadCast.fire(new GameEvent.GoIn(space), context));
            pushPort.push(new PushEvent.GameMove(sessionId, player.id()));
        }

    }

}
