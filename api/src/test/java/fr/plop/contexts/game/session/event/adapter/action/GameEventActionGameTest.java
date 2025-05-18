package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.session.core.domain.model.GameOver;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.i18n.domain.I18n;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameEventActionGameTest {

    private final GameEventActionGame.OutputPort outputPort = mock(GameEventActionGame.OutputPort.class);
    private final GameEventBroadCast broadCast = mock(GameEventBroadCast.class);
    private final GameEventActionGame event = new GameEventActionGame(outputPort, broadCast);
    private final GameSession.Id sessionId = new GameSession.Id();
    private final GamePlayer.Id playerId = new GamePlayer.Id();
    private final GamePlayer.Id otherPlayerId1 = new GamePlayer.Id();
    private final GamePlayer.Id otherPlayerId2 = new GamePlayer.Id();


    @BeforeEach
    void setUp() {
        when(outputPort.findPlayerIds(sessionId))
                .thenReturn(Stream.of(playerId, otherPlayerId1, otherPlayerId2));
    }

    @Test
    public void allSuccess() {

        I18n.Id i18nId = new I18n.Id();
        GameOver gameOver = new GameOver(GameOver.Type.SUCCESS_ALL_ENDED, i18nId);
        event.over(sessionId, playerId, gameOver);

        verify(outputPort).win(sessionId, playerId, i18nId);
        verify(outputPort).win(sessionId, otherPlayerId1, i18nId);
        verify(outputPort).win(sessionId, otherPlayerId2, i18nId);
        verify(outputPort).ended(sessionId);

        ArgumentCaptor<GameEvent.UpdateStatus> captor = ArgumentCaptor.forClass(GameEvent.UpdateStatus.class);
        verify(broadCast, times(3)).fire(captor.capture());
        List<GameEvent.UpdateStatus> events = captor.getAllValues();
        assertThat(events).hasSize(3)
                .anySatisfy(event -> {
                    assertThat(event.sessionId()).isEqualTo(sessionId);
                    assertThat(event.playerId()).isEqualTo(playerId);
                })
                .anySatisfy(event -> {
                    assertThat(event.sessionId()).isEqualTo(sessionId);
                    assertThat(event.playerId()).isEqualTo(otherPlayerId1);
                })
                .anySatisfy(event -> {
                    assertThat(event.sessionId()).isEqualTo(sessionId);
                    assertThat(event.playerId()).isEqualTo(otherPlayerId2);
                });

    }

    @Test
    public void oneSuccess() {

        I18n.Id i18nId = new I18n.Id();
        GameOver gameOver = new GameOver(GameOver.Type.SUCCESS_ONE_CONTINUE, i18nId);
        event.over(sessionId, playerId, gameOver);

        verify(outputPort).win(sessionId, playerId, i18nId);
        verify(outputPort, never()).ended(sessionId);

        ArgumentCaptor<GameEvent.UpdateStatus> captor = ArgumentCaptor.forClass(GameEvent.UpdateStatus.class);
        verify(broadCast).fire(captor.capture());
        List<GameEvent.UpdateStatus> events = captor.getAllValues();
        assertThat(events).hasSize(1)
                .anySatisfy(event -> {
                    assertThat(event.sessionId()).isEqualTo(sessionId);
                    assertThat(event.playerId()).isEqualTo(playerId);
                });
    }

}