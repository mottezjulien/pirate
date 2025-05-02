package fr.plop.contexts.game.domain.usecase;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
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
    private final GameMoveUseCase useCase = new GameMoveUseCase(outPort, browCast);

    private final GameSession.Id gameId = new GameSession.Id("ABC");
    private final ConnectUser.Id userId = new ConnectUser.Id("1234");

    private final BoardSpace spaceA = spaceA();
    private final BoardSpace spaceB = spaceB();
    private final BoardSpace spaceC = spaceC();
    private final BoardSpace spaceD = spaceD();
    private final BoardSpace spaceE = spaceE();

    @Test
    public void whenPlayerNotFound() throws GameException {
        when(outPort.playerByUserId(userId)).thenThrow(new GameException(GameException.Type.PLAYER_NOT_FOUND));

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);
        
        assertThatThrownBy(() -> useCase.apply(gameId, userId, inSpaceAPosition()))
                .isInstanceOf(GameException.class)
                .hasFieldOrPropertyWithValue("type", GameException.Type.PLAYER_NOT_FOUND);

        verify(browCast, never()).fire(any());
        verify(outPort, never()).savePosition(any(), any());
    }

    @Test
    public void whenBoardNotFound() throws GameException {
        player(List.of(spaceA.id(), spaceB.id()));
        when(outPort.boardBySessionId(gameId)).thenThrow(new GameException(GameException.Type.SESSION_NOT_FOUND));
        assertThatThrownBy(() -> useCase.apply(gameId, userId, inSpaceAPosition()))
                .isInstanceOf(GameException.class)
                .hasFieldOrPropertyWithValue("type", GameException.Type.SESSION_NOT_FOUND);

        verify(browCast, never()).fire(any());
        verify(outPort, never()).savePosition(any(), any());
    }

    @Test
    public void whenSameSpacesDoNothing() throws GameException {
        player(List.of(spaceA.id(), spaceB.id()));

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);

        useCase.apply(gameId, userId, inSpaceABPosition());

        verify(browCast, never()).fire(any());
        verify(outPort, never()).savePosition(any(), any());
    }

    @Test
    public void onGoInSpaceOnlyOne() throws GameException {
        GamePlayer player = player(List.of());

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);

        useCase.apply(gameId, userId, inSpaceAPosition());

        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceA.id()));
        verify(outPort).savePosition(player.id(), List.of(spaceA.id()));
    }


    @Test
    public void onGoOutSpaceOnlyOne() throws GameException {
        GamePlayer player = player(List.of(spaceA.id()));

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);

        useCase.apply(gameId, userId, outPosition());

        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceA.id()));
        verify(outPort).savePosition(player.id(), List.of());
    }

    @Test
    public void onGoInAndOutSpaceMultiple() throws GameException {
        GamePlayer player = player(List.of(spaceA.id(), spaceB.id(), spaceC.id()));

        BoardConfig board = new BoardConfig(List.of(spaceA, spaceB, spaceC, spaceD, spaceE));
        when(outPort.boardBySessionId(gameId)).thenReturn(board);

        useCase.apply(gameId, userId, inCDEPosition());

        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceA.id()));
        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceB.id()));
        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceD.id()));
        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceE.id()));

        verify(outPort).savePosition(player.id(), List.of(spaceC.id(), spaceD.id(), spaceE.id()));
    }

    private GamePlayer player(List<BoardSpace.Id> positions) throws GameException {
        GamePlayer player = mock(GamePlayer.class);
        when(player.id()).thenReturn(new GamePlayer.Id("any"));
        when(player.positions()).thenReturn(positions);
        when(outPort.playerByUserId(userId)).thenReturn(player);
        return player;
    }

    private static BoardSpace spaceA() {
        BoardSpace.Rect rect = new BoardSpace.Rect(new BoardSpace.Point(1, 1), new BoardSpace.Point(10, 10));
        return new BoardSpace(List.of(rect));
    }

    private static GameMoveUseCase.Request inSpaceAPosition() {
        BoardSpace.Point position = new BoardSpace.Point(5,4.6f);
        return new GameMoveUseCase.Request(position);
    }

    private static GameMoveUseCase.Request inSpaceABPosition() {
        BoardSpace.Point position = new BoardSpace.Point(9,1.7f);
        return new GameMoveUseCase.Request(position);
    }

    private static BoardSpace spaceB() {
        BoardSpace.Rect rect = new BoardSpace.Rect(new BoardSpace.Point(8, 1), new BoardSpace.Point(12, 2));
        return new BoardSpace(List.of(rect));
    }

    private static BoardSpace spaceC() {
        BoardSpace.Rect rect = new BoardSpace.Rect(new BoardSpace.Point(8, 5), new BoardSpace.Point(14, 20));
        return new BoardSpace(List.of(rect));
    }

    private static BoardSpace spaceD() {
        BoardSpace.Rect rect = new BoardSpace.Rect(new BoardSpace.Point(10, 0), new BoardSpace.Point(14, 8));
        return new BoardSpace(List.of(rect));
    }

    private static BoardSpace spaceE() {
        BoardSpace.Rect rect = new BoardSpace.Rect(new BoardSpace.Point(9, 4), new BoardSpace.Point(16, 8));
        return new BoardSpace(List.of(rect));
    }

    private static GameMoveUseCase.Request inCDEPosition() {
        BoardSpace.Point position = new BoardSpace.Point(12,6f);
        return new GameMoveUseCase.Request(position);
    }

    private static GameMoveUseCase.Request outPosition() {
        BoardSpace.Point position = new BoardSpace.Point(99,6.6f);
        return new GameMoveUseCase.Request(position);
    }

}