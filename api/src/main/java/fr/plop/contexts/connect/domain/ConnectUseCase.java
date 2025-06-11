package fr.plop.contexts.connect.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSession;

import java.util.Optional;

public class ConnectUseCase {

    public interface OutPort {
        Optional<ConnectAuth> findBySessionIdAndToken(GameSession.Id sessionId, ConnectToken token);

        Optional<ConnectAuth> findByToken(ConnectToken token);
    }

    private final OutPort port;

    public ConnectUseCase(OutPort port) {
        this.port = port;
    }

    public ConnectUser findUserIdBySessionIdAndRawToken(GameSession.Id sessionId, ConnectToken token) throws ConnectException {
        Optional<ConnectAuth> opt = port.findBySessionIdAndToken(sessionId, token);
        ConnectAuth auth = opt.orElseThrow(() -> new ConnectException(ConnectException.Type.EMPTY));
        if (auth.isExpiry()) {
            throw new ConnectException(ConnectException.Type.EXPIRED_TOKEN);
        }
        DeviceConnect connect = auth.connect();
        return connect.user();
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
