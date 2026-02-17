package fr.plop.contexts.game.instance.event.adapter.action;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.model.InstanceGameOver;
import fr.plop.contexts.game.instance.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.instance.push.PushEvent;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.time.GameInstanceTimerRemove;
import fr.plop.subs.i18n.domain.I18n;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GameEventActionGameTest {

    private final GameOverUseCase.OutputPort outputPort = mock(GameOverUseCase.OutputPort.class);
    private final PushPort pushPort = mock(PushPort.class);
    private final GameInstanceTimerRemove timerRemove = mock(GameInstanceTimerRemove.class);
    private final GameConfigCache cache = mock(GameConfigCache.class);
    private final GameOverUseCase event = new GameOverUseCase(outputPort, pushPort, timerRemove, cache);
    private final GameInstanceContext context = new GameInstanceContext();
    private final GamePlayer.Id otherPlayerId1 = new GamePlayer.Id();
    private final GamePlayer.Id otherPlayerId2 = new GamePlayer.Id();


    @BeforeEach
    void setUp() {
        when(outputPort.findActivePlayerIds(context.instanceId()))
                .thenReturn(Stream.of(context.playerId(), otherPlayerId1, otherPlayerId2));
    }

    @Test
    public void allSuccess() {

        I18n.Id i18nId = new I18n.Id();
        InstanceGameOver gameOver = new InstanceGameOver(InstanceGameOver.Type.SUCCESS_ALL_ENDED, Optional.of(i18nId));
        event.apply(context, gameOver);

        verify(outputPort).win(context.playerId(), Optional.of(i18nId));
        verify(outputPort).win(otherPlayerId1, Optional.of(i18nId));
        verify(outputPort).win(otherPlayerId2, Optional.of(i18nId));
        verify(outputPort).ended(context.instanceId());

        ArgumentCaptor<PushEvent.GameStatus> captor = ArgumentCaptor.forClass(PushEvent.GameStatus.class);
        verify(pushPort, times(3)).push(captor.capture());
        List<PushEvent.GameStatus> pushs = captor.getAllValues();
        assertThat(pushs).hasSize(3)
                .anySatisfy(push -> assertThat(push.context().playerId()).isEqualTo(context.playerId()))
                .anySatisfy(push -> assertThat(push.context().playerId()).isEqualTo(otherPlayerId1))
                .anySatisfy(push -> assertThat(push.context().playerId()).isEqualTo(otherPlayerId2));

    }

    @Test
    public void oneSuccess() {
        I18n.Id i18nId = new I18n.Id();
        InstanceGameOver gameOver = new InstanceGameOver(InstanceGameOver.Type.SUCCESS_ONE_CONTINUE, Optional.of(i18nId));
        event.apply(context, gameOver);

        verify(outputPort).win(context.playerId(), Optional.of(i18nId));
        verify(outputPort, never()).ended(context.instanceId());

        ArgumentCaptor<PushEvent.GameStatus> captor = ArgumentCaptor.forClass(PushEvent.GameStatus.class);
        verify(pushPort).push(captor.capture());
        List<PushEvent.GameStatus> events = captor.getAllValues();
        assertThat(events).hasSize(1)
                .anySatisfy(push -> assertThat(push.context().playerId()).isEqualTo(context.playerId()));
    }


    @Test
    public void oneSuccessEndedGame() {
        when(outputPort.findActivePlayerIds(context.instanceId()))
                .thenReturn(Stream.of(context.playerId()));

        I18n.Id i18nId = new I18n.Id();
        InstanceGameOver gameOver = new InstanceGameOver(InstanceGameOver.Type.SUCCESS_ONE_CONTINUE, Optional.of(i18nId));
        event.apply(context, gameOver);

        verify(outputPort).win(context.playerId(), Optional.of(i18nId));
        verify(outputPort).ended(context.instanceId());

        ArgumentCaptor<PushEvent.GameStatus> captor = ArgumentCaptor.forClass(PushEvent.GameStatus.class);
        verify(pushPort).push(captor.capture());
        List<PushEvent.GameStatus> events = captor.getAllValues();
        assertThat(events).hasSize(1)
                .anySatisfy(push -> assertThat(push.context().playerId()).isEqualTo(context.playerId()));
    }

}