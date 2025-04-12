package fr.plop.contexts.connect.domain;

import java.util.Optional;

public class ConnectUseCase {

    public interface OutPort {
        Optional<ConnectAuth> findByToken(ConnectToken token);

    }

    private final OutPort port;

    public ConnectUseCase(OutPort port) {
        this.port = port;
    }

    public ConnectUser findUserIdByRawToken(ConnectToken token) throws ConnectException {
        Optional<ConnectAuth> opt = port.findByToken(token);
        ConnectAuth auth = opt.orElseThrow(() -> new ConnectException(ConnectException.Type.EMPTY));
        if (auth.isExpiry()) {
            throw new ConnectException(ConnectException.Type.EXPIRED_TOKEN);
        }
        DeviceConnect connect = auth.connect();
        return connect.user();
    }

}
