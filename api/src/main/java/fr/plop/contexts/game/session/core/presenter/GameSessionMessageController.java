package fr.plop.contexts.game.session.core.presenter;

import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions/{sessionId}/players/{currentPlayerId}/message")
public class GameSessionMessageController {

    private final PushPort pushPort;

    public GameSessionMessageController(PushPort pushPort) {
        this.pushPort = pushPort;
    }


    //TODO UNIQUEMENT POUR l'ADMIN ?? USELESS ?? ADD CONNECT TOKEN ?
    @PostMapping({"", "/"})
    public void sendMessage(@PathVariable("sessionId") String sessionIdStr,
                            @PathVariable("currentPlayerId") String playerIdStr,
                            @RequestBody SendMessageRequest request) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        GamePlayer.Id playerId = new GamePlayer.Id(playerIdStr);
        pushPort.push(new PushEvent.Message(new GameSessionContext(sessionId, playerId), request.value()));
    }

    public record SendMessageRequest(String value) {

    }

}


