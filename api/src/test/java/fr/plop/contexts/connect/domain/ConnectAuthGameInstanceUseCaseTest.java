package fr.plop.contexts.connect.domain;


import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectAuthGameInstanceUseCaseTest {

    private final ConnectAuthGameInstanceUseCase.Port port = mock(ConnectAuthGameInstanceUseCase.Port.class);
    private final ConnectAuthGameInstanceUseCase useCase = new ConnectAuthGameInstanceUseCase(port);

    private final ConnectAuthUser.Id authUserId = new ConnectAuthUser.Id();
    private final ConnectToken token = new ConnectToken();
    private final GameInstanceContext context = new GameInstanceContext();

    @Test
    public void returnEmptyIfNotFoundToken() {
        when(port.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.findContext(context.instanceId(), token))
                .isInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("type", ConnectException.Type.NOT_FOUND);
    }

    @Test
    public void returnExpiredIfNotValidToken() {
        when(port.findByToken(token)).thenReturn(Optional.of(new ConnectAuthGameInstance(new ConnectAuthGameInstance.Id(),
                ConnectAuthGameInstance.Status.OPENED, token, authUserId, context, Instant.now().minus(3, ChronoUnit.DAYS))));

        assertThatThrownBy(() -> useCase.findContext(context.instanceId(), token))
                .isInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("type", ConnectException.Type.EXPIRED_TOKEN);
    }


    @Test
    public void returnSessionExceptionIfSessionInvalid() {
        when(port.findByToken(token)).thenReturn(Optional.of(new ConnectAuthGameInstance(new ConnectAuthGameInstance.Id(),
                ConnectAuthGameInstance.Status.OPENED, token, authUserId, context, Instant.now().minus(3, ChronoUnit.MINUTES))));

        assertThatThrownBy(() -> useCase.findContext(new GameInstance.Id(), token))
                .isInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("type", ConnectException.Type.INVALID_SESSION_ID);
    }

    @Test
    public void returnUserFromTokenIfValidToken() throws ConnectException {
        when(port.findByToken(token)).thenReturn(Optional.of(new ConnectAuthGameInstance(new ConnectAuthGameInstance.Id(),
                ConnectAuthGameInstance.Status.OPENED, token, authUserId, context, Instant.now().minus(3, ChronoUnit.MINUTES))));

        GameInstanceContext foundContext = useCase.findContext(context.instanceId(), token);

        assertThat(foundContext).isEqualTo(this.context);
    }

}