package fr.plop.contexts.connect.domain;


import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectAuthGameSessionUseCaseTest {

    private final ConnectAuthGameSessionUseCase.Port port = mock(ConnectAuthGameSessionUseCase.Port.class);
    private final ConnectAuthGameSessionUseCase useCase = new ConnectAuthGameSessionUseCase(port);

    private final ConnectAuthUser.Id authUserId = new ConnectAuthUser.Id();
    private final ConnectToken token = new ConnectToken();
    private final GameSessionContext context = new GameSessionContext();

    @Test
    public void returnEmptyIfNotFoundToken() {
        when(port.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.findContext(context.sessionId(), token))
                .isInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("type", ConnectException.Type.NOT_FOUND);
    }

    @Test
    public void returnExpiredIfNotValidToken() {
        when(port.findByToken(token)).thenReturn(Optional.of(new ConnectAuthGameSession(new ConnectAuthGameSession.Id(),
                ConnectAuthGameSession.Type.OPENED, token, authUserId, context, Instant.now().minus(3, ChronoUnit.DAYS))));

        assertThatThrownBy(() -> useCase.findContext(context.sessionId(), token))
                .isInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("type", ConnectException.Type.EXPIRED_TOKEN);
    }


    @Test
    public void returnSessionExceptionIfSessionInvalid() {
        when(port.findByToken(token)).thenReturn(Optional.of(new ConnectAuthGameSession(new ConnectAuthGameSession.Id(),
                ConnectAuthGameSession.Type.OPENED, token, authUserId, context, Instant.now().minus(3, ChronoUnit.MINUTES))));

        assertThatThrownBy(() -> useCase.findContext(new GameSession.Id(), token))
                .isInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("type", ConnectException.Type.INVALID_SESSION_ID);
    }

    @Test
    public void returnUserFromTokenIfValidToken() throws ConnectException {
        when(port.findByToken(token)).thenReturn(Optional.of(new ConnectAuthGameSession(new ConnectAuthGameSession.Id(),
                ConnectAuthGameSession.Type.OPENED, token, authUserId, context, Instant.now().minus(3, ChronoUnit.MINUTES))));

        GameSessionContext foundContext = useCase.findContext(context.sessionId(), token);

        assertThat(foundContext).isEqualTo(this.context);
    }

}