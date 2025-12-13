package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestrator;
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
    private final GameEventOrchestrator eventOrchestrator;
    private final PushPort pushPort;
    private final GameConfigCache cache;

    public GameMoveUseCase(OutPort outPort, GamePlayerGetPort gamePlayerGetPort, GameEventOrchestrator eventOrchestrator, PushPort pushPort, GameConfigCache cache) {
        this.outPort = outPort;
        this.gamePlayerGetPort = gamePlayerGetPort;
        this.eventOrchestrator = eventOrchestrator;
        this.pushPort = pushPort;
        this.cache = cache;
    }


    public void apply(GameSessionContext context, Point position) throws GameException {
        BoardConfig board = cache.board(context.sessionId());
        List<BoardSpace.Id> spaceInIds = gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId());
        List<BoardSpace> next = board.spacesByPoint(position).toList();
        System.out.println("MOVE: BoardSpace next: " + next.stream().map(boardSpace -> boardSpace.id().value()).collect(Collectors.joining()));
        List<BoardSpace.Id> nextIds = next.stream().map(BoardSpace::id).toList();
        if (!spaceInIds.equals(nextIds)) {
            outPort.savePosition(context.playerId(), nextIds);

            List<BoardSpace.Id> removed = ListTools.removed(spaceInIds, nextIds);
            removed.forEach(space -> eventOrchestrator.fire(context, new GameEvent.GoOut(space)));

            List<BoardSpace.Id> added = ListTools.added(spaceInIds, nextIds);
            added.forEach(space -> eventOrchestrator.fire(context, new GameEvent.GoIn(space)));
            pushPort.push(new PushEvent.GameMove(context));
        }

    }

}
