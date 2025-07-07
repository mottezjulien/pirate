package fr.plop.contexts.game.session.push;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
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

    public record Key(GameSession.Id sessionId, GamePlayer.Id playerId) {

    }

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    private final Map<Key, WebSocketSession> playerIdsWithSession = new HashMap<>();

    private final ConnectUseCase connectUseCase;

    public WebSocketHandler(ConnectUseCase connectUseCase) {
        this.connectUseCase = connectUseCase;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Nouvelle connexion WebSocket établie: {}", session.getId());
        Optional<Key> optionalKey = keyFromSession(session);
        optionalKey.ifPresent(key -> playerIdsWithSession.put(key, session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Connexion WebSocket fermée: {}", session.getId());
        keyFromSession(session).ifPresent(playerIdsWithSession::remove);
    }

    private Optional<Key> keyFromSession(WebSocketSession session) {
        String sessionIdStr = null;
        String tokenStr = null;
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("sessionId=")) {
                    sessionIdStr = param.substring("sessionId=".length());
                }
                if (param.startsWith("token=")) {
                    tokenStr = param.substring("token=".length());
                }
            }
        }
        if(sessionIdStr != null && tokenStr != null) {
            GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
            try {
                ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(tokenStr));
                if(user.player().isPresent()) {
                    return Optional.of(new Key(sessionId, user.player().get().id()));
                }
            } catch (ConnectException e) {
                logger.error("Erreur lors de la récupération du joueur {}", session.getId(), e);
            }
        }
        return Optional.empty();
    }

    public void broadcastMessage(GameSession.Id sessionId, GamePlayer.Id playerId, String message) {
        WebSocketSession session = playerIdsWithSession.get(new Key(sessionId, playerId));
        if(session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                logger.error("Erreur lors de l'envoi du message à la session {}", session.getId(), e);
            }
        }
    }
}