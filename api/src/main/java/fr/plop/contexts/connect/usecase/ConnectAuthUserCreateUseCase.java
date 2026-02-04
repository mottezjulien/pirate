package fr.plop.contexts.connect.usecase;

import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectUserDevice;

import java.util.Optional;

public class ConnectAuthUserCreateUseCase {

    public interface Port {
        Optional<ConnectUserDevice> findByDeviceId(String deviceId);

        ConnectAuthUser createAuth(ConnectUserDevice connect);

        ConnectUserDevice createDeviceConnect(String deviceId);

        Optional<ConnectAuthUser> lastAuth(ConnectUserDevice.Id id);
    }

    private final Port port;

    public ConnectAuthUserCreateUseCase(Port port) {
        this.port = port;
    }

    public ConnectAuthUser byDeviceId(String deviceId) {
        Optional<ConnectUserDevice> optDevice = port.findByDeviceId(deviceId);
        return optDevice
                .map(this::byKnownDevice)
                .orElseGet(() -> byUnknownDevice(deviceId));
    }
    private ConnectAuthUser byKnownDevice(ConnectUserDevice connectUserDevice) {
        return port.lastAuth(connectUserDevice.id())
                .filter(ConnectAuthUser::isValid)
                .orElseGet(() -> port.createAuth(connectUserDevice));
    }

    private ConnectAuthUser byUnknownDevice(String deviceId) {
        return port.createAuth(port.createDeviceConnect(deviceId));
    }

}
