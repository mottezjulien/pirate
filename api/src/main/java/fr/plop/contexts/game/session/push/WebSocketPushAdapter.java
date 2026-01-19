package fr.plop.contexts.game.session.push;

import org.springframework.stereotype.Component;

@Component
public class WebSocketPushAdapter implements PushPort {

    public static final String MESSAGE_INIT = "SYSTEM:INIT";
    private static final String MESSAGE_MOVE = "SYSTEM:MOVE";
    private static final String MESSAGE_STATUS = "SYSTEM:STATUS";
    private static final String MESSAGE_MESSAGE = "SYSTEM:MESSAGE";
    private static final String MESSAGE_TALK = "SYSTEM:TALK";
    private static final String MESSAGE_IMAGE = "SYSTEM:IMAGE";
    private static final String MESSAGE_CONFIRM = "SYSTEM:CONFIRM";
    private static final String MESSAGE_INVENTORY = "SYSTEM:INVENTORY";
    private final WebSocketHandler webSocketHandler;

    public WebSocketPushAdapter(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void push(PushEvent event) {
        webSocketHandler.broadcastMessage(event.context(), message(event));
    }

    private String message(PushEvent event) {
        return switch (event) {
            case PushEvent.GameMove ignored -> MESSAGE_MOVE;
            case PushEvent.GameStatus ignored -> MESSAGE_STATUS;
            case PushEvent.Message message -> MESSAGE_MESSAGE + ":" + message.message();
            case PushEvent.Talk talk -> MESSAGE_TALK + ":" + talk.talkId().value();
            case PushEvent.Image image -> MESSAGE_IMAGE + ":" + image.imageId().value();
            case PushEvent.Confirm confirm -> MESSAGE_CONFIRM + ":" + confirm.confirmId().value() + ":" + confirm.message();
            case PushEvent.Inventory ignored -> MESSAGE_INVENTORY;
        };
    }
}