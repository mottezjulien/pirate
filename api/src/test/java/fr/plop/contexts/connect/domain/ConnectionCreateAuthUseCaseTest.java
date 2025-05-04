package fr.plop.contexts.connect.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ConnectionCreateAuthUseCaseTest {

    private ConnectionCreateAuthUseCase.DataOutPort outPort = mock(ConnectionCreateAuthUseCase.DataOutPort.class);
    private ConnectionCreateAuthUseCase useCase = new ConnectionCreateAuthUseCase(outPort);


    @BeforeEach
    void setUp() {
        reset(outPort);
    }

    @Test
    void happyPath_getTheLastAuthIfIsValid() {
        String deviceId = "deviceId";
        DeviceConnect connect = findByDeviceId(deviceId);
        when(outPort.lastAuth(connect.id())).thenReturn(Optional.of(new ConnectAuth(new ConnectToken("tokenA"), connect, Instant.now())));
        when(outPort.createAuth(connect)).thenReturn(new ConnectAuth(new ConnectToken("tokenB"), connect, Instant.now()));
        ConnectAuth result = useCase.byDeviceId(deviceId);
        assertThat(result.token().value()).isEqualTo("tokenA");
    }

    @Test
    void createNewAuthThIfeLastAuthIsNotValid() {
        String deviceId = "deviceId";
        DeviceConnect connect = findByDeviceId(deviceId);
        when(outPort.lastAuth(connect.id())).thenReturn(Optional.of(new ConnectAuth(new ConnectToken("tokenA"), connect, Instant.now().minus(3, ChronoUnit.DAYS))));
        when(outPort.createAuth(connect)).thenReturn(new ConnectAuth(new ConnectToken("tokenB"), connect, Instant.now()));
        ConnectAuth result = useCase.byDeviceId(deviceId);
        assertThat(result.token().value()).isEqualTo("tokenB");
    }

    @Test
    void createNewDeviceAndAuthDeviceNotFound() {
        String deviceId = "deviceId";
        when(outPort.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        DeviceConnect connect = new DeviceConnect(new DeviceConnect.Id("any"), new ConnectUser(new ConnectUser.Id("userId")), deviceId);
        when(outPort.createDeviceConnectWithUnknownUser(deviceId)).thenReturn(connect);

        when(outPort.createAuth(connect)).thenReturn(new ConnectAuth(new ConnectToken("tokenC"), connect, Instant.now()));

        ConnectAuth result = useCase.byDeviceId(deviceId);
        assertThat(result.token().value()).isEqualTo("tokenC");
    }


    public DeviceConnect findByDeviceId(String deviceIdStr) {
        DeviceConnect.Id deviceId = mock(DeviceConnect.Id.class);
        DeviceConnect connect = new DeviceConnect(deviceId, new ConnectUser(new ConnectUser.Id("userId")), deviceIdStr);
        when(outPort.findByDeviceId(deviceIdStr)).thenReturn(Optional.of(connect));
        return connect;
    }






}