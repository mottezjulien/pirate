package fr.plop.contexts.game.session.confirm;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/sessions/{sessionId}/confirms")
public class ConfirmController {

    private final ConnectAuthGameSessionUseCase authGameSessionUseCase;
    private final GameEventOrchestrator eventOrchestrator;

    public ConfirmController(ConnectAuthGameSessionUseCase authGameSessionUseCase,
                             GameEventOrchestrator eventOrchestrator) {
        this.authGameSessionUseCase = authGameSessionUseCase;
        this.eventOrchestrator = eventOrchestrator;
    }

    @PostMapping({"/{confirmId}/answer", "/{confirmId}/answer/"})
    public void answer(@RequestHeader("Authorization") String rawSessionToken,
                       @PathVariable("sessionId") String sessionIdStr,
                       @PathVariable("confirmId") String confirmIdStr,
                       @RequestBody AnswerRequestDTO request) {
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final Consequence.Id confirmId = new Consequence.Id(confirmIdStr);
        try {
            final GameSessionContext context = authGameSessionUseCase
                    .findContext(sessionId, new ConnectToken(rawSessionToken));
            eventOrchestrator.fire(context, new GameEvent.ConfirmAnswer(confirmId, request.answer()));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record AnswerRequestDTO(boolean answer) {
    }

}
