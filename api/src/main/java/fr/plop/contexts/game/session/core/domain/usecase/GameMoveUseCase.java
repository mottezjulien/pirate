package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.generic.position.Point;
import fr.plop.generic.tools.ListTools;

import java.util.List;
import java.util.stream.Collectors;

public class GameMoveUseCase {

    public interface OutPort {

        void savePosition(GamePlayer.Id playerId, List<BoardSpace.Id> spaceIds) throws GameException;

    }

    private final OutPort outPort;

    private final GamePlayerGetPort gamePlayerGetPort;

    private final GameEventBroadCast broadCast;

    private final PushPort pushPort;

    public GameMoveUseCase(OutPort outPort, GamePlayerGetPort gamePlayerGetPort, GameEventBroadCast broadCast, PushPort pushPort) {
        this.outPort = outPort;
        this.gamePlayerGetPort = gamePlayerGetPort;
        this.broadCast = broadCast;
        this.pushPort = pushPort;
    }

    public void apply(GameSessionContext context, BoardConfig board, Point position) throws GameException {

        List<BoardSpace.Id> spaceInIds = gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId());
        List<BoardSpace> next = board.spacesByPoint(position).toList();
        System.out.println("MOVE: BoardSpace next: " + next.stream().map(boardSpace -> boardSpace.id().value()).collect(Collectors.joining()));
        List<BoardSpace.Id> nextIds = next.stream().map(BoardSpace::id).toList();
        if (!spaceInIds.equals(nextIds)) {
            outPort.savePosition(context.playerId(), nextIds);

            List<BoardSpace.Id> removed = ListTools.removed(spaceInIds, nextIds);
            removed.forEach(space -> broadCast.fire(context, new GameEvent.GoOut(space)));

            List<BoardSpace.Id> added = ListTools.added(spaceInIds, nextIds);
            added.forEach(space -> broadCast.fire(context, new GameEvent.GoIn(space)));
            pushPort.push(new PushEvent.GameMove(context));
        }

    }

}
