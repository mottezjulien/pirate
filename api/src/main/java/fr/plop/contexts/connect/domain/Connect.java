package fr.plop.contexts.connect.domain;

import java.util.Optional;

public sealed interface Connect permits DeviceConnect {

    record Id(String value) {

    }

    Id id();

    Optional<ConnectAuth> lastAuth();


}
