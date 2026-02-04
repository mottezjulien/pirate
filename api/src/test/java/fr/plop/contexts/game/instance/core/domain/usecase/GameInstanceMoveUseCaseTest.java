package fr.plop.contexts.game.instance.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameInstanceMoveUseCaseTest {

    private final GameInstanceMoveUseCase.OutPort outPort = mock(GameInstanceMoveUseCase.OutPort.class);
    private final GamePlayerGetPort gamePlayerGetPort = mock(GamePlayerGetPort.class);
    private final GameEventOrchestrator eventOrchestrator = mock(GameEventOrchestrator.class);
    private final PushPort pushPort = mock(PushPort.class);
    private final GameConfigCache cache = mock(GameConfigCache.class);
    private final GameInstanceMoveUseCase useCase = new GameInstanceMoveUseCase(outPort, gamePlayerGetPort, eventOrchestrator, pushPort, cache);
    private final GameInstanceContext context = new GameInstanceContext(new GameInstance.Id(), new GamePlayer.Id());
    private final BoardSpace spaceA = spaceA();
    private final BoardSpace spaceB = spaceB();
    private final BoardSpace spaceC = spaceC();
    private final BoardSpace spaceD = spaceD();
    private final BoardSpace spaceE = spaceE();

    @Test
    public void whenSameSpacesDoNothing() throws GameInstanceException {
        when(cache.board(context.instanceId())).thenReturn(new BoardConfig(List.of(spaceA, spaceB)));

        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceA.id(), spaceB.id()));

        useCase.apply(context, inSpaceABPosition());

        verify(eventOrchestrator, never()).fire(any(), any());
        verify(outPort, never()).savePosition(any(), any());
    }

    @Test
    public void onGoInSpaceOnlyOne() throws GameInstanceException {
        when(cache.board(context.instanceId())).thenReturn(new BoardConfig(List.of(spaceA, spaceB)));

        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of());

        useCase.apply(context, inSpaceAPosition());

        verify(eventOrchestrator).fire(context, new GameEvent.GoIn(spaceA.id()));

        verify(outPort).savePosition(context.playerId(), List.of(spaceA.id()));
    }


    @Test
    public void onGoOutSpaceOnlyOne() throws GameInstanceException {
        when(cache.board(context.instanceId())).thenReturn(new BoardConfig(List.of(spaceA, spaceB)));

        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceA.id()));

        useCase.apply(context, outPosition());

        verify(eventOrchestrator).fire(context, new GameEvent.GoOut(spaceA.id()));

        verify(outPort).savePosition(context.playerId(), List.of());

    }

    @Test
    public void onGoInAndOutSpaceMultiple() throws GameInstanceException {
        when(cache.board(context.instanceId())).thenReturn(new BoardConfig(List.of(spaceA, spaceB, spaceC, spaceD, spaceE)));
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceA.id(), spaceB.id(), spaceC.id()));

        useCase.apply(context, inCDEPosition());

        verify(eventOrchestrator).fire(context, new GameEvent.GoOut(spaceA.id()));
        verify(eventOrchestrator).fire(context, new GameEvent.GoOut(spaceB.id()));
        verify(eventOrchestrator).fire(context, new GameEvent.GoIn(spaceD.id()));
        verify(eventOrchestrator).fire(context, new GameEvent.GoIn(spaceE.id()));

        verify(outPort).savePosition(context.playerId(), List.of(spaceC.id(), spaceD.id(), spaceE.id()));
    }

    private static BoardSpace spaceA() {
        Rectangle rectangle = new Rectangle(Point.from(1, 1), Point.from(10, 10));
        return new BoardSpace(List.of(rectangle));
    }

    private static Point inSpaceAPosition() {
        return Point.from(5, 4.6f);
    }

    private static Point inSpaceABPosition() {
        return Point.from(9, 1.7f);
    }

    private static BoardSpace spaceB() {
        Rectangle rectangle = new Rectangle(Point.from(8, 1), Point.from(12, 2));
        return new BoardSpace(List.of(rectangle));
    }

    private static BoardSpace spaceC() {
        Rectangle rectangle = new Rectangle(Point.from(8, 5), Point.from(14, 20));
        return new BoardSpace(List.of(rectangle));
    }

    private static BoardSpace spaceD() {
        Rectangle rectangle = new Rectangle(Point.from(10, 0), Point.from(14, 8));
        return new BoardSpace(List.of(rectangle));
    }

    private static BoardSpace spaceE() {
        Rectangle rectangle = new Rectangle(Point.from(9, 4), Point.from(16, 8));
        return new BoardSpace(List.of(rectangle));
    }

    private static Point inCDEPosition() {
        return Point.from(12, 6f);
    }

    private static Point outPosition() {
        return Point.from(99, 6.6f);
    }

}