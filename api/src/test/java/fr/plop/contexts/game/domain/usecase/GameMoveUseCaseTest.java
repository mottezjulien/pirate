package fr.plop.contexts.game.domain.usecase;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.board.domain.model.BoardSpace;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.event.domain.GameEvent;
import fr.plop.contexts.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.domain.GameException;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    private final Game.Id gameId = new Game.Id("ABC");
    private final ConnectUser.Id userId = new ConnectUser.Id("1234");

    @Test
    public void whenPlayerNotFound() {
        when(outPort.findByGameIdAndUserId(gameId, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.apply(gameId, userId, inSpaceAPosition()))
                .isInstanceOf(GameException.class)
                .hasFieldOrPropertyWithValue("type", GameException.Type.GAME_NOT_FOUND);
    }

    @Test
    public void whenBoardNotFound() {
        expectFoundGamePlayer();
        when(outPort.findByGameId(gameId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.apply(gameId, userId, inSpaceAPosition()))
                .isInstanceOf(GameException.class)
                .hasFieldOrPropertyWithValue("type", GameException.Type.GAME_NOT_FOUND);

    }

    @Test
    public void noSpaces() throws GameException {
        GamePlayer player = expectFoundGamePlayer();

        Board board = new Board();
        when(outPort.findByGameId(gameId)).thenReturn(Optional.of(board));

        useCase.apply(gameId, userId, inSpaceAPosition());

        verify(browCast, never()).fire(any());

        assertThat(board.spacesByPlayerId(player.id())).isEmpty();
        verify(outPort).savePosition(gameId, board, player.id());
    }

    @Test
    public void whenOutPositionThenNothing() throws GameException {
        GamePlayer player = expectFoundGamePlayer();

        Board board = new Board(List.of(spaceA()));
        when(outPort.findByGameId(gameId)).thenReturn(Optional.of(board));

        useCase.apply(gameId, userId, outPosition());

        verify(browCast, never()).fire(any());

        assertThat(board.spacesByPlayerId(player.id())).isEmpty();
        verify(outPort).savePosition(gameId, board, player.id());
    }

    @Test
    public void onGoInSpaceOnlyOne() throws GameException {
        GamePlayer player = expectFoundGamePlayer();

        BoardSpace spaceA = spaceA();
        Board board = new Board(List.of(spaceA));
        when(outPort.findByGameId(gameId)).thenReturn(Optional.of(board));

        useCase.apply(gameId, userId, inSpaceAPosition());

        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceA));

        assertThat(board.spacesByPlayerId(player.id())).containsExactly(spaceA);
        verify(outPort).savePosition(gameId, board, player.id());
    }


    @Test
    public void onGoOutSpaceOnlyOne() throws GameException {
        GamePlayer player = expectFoundGamePlayer();

        BoardSpace spaceA = spaceA();
        Board board = new Board(List.of(spaceA));
        board.putPositions(player.id(), List.of(spaceA));

        when(outPort.findByGameId(gameId)).thenReturn(Optional.of(board));

        useCase.apply(gameId, userId, outPosition());

        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceA));

        assertThat(board.spacesByPlayerId(player.id())).isEmpty();
        verify(outPort).savePosition(gameId, board, player.id());
    }

    @Test
    public void onGoInAndOutSpaceMultiple() throws GameException {
        GamePlayer player = expectFoundGamePlayer();

        BoardSpace spaceA = spaceA();
        BoardSpace spaceB = spaceB();
        BoardSpace spaceC = spaceC();
        BoardSpace spaceD = spaceD();
        BoardSpace spaceE = spaceE();
        Board board = new Board(List.of(spaceA, spaceB, spaceC, spaceD, spaceE));
        board.putPositions(player.id(), List.of(spaceA, spaceB, spaceC));

        when(outPort.findByGameId(gameId)).thenReturn(Optional.of(board));

        useCase.apply(gameId, userId, inCDEPosition());

        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceA));
        verify(browCast).fire(new GameEvent.GoOut(gameId, player.id(), spaceB));
        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceD));
        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), spaceE));

        assertThat(board.spacesByPlayerId(player.id())).containsExactly(spaceC, spaceD, spaceE);
        verify(outPort).savePosition(gameId, board, player.id());
    }

    private GamePlayer expectFoundGamePlayer() {
        GamePlayer player = mock(GamePlayer.class);
        when(player.id()).thenReturn(new GamePlayer.Id("any"));
        when(outPort.findByGameIdAndUserId(gameId, userId)).thenReturn(Optional.of(player));
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