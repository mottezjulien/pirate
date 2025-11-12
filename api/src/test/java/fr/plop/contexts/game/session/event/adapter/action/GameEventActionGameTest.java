package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.subs.i18n.domain.I18n;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameEventActionGameTest {

    private final GameOverUseCase.OutputPort outputPort = mock(GameOverUseCase.OutputPort.class);
    private final PushPort pushPort = mock(PushPort.class);
    private final GameOverUseCase event = new GameOverUseCase(outputPort, pushPort);
    private final GameSession.Id sessionId = new GameSession.Id();
    private final GamePlayer.Id playerId = new GamePlayer.Id();
    private final GamePlayer.Id otherPlayerId1 = new GamePlayer.Id();
    private final GamePlayer.Id otherPlayerId2 = new GamePlayer.Id();


    @BeforeEach
    void setUp() {
        when(outputPort.findActivePlayerIds(sessionId))
                .thenReturn(Stream.of(playerId, otherPlayerId1, otherPlayerId2));
    }

    @Test
    public void allSuccess() {

        I18n.Id i18nId = new I18n.Id();
        SessionGameOver gameOver = new SessionGameOver(SessionGameOver.Type.SUCCESS_ALL_ENDED, Optional.of(i18nId));
        event.apply(sessionId, playerId, gameOver);

        verify(outputPort).win(playerId, Optional.of(i18nId));
        verify(outputPort).win(otherPlayerId1, Optional.of(i18nId));
        verify(outputPort).win(otherPlayerId2, Optional.of(i18nId));
        verify(outputPort).ended(sessionId);

        ArgumentCaptor<PushEvent.GameStatus> captor = ArgumentCaptor.forClass(PushEvent.GameStatus.class);
        verify(pushPort, times(3)).push(captor.capture());
        List<PushEvent.GameStatus> pushs = captor.getAllValues();
        assertThat(pushs).hasSize(3)
                .anySatisfy(push -> assertThat(push.playerId()).isEqualTo(playerId))
                .anySatisfy(push -> assertThat(push.playerId()).isEqualTo(otherPlayerId1))
                .anySatisfy(push -> assertThat(push.playerId()).isEqualTo(otherPlayerId2));

    }

    @Test
    public void oneSuccess() {
        I18n.Id i18nId = new I18n.Id();
        SessionGameOver gameOver = new SessionGameOver(SessionGameOver.Type.SUCCESS_ONE_CONTINUE, Optional.of(i18nId));
        event.apply(sessionId, playerId, gameOver);

        verify(outputPort).win(playerId, Optional.of(i18nId));
        verify(outputPort, never()).ended(sessionId);

        ArgumentCaptor<PushEvent.GameStatus> captor = ArgumentCaptor.forClass(PushEvent.GameStatus.class);
        verify(pushPort).push(captor.capture());
        List<PushEvent.GameStatus> events = captor.getAllValues();
        assertThat(events).hasSize(1)
                .anySatisfy(push -> assertThat(push.playerId()).isEqualTo(playerId));
    }


    @Test
    public void oneSuccessEndedGame() {
        when(outputPort.findActivePlayerIds(sessionId))
                .thenReturn(Stream.of(playerId));

        I18n.Id i18nId = new I18n.Id();
        SessionGameOver gameOver = new SessionGameOver(SessionGameOver.Type.SUCCESS_ONE_CONTINUE, Optional.of(i18nId));
        event.apply(sessionId, playerId, gameOver);

        verify(outputPort).win(playerId, Optional.of(i18nId));
        verify(outputPort).ended(sessionId);

        ArgumentCaptor<PushEvent.GameStatus> captor = ArgumentCaptor.forClass(PushEvent.GameStatus.class);
        verify(pushPort).push(captor.capture());
        List<PushEvent.GameStatus> events = captor.getAllValues();
        assertThat(events).hasSize(1)
                .anySatisfy(push -> {
                    assertThat(push.playerId()).isEqualTo(playerId);
                });
    }

}