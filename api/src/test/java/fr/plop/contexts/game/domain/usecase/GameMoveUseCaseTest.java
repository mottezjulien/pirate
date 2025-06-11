package fr.plop.contexts.game.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameMoveUseCaseTest {

    private final GameMoveUseCase.OutPort outPort = mock(GameMoveUseCase.OutPort.class);
    private final GameEventBroadCast browCast = mock(GameEventBroadCast.class);
    private final GameEventBroadCast browCast = mock(GameEventBroadCast.class);
    private final GameMoveUseCase useCase = new GameMoveUseCase(outPort, browCast);

    private final GameSession.Id gameId = new GameSession.Id("ABC");
    //private final ConnectUser.Id userId = new ConnectUser.Id("1234");


    private final BoardSpace spaceA = spaceA();
    private final BoardSpace spaceB = spaceB();
    private final BoardSpace spaceC = spaceC();
    private final BoardSpace spaceD = spaceD();
    private final BoardSpace spaceE = spaceE();



    @Test
    public void whenBoardNotFound() throws GameException {
        GamePlayer player = player(List.of(spaceA.id(), spaceB.id()));
        when(outPort.boardBySessionId(gameId)).thenThrow(new GameException(GameException.Type.SESSION_NOT_FOUND));
        assertThatThrownBy(() -> useCase.apply(gameId, player, inSpaceAPosition()))
                .isInstanceOf(GameException.class)
                .hasFieldOrPropertyWithValue("type", GameException.Type.SESSION_NOT_FOUND);

        verify(browCast, never()).fire(any());
        verify(outPort, never()).savePosition(any(), any());
    }

    @Test
    public void whenSameSpacesDoNothing() throws GameException {
        GamePlayer player = player(List.of(spaceA.id(), spaceB.id()));

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);

        useCase.apply(gameId, player, inSpaceABPosition());

        verify(browCast, never()).fire(any());
        verify(outPort, never()).savePosition(any(), any());
    }

    @Test
    public void onGoInSpaceOnlyOne() throws GameException {
        GamePlayer player = player(List.of());

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);

        useCase.apply(gameId, player, inSpaceAPosition());

        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceA.id()));
        verify(outPort).savePosition(player.id(), List.of(spaceA.id()));
    }


    @Test
    public void onGoOutSpaceOnlyOne() throws GameException {
        GamePlayer player = player(List.of(spaceA.id()));

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);

        useCase.apply(gameId, player, outPosition());

        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceA.id()));
        verify(outPort).savePosition(player.id(), List.of());
    }

    @Test
    public void onGoInAndOutSpaceMultiple() throws GameException {
        GamePlayer player = player(List.of(spaceA.id(), spaceB.id(), spaceC.id()));

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB, spaceC, spaceD, spaceE));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);

        useCase.apply(gameId, player, inCDEPosition());

        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceA.id()));
        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceB.id()));
        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceD.id()));
        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceE.id()));

        verify(outPort).savePosition(player.id(), List.of(spaceC.id(), spaceD.id(), spaceE.id()));
    }

    private GamePlayer player(List<BoardSpace.Id> positions) {
        GamePlayer player = mock(GamePlayer.class);
        when(player.id()).thenReturn(new GamePlayer.Id("any"));
        when(player.spaceIds()).thenReturn(positions);
        return player;
    }

    private static BoardSpace spaceA() {
        Rect rect = new Rect(new Point(1, 1), new Point(10, 10));
        return new BoardSpace(List.of(rect));
    }

    private static GameMoveUseCase.Request inSpaceAPosition() {
        Point position = new Point(5,4.6f);
        return new GameMoveUseCase.Request(position);
    }

    private static GameMoveUseCase.Request inSpaceABPosition() {
        Point position = new Point(9,1.7f);
        return new GameMoveUseCase.Request(position);
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

    private static GameMoveUseCase.Request inCDEPosition() {
        Point position = new Point(12,6f);
        return new GameMoveUseCase.Request(position);
    }

    private static GameMoveUseCase.Request outPosition() {
        Point position = new Point(99,6.6f);
        return new GameMoveUseCase.Request(position);
    }

}