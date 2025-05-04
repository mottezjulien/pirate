package fr.plop.contexts.connect.domain;

import java.util.Optional;

public class ConnectionCreateAuthUseCase {

    public interface DataOutPort {
        Optional<DeviceConnect> findByDeviceId(String deviceId);
        ConnectAuth createAuth(DeviceConnect connect);
        DeviceConnect createDeviceConnectWithUnknownUser(String deviceId);
        Optional<ConnectAuth> lastAuth(DeviceConnect.Id id);
    }

    private final DataOutPort dataOutPort;

    public ConnectionCreateAuthUseCase(DataOutPort dataOutPort) {
        this.dataOutPort = dataOutPort;
    }

    public ConnectAuth byDeviceId(String deviceId) {
        Optional<DeviceConnect> optDevice = dataOutPort.findByDeviceId(deviceId);
        if(optDevice.isPresent()) {
            DeviceConnect deviceConnect = optDevice.orElseThrow();
            Optional<ConnectAuth> optLastAuth = dataOutPort.lastAuth(deviceConnect.id());
            if(optLastAuth.isPresent()) {
                ConnectAuth lastAuth = optLastAuth.orElseThrow();
                if(lastAuth.isValid()) {
                    return lastAuth;
                }
            }
            return dataOutPort.createAuth(deviceConnect);
        }

        DeviceConnect deviceConnect = dataOutPort.createDeviceConnectWithUnknownUser(deviceId);
        return dataOutPort.createAuth(deviceConnect);

    }
}
