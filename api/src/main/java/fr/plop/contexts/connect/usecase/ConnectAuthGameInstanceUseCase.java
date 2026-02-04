package fr.plop.contexts.connect.usecase;

import fr.plop.contexts.connect.domain.ConnectAuthGameInstance;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.user.User;

import java.util.Optional;
import java.util.stream.Stream;

public class ConnectAuthGameInstanceUseCase {

    public interface Port {
        Stream<ConnectAuthGameInstance> findOpenedByUserId(User.Id id);
        ConnectAuthGameInstance create(ConnectAuthUser.Id authUserId, GameInstanceContext context);
        Optional<ConnectAuthGameInstance> close(ConnectAuthGameInstance.Id authSessionId);
        Optional<ConnectAuthGameInstance> findByToken(ConnectToken connectToken);
    }

    private final Port port;

    public ConnectAuthGameInstanceUseCase(Port port) {
        this.port = port;
    }

    public ConnectAuthGameInstance create(ConnectAuthUser connectAuthUser, GameInstanceContext context) throws ConnectException {
         if(port.findOpenedByUserId(connectAuthUser.userId())
                 .anyMatch(ConnectAuthGameInstance::isValid)) {
             throw new ConnectException(ConnectException.Type.ALREADY_CONNECTED);
         }
         return port.create(connectAuthUser.id(), context);
    }

    public Optional<ConnectAuthGameInstance> close(ConnectAuthGameInstance.Id authSessionId) throws ConnectException {
        return port.close(authSessionId);
    }

    public GameInstanceContext findContext(GameInstance.Id sessionId, ConnectToken connectToken) throws ConnectException {
        return findSessionAuth(sessionId, connectToken).context();
    }

    public ConnectAuthGameInstance findSessionAuth(GameInstance.Id sessionId, ConnectToken connectToken) throws ConnectException {
        ConnectAuthGameInstance auth = port.findByToken(connectToken)
                .orElseThrow(() -> new ConnectException(ConnectException.Type.NOT_FOUND));
        if(!auth.isSessionId(sessionId)) {
            throw new ConnectException(ConnectException.Type.INVALID_SESSION_ID);
        }
        if(!auth.isValid()) {
            //TODO AUTO - RECONNECT
            throw new ConnectException(ConnectException.Type.EXPIRED_TOKEN);
        }
        return auth;
    }


}
