package fr.plop.contexts.connect.usecase;

import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.DeviceUserConnect;

import java.util.Optional;

public class ConnectAuthUserCreateUseCase {

    public interface Port {
        Optional<DeviceUserConnect> findByDeviceId(String deviceId);

        ConnectAuthUser createAuth(DeviceUserConnect connect);

        DeviceUserConnect createDeviceConnect(String deviceId);

        Optional<ConnectAuthUser> lastAuth(DeviceUserConnect.Id id);
    }

    private final Port port;

    public ConnectAuthUserCreateUseCase(Port port) {
        this.port = port;
    }

    public ConnectAuthUser byDeviceId(String deviceId) {
        Optional<DeviceUserConnect> optDevice = port.findByDeviceId(deviceId);
        return optDevice
                .map(this::byKnownDevice)
                .orElseGet(() -> byUnknownDevice(deviceId));
    }
    private ConnectAuthUser byKnownDevice(DeviceUserConnect deviceUserConnect) {
        return port.lastAuth(deviceUserConnect.id())
                .filter(ConnectAuthUser::isValid)
                .orElseGet(() -> port.createAuth(deviceUserConnect));
    }

    private ConnectAuthUser byUnknownDevice(String deviceId) {
        return port.createAuth(port.createDeviceConnect(deviceId));
    }

}
