package fr.plop.contexts.connect.domain;

import java.util.Optional;

public record DeviceConnect(Connect.Id id,
                            Optional<ConnectAuth> lastAuth,

                            ConnectUser user,
                            String deviceId) implements Connect {


}
