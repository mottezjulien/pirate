package fr.plop.contexts.connect.domain;


import fr.plop.contexts.game.session.core.domain.model.GameSession;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectUseCaseTest {

    @Test
    public void returnEmptyIfNotFoundToken() {
        ConnectToken token = new ConnectToken("token");
        GameSession.Id sessionId = new GameSession.Id("sessionId");

        ConnectUseCase.OutPort port = mock(ConnectUseCase.OutPort.class);
        ConnectUseCase useCase = new ConnectUseCase(port);

        when(port.findBySessionIdAndToken(sessionId, token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.findUserIdBySessionIdAndRawToken(sessionId, token))
                .isInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("type", ConnectException.Type.EMPTY);
    }

    @Test
    public void returnExpiredIfNotValidToken() {
        ConnectToken token = new ConnectToken("token");
        GameSession.Id sessionId = new GameSession.Id("sessionId");

        ConnectUseCase.OutPort port = mock(ConnectUseCase.OutPort.class);
        ConnectUseCase useCase = new ConnectUseCase(port);

        DeviceConnect connect = mock(DeviceConnect.class);
        when(port.findBySessionIdAndToken(sessionId, token)).thenReturn(Optional.of(new ConnectAuth(token, connect, Instant.now().minus(3, ChronoUnit.DAYS))));

        assertThatThrownBy(() -> useCase.findUserIdBySessionIdAndRawToken(sessionId, token))
                .isInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("type", ConnectException.Type.EXPIRED_TOKEN);
    }

    @Test
    public void returnUserFromTokenIfValidToken() throws ConnectException {
        ConnectToken token = new ConnectToken("token");
        GameSession.Id sessionId = new GameSession.Id("sessionId");

        ConnectUseCase.OutPort port = mock(ConnectUseCase.OutPort.class);
        ConnectUseCase useCase = new ConnectUseCase(port);

        DeviceConnect connect = mock(DeviceConnect.class);
        when(connect.user()).thenReturn(new ConnectUser(new ConnectUser.Id("myId")));
        ConnectAuth auth = new ConnectAuth(token, connect, Instant.now().minus(3, ChronoUnit.MINUTES));
        when(port.findBySessionIdAndToken(sessionId, token)).thenReturn(Optional.of(auth));

        ConnectUser user = useCase.findUserIdBySessionIdAndRawToken(sessionId, token);

        assertThat(user.id().value()).isEqualTo("myId");
    }

}