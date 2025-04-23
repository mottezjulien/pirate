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
        assertThatThrownBy(() -> useCase.apply(gameId, userId, anyPosition()))
                .isInstanceOf(GameException.class)
                .hasFieldOrPropertyWithValue("type", GameException.Type.GAME_NOT_FOUND);
    }

    @Test
    public void whenBoardNotFound() {
        expectFoundGamePlayer();

        when(outPort.findByGameId(gameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.apply(gameId, userId, anyPosition()))
                .isInstanceOf(GameException.class)
                .hasFieldOrPropertyWithValue("type", GameException.Type.GAME_NOT_FOUND);

    }

    @Test
    public void noSpaces() throws GameException {
        GamePlayer player = expectFoundGamePlayer();

        Board board = new Board();
        when(outPort.findByGameId(gameId)).thenReturn(Optional.of(board));

        useCase.apply(gameId, userId, anyPosition());

        verify(browCast, never()).fire(any());

        assertThat(board.spacesByPlayerId(player.id())).isEmpty();
        verify(outPort).saveBoard(board, player.id());
    }

    @Test
    public void oneAnySpace() throws GameException {
        GamePlayer player = expectFoundGamePlayer();

        BoardSpace.Rect rect = new BoardSpace.Rect(new BoardSpace.Point(1, 1), new BoardSpace.Point(2, 2));
        BoardSpace space = new BoardSpace(List.of(rect));
        Board board = new Board(List.of(space));
        when(outPort.findByGameId(gameId)).thenReturn(Optional.of(board));

        useCase.apply(gameId, userId, anyPosition());

        verify(browCast, never()).fire(any());

        assertThat(board.spacesByPlayerId(player.id())).isEmpty();
        verify(outPort).saveBoard(board, player.id());
    }

    @Test
    public void onNewSpaceOnlyOne() throws GameException {
        GamePlayer player = expectFoundGamePlayer();

        BoardSpace.Rect rect = new BoardSpace.Rect(new BoardSpace.Point(1, 1), new BoardSpace.Point(10, 10));
        BoardSpace space = new BoardSpace(List.of(rect));
        Board board = new Board(List.of(space));
        when(outPort.findByGameId(gameId)).thenReturn(Optional.of(board));

        useCase.apply(gameId, userId, anyPosition());

        verify(browCast).fire(new GameEvent.GoIn(gameId, player.id(), space));

        assertThat(board.spacesByPlayerId(player.id())).containsExactly(space);
        verify(outPort).saveBoard(board, player.id());
    }

    pouet


    private GamePlayer expectFoundGamePlayer() {
        GamePlayer player = mock(GamePlayer.class);
        when(player.id()).thenReturn(new GamePlayer.Id("5678"));
        when(outPort.findByGameIdAndUserId(gameId, userId)).thenReturn(Optional.of(player));
        return player;
    }


    private static GameMoveUseCase.Request anyPosition() {
        BoardSpace.Point position = new BoardSpace.Point(5,4.6f);
        return new GameMoveUseCase.Request(position);
    }

}