package fr.plop.contexts.connect.domain;

import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import fr.plop.contexts.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ConnectionAuthCreateUseCaseTest {

    private final ConnectAuthUserCreateUseCase.Port outPort = mock(ConnectAuthUserCreateUseCase.Port.class);
    private final ConnectAuthUserCreateUseCase useCase = new ConnectAuthUserCreateUseCase(outPort);


    @BeforeEach
    void setUp() {
        reset(outPort);
    }

    @Test
    void happyPath_getTheLastAuthIfIsValid() {
        String deviceId = "deviceId";
        DeviceUserConnect connect = findByDeviceId(deviceId);
        when(outPort.lastAuth(connect.id())).thenReturn(Optional.of(new ConnectAuthUser(new ConnectAuthUser.Id(), new ConnectToken("tokenA"), connect, Instant.now())));
        when(outPort.createAuth(connect)).thenReturn(new ConnectAuthUser(new ConnectAuthUser.Id(), new ConnectToken("tokenB"), connect, Instant.now()));
        ConnectAuthUser result = useCase.byDeviceId(deviceId);
        assertThat(result.token().value()).isEqualTo("tokenA");
    }

    @Test
    void createNewAuthThIfeLastAuthIsNotValid() {
        String deviceId = "deviceId";
        DeviceUserConnect connect = findByDeviceId(deviceId);
        when(outPort.lastAuth(connect.id())).thenReturn(Optional.of(new ConnectAuthUser(new ConnectAuthUser.Id(), new ConnectToken("tokenA"), connect, Instant.now().minus(3, ChronoUnit.DAYS))));
        when(outPort.createAuth(connect)).thenReturn(new ConnectAuthUser(new ConnectAuthUser.Id(), new ConnectToken("tokenB"), connect, Instant.now()));
        ConnectAuthUser result = useCase.byDeviceId(deviceId);
        assertThat(result.token().value()).isEqualTo("tokenB");
    }

    @Test
    void createNewDeviceAndAuthDeviceNotFound() {
        String deviceId = "deviceId";
        when(outPort.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        DeviceUserConnect connect = new DeviceUserConnect(new DeviceUserConnect.Id("any"), deviceId, new User.Id("userId"));
        when(outPort.createDeviceConnect(deviceId)).thenReturn(connect);

        when(outPort.createAuth(connect)).thenReturn(new ConnectAuthUser(new ConnectAuthUser.Id(), new ConnectToken("tokenC"), connect, Instant.now()));

        ConnectAuthUser result = useCase.byDeviceId(deviceId);
        assertThat(result.token().value()).isEqualTo("tokenC");
    }


    public DeviceUserConnect findByDeviceId(String deviceIdStr) {
        DeviceUserConnect.Id deviceId = mock(DeviceUserConnect.Id.class);
        DeviceUserConnect connect = new DeviceUserConnect(deviceId, deviceIdStr, new User.Id("userId"));
        when(outPort.findByDeviceId(deviceIdStr)).thenReturn(Optional.of(connect));
        return connect;
    }


}