package fr.plop.contexts.game.session.map.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.subs.image.ImageDetailsResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/sessions/{sessionId}/maps")
public class GameSessionMapController {

    private final ConnectUseCase connectUseCase;
    private final GameConfigCache cache;
    private final GameSessionSituationGetPort situationGetPort;

    public GameSessionMapController(ConnectUseCase connectUseCase, GameConfigCache cache, GameSessionSituationGetPort situationGetPort) {
        this.connectUseCase = connectUseCase;
        this.cache = cache;
        this.situationGetPort = situationGetPort;
    }

    @GetMapping({"", "/"})
    public List<ImageDetailsResponseDTO> maps(@RequestHeader("Authorization") String rawToken,
                                              @PathVariable("sessionId") String sessionIdStr) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));
            GameSessionSituation situation = situationGetPort.get(sessionId, player);
            MapConfig map = cache.map(sessionId);
            return map.select(situation).map(ImageDetailsResponseDTO::fromMapItemModel).toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

}


