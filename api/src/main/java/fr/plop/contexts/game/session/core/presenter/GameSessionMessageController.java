package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions/{sessionId}/players/{playerId}/message")
public class GameSessionMessageController {

    private final ConnectUseCase connectUseCase;
    private final PushPort pushPort;

    public GameSessionMessageController(ConnectUseCase connectUseCase, PushPort pushPort) {
        this.connectUseCase = connectUseCase;
        this.pushPort = pushPort;
    }

    //TODO UNIQUEMENT POUR l'ADMIN ?? USELESS ?? ADD CONNECT TOKEN ?
    @PostMapping({"", "/"})
    public void sendMessage(@PathVariable("sessionId") String sessionIdStr,
                            @PathVariable("playerId") String playerIdStr,
                            @RequestBody SendMessageRequest request) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        GamePlayer.Id playerId = new GamePlayer.Id(playerIdStr);
        pushPort.push(new PushEvent.Message(sessionId, playerId, request.value()));
    }

    public record SendMessageRequest(String value) {

    }

}


