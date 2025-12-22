package fr.plop.contexts.connect.usecase;

import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.user.User;

import java.util.Optional;
import java.util.stream.Stream;

public class ConnectAuthGameSessionUseCase {

    public interface Port {
        Stream<ConnectAuthGameSession> findOpenedByUserId(User.Id id);
        ConnectAuthGameSession create(ConnectAuthUser.Id authUserId, GameSessionContext context);
        void close(ConnectAuthGameSession.Id authSessionId);
        Optional<ConnectAuthGameSession> findByToken(ConnectToken connectToken);
    }

    private final Port port;

    public ConnectAuthGameSessionUseCase(Port port) {
        this.port = port;
    }

    public ConnectAuthGameSession create(ConnectAuthUser connectAuthUser, GameSessionContext context) throws ConnectException {
         if(port.findOpenedByUserId(connectAuthUser.userId())
                 .anyMatch(ConnectAuthGameSession::isValid)) {
             throw new ConnectException(ConnectException.Type.ALREADY_CONNECTED);
         }
         return port.create(connectAuthUser.id(), context);
    }

    public void close(ConnectAuthGameSession.Id authSessionId) throws ConnectException {
        port.close(authSessionId);
    }

    public GameSessionContext findContext(GameSession.Id sessionId, ConnectToken connectToken) throws ConnectException {
        return findSessionAuth(sessionId, connectToken).context();
    }

    public ConnectAuthGameSession findSessionAuth(GameSession.Id sessionId, ConnectToken connectToken) throws ConnectException {
        ConnectAuthGameSession auth = port.findByToken(connectToken)
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
