package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameMoveUseCaseTest {

    private final GameMoveUseCase.OutPort outPort = mock(GameMoveUseCase.OutPort.class);
    private final GamePlayerGetPort gamePlayerGetPort = mock(GamePlayerGetPort.class);
    private final GameEventBroadCast browCast = mock(GameEventBroadCast.class);
    private final PushPort pushPort = mock(PushPort.class);

    private final GameMoveUseCase useCase = new GameMoveUseCase(outPort, gamePlayerGetPort, browCast, pushPort);
    private final GameSessionContext context = new GameSessionContext(new GameSession.Id(), new GamePlayer.Id());
    private final BoardSpace spaceA = spaceA();
    private final BoardSpace spaceB = spaceB();
    private final BoardSpace spaceC = spaceC();
    private final BoardSpace spaceD = spaceD();
    private final BoardSpace spaceE = spaceE();

    @Test
    public void whenSameSpacesDoNothing() throws GameException {
        final BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceA.id(), spaceB.id()));

        useCase.apply(context, board, inSpaceABPosition());

        verify(browCast, never()).fire(any(), any());
        verify(outPort, never()).savePosition(any(), any());
    }

    @Test
    public void onGoInSpaceOnlyOne() throws GameException {
        final BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of());

        useCase.apply(context, board, inSpaceAPosition());

        verify(browCast).fire(context, new GameEvent.GoIn(spaceA.id()));

        verify(outPort).savePosition(context.playerId(), List.of(spaceA.id()));
    }


    @Test
    public void onGoOutSpaceOnlyOne() throws GameException {
        final BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceA.id()));

        useCase.apply(context, board, outPosition());

        verify(browCast).fire(context, new GameEvent.GoOut(spaceA.id()));

        verify(outPort).savePosition(context.playerId(), List.of());

    }

    @Test
    public void onGoInAndOutSpaceMultiple() throws GameException {
        final BoardConfig board = new BoardConfig(List.of(spaceA, spaceB, spaceC, spaceD, spaceE));
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceA.id(), spaceB.id(), spaceC.id()));

        useCase.apply(context, board, inCDEPosition());

        verify(browCast).fire(context, new GameEvent.GoOut(spaceA.id()));
        verify(browCast).fire(context, new GameEvent.GoOut(spaceB.id()));
        verify(browCast).fire(context, new GameEvent.GoIn(spaceD.id()));
        verify(browCast).fire(context, new GameEvent.GoIn(spaceE.id()));

        verify(outPort).savePosition(context.playerId(), List.of(spaceC.id(), spaceD.id(), spaceE.id()));
    }

    private static BoardSpace spaceA() {
        Rect rect = new Rect(new Point(1, 1), new Point(10, 10));
        return new BoardSpace(List.of(rect));
    }

    private static Point inSpaceAPosition() {
        return new Point(5, 4.6f);
    }

    private static Point inSpaceABPosition() {
        return new Point(9, 1.7f);
    }

    private static BoardSpace spaceB() {
        Rect rect = new Rect(new Point(8, 1), new Point(12, 2));
        return new BoardSpace(List.of(rect));
    }

    private static BoardSpace spaceC() {
        Rect rect = new Rect(new Point(8, 5), new Point(14, 20));
        return new BoardSpace(List.of(rect));
    }

    private static BoardSpace spaceD() {
        Rect rect = new Rect(new Point(10, 0), new Point(14, 8));
        return new BoardSpace(List.of(rect));
    }

    private static BoardSpace spaceE() {
        Rect rect = new Rect(new Point(9, 4), new Point(16, 8));
        return new BoardSpace(List.of(rect));
    }

    private static Point inCDEPosition() {
        return new Point(12, 6f);
    }

    private static Point outPosition() {
        return new Point(99, 6.6f);
    }

}