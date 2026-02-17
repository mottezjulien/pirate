package fr.plop.contexts.game.instance.core.presenter;

import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.push.PushEvent;
import fr.plop.contexts.game.instance.push.PushPort;
import org.springframework.web.bind.annotation.*;



//TODO MOVE TO MessageController
@RestController
@RequestMapping("/instances/{instanceId}/players/{currentPlayerId}/message")
public class GameInstanceMessageController {

    private final PushPort pushPort;

    public GameInstanceMessageController(PushPort pushPort) {
        this.pushPort = pushPort;
    }


    //TODO UNIQUEMENT POUR l'ADMIN ?? USELESS ?? ADD CONNECT TOKEN ?
    @PostMapping({"", "/"})
    public void sendMessage(@PathVariable("instanceId") String instanceIdStr,
                            @PathVariable("currentPlayerId") String playerIdStr,
                            @RequestBody SendMessageRequest request) {
        GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        GamePlayer.Id playerId = new GamePlayer.Id(playerIdStr);
        pushPort.push(new PushEvent.Message(new GameInstanceContext(instanceId, playerId), request.value()));
    }

    public record SendMessageRequest(String value) {

    }

}


