package fr.plop.contexts.game.session.push;

import org.springframework.stereotype.Component;

@Component
public class WebSocketPushAdapter implements PushPort {

    private final WebSocketHandler webSocketHandler;

    public WebSocketPushAdapter(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void push(PushEvent event) {
        webSocketHandler.broadcastMessage(event.sessionId(), event.playerId(), message(event));
    }

    private String message(PushEvent event) {
        return switch (event) {
            case PushEvent.GameMove ignored -> "SYSTEM:MOVE";
            case PushEvent.GameStatus ignored -> "SYSTEM:STATUS";
            case PushEvent.Message message -> "SYSTEM:MESSAGE:" + message.message();
            case PushEvent.Talk talk -> "SYSTEM:TALK:" + talk.talkId().value();
        };
    }
}