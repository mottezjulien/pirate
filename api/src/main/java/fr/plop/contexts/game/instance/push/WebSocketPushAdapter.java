package fr.plop.contexts.game.instance.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketPushAdapter implements PushPort {

    public static final String MESSAGE_INIT = "{\"origin\":\"SYSTEM\",\"status\":\"INIT\"}";

    private final WebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public WebSocketPushAdapter(WebSocketHandler webSocketHandler, ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    public void push(PushEvent event) {
        webSocketHandler.broadcastMessage(event.context(), message(event));
    }

    private String message(PushEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("origin", "SYSTEM");

        switch (event) {
            case PushEvent.GameMove ignored -> data.put("type", "MOVE");
            case PushEvent.GameStatus ignored -> data.put("type", "GOAL");
            case PushEvent.Message message -> {
                data.put("type", "MESSAGE");
                data.put("message", message.message());
            }
            case PushEvent.Talk talk -> {
                data.put("type", "TALK");
                data.put("talkId", talk.talkId().value());
            }
            case PushEvent.Image image -> {
                data.put("type", "IMAGE");
                data.put("imageId", image.imageId().value());
            }
            case PushEvent.Confirm confirm -> {
                data.put("type", "CONFIRM");
                data.put("confirmId", confirm.token().value());
                data.put("message", confirm.message());
            }
            case PushEvent.Inventory ignored -> data.put("type", "INVENTORY");
        }

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing push event", e);
        }
    }
}