package fr.plop.contexts.game.instance.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.push.PushEvent;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.generic.tools.ListTools;

import java.util.List;

public class GameInstanceMoveUseCase {

    public interface OutPort {

        void savePosition(GamePlayer.Id playerId, List<BoardSpace.Id> spaceIds) throws GameInstanceException;

    }

    private final OutPort outPort;
    private final GamePlayerGetPort gamePlayerGetPort;
    private final GameEventOrchestrator eventOrchestrator;
    private final PushPort pushPort;

    public GameInstanceMoveUseCase(OutPort outPort, GamePlayerGetPort gamePlayerGetPort, GameEventOrchestrator eventOrchestrator, PushPort pushPort) {
        this.outPort = outPort;
        this.gamePlayerGetPort = gamePlayerGetPort;
        this.eventOrchestrator = eventOrchestrator;
        this.pushPort = pushPort;
    }


    public void apply(GameInstanceContext context, List<BoardSpace.Id> nextSpaceIds) throws GameInstanceException {
        List<BoardSpace.Id> spaceInIds = gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId());
        if (!spaceInIds.equals(nextSpaceIds)) {
            outPort.savePosition(context.playerId(), nextSpaceIds);

            List<BoardSpace.Id> removed = ListTools.removed(spaceInIds, nextSpaceIds);
            removed.forEach(space -> eventOrchestrator.fire(context, new GameEvent.GoOut(space)));

            List<BoardSpace.Id> added = ListTools.added(spaceInIds, nextSpaceIds);
            added.forEach(space -> eventOrchestrator.fire(context, new GameEvent.GoIn(space)));
            pushPort.push(new PushEvent.GameMove(context));
        }

    }

}
