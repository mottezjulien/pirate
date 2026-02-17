package fr.plop.contexts.game.instance.message;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.config.message.MessageToken;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/instances/{instanceId}/messages")
public class MessageController {

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;

    private final GameEventOrchestrator eventOrchestrator;

    public MessageController(ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameEventOrchestrator eventOrchestrator) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.eventOrchestrator = eventOrchestrator;
    }

    @PostMapping({"/{token}/confirm", "/{token}/confirm/"})
    public void confirm(@RequestHeader("Authorization") String rawInstanceToken,
                        @PathVariable("instanceId") String instanceIdStr,
                        @PathVariable("token") String tokenStr,
                        @RequestBody ConfirmRequest request) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final MessageToken token = new MessageToken(tokenStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase
                    .findContext(instanceId, new ConnectToken(rawInstanceToken));
            eventOrchestrator.fire(context, new GameEvent.MessageConfirmAnswer(token, request.answer()));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record ConfirmRequest(boolean answer) {

    }

}
