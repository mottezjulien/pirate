package fr.plop.contexts.game.instance.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.push.PushEvent;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.generic.position.Point;
import fr.plop.generic.tools.ListTools;

import java.util.List;
import java.util.stream.Collectors;

public class GameInstanceMoveUseCase {

    public interface OutPort {

        void savePosition(GamePlayer.Id playerId, List<BoardSpace.Id> spaceIds) throws GameInstanceException;

    }

    private final OutPort outPort;
    private final GamePlayerGetPort gamePlayerGetPort;
    private final GameEventOrchestrator eventOrchestrator;
    private final PushPort pushPort;
    private final GameConfigCache cache;

    public GameInstanceMoveUseCase(OutPort outPort, GamePlayerGetPort gamePlayerGetPort, GameEventOrchestrator eventOrchestrator, PushPort pushPort, GameConfigCache cache) {
        this.outPort = outPort;
        this.gamePlayerGetPort = gamePlayerGetPort;
        this.eventOrchestrator = eventOrchestrator;
        this.pushPort = pushPort;
        this.cache = cache;
    }


    public void apply(GameInstanceContext context, Point position) throws GameInstanceException {
        BoardConfig board = cache.board(context.instanceId());
        System.out.println("DEBUG: Board spaces count: " + board.spaces().size());
        for (BoardSpace space : board.spaces()) {
            System.out.println("DEBUG: Checking space " + space.id().value() + " with " + space.rectangles().size() + " rectangles");
            for (fr.plop.generic.position.Rectangle rect : space.rectangles()) {
                System.out.println("DEBUG: Rect: " + rect.bottomLeft() + " to " + rect.topRight() + " contains " + position + " ? " + rect.in(position));
            }
        }
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
