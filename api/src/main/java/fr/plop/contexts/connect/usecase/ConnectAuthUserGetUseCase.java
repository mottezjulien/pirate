package fr.plop.contexts.connect.usecase;

import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;

import java.util.Optional;

public class ConnectAuthUserGetUseCase {
    public interface Port {
        Optional<ConnectAuthUser> findByToken(ConnectToken token);
    }

    private final Port port;
    public ConnectAuthUserGetUseCase(Port port) {
        this.port = port;
    }

    public ConnectAuthUser findByConnectToken(ConnectToken connectToken) throws ConnectException {
        ConnectAuthUser auth = port.findByToken(connectToken)
                .orElseThrow(() -> new ConnectException(ConnectException.Type.NOT_FOUND));
        if (auth.isExpiry()) {
            throw new ConnectException(ConnectException.Type.EXPIRED_TOKEN);
        }
        return auth;
    }

}
