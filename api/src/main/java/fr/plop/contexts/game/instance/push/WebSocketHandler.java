package fr.plop.contexts.game.instance.push;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    private final Map<GameInstanceContext, WebSocketSession> playerIdsWithSession = new HashMap<>();

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;

    public WebSocketHandler(ConnectAuthGameInstanceUseCase authGameInstanceUseCase) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Nouvelle connexion WebSocket établie: {}", session.getId());
        Optional<GameInstanceContext> optionalKey = keyFromSession(session);
        optionalKey.ifPresent(key -> {
            playerIdsWithSession.put(key, session);
            broadcastMessage(key, WebSocketPushAdapter.MESSAGE_INIT);
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Connexion WebSocket fermée: {}", session.getId());
        keyFromSession(session).ifPresent(playerIdsWithSession::remove);
    }

    private Optional<GameInstanceContext> keyFromSession(WebSocketSession session) {
        final String sessionIdPrefix = "instanceId=";
        final String tokenPrefix = "token=";
        Optional<GameInstance.Id> optSessionId = Optional.empty();
        String tokenStr = null;
        if (session.getUri() != null
                && session.getUri().getQuery() != null) {
            String query = session.getUri().getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith(sessionIdPrefix)) {
                    optSessionId = Optional.of(new GameInstance.Id(param.substring(sessionIdPrefix.length())));
                }
                if (param.startsWith(tokenPrefix)) {
                    tokenStr = param.substring(tokenPrefix.length());
                }
            }
        }
        if (optSessionId.isPresent() && tokenStr != null) {
            GameInstance.Id sessionId = optSessionId.get();
            try {
                GameInstanceContext context = authGameInstanceUseCase.findContext(sessionId, new ConnectToken(tokenStr));
                return Optional.of(context);
            } catch (ConnectException e) {
                logger.error("Erreur lors de la récupération du joueur {}", session.getId(), e);
            }
        }
        return Optional.empty();
    }

    public void broadcastMessage(GameInstanceContext context, String message) {
        WebSocketSession session = playerIdsWithSession.get(context);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                logger.error("Erreur lors de l'envoi du value à la session {}", session.getId(), e);
            }
        }
    }
}