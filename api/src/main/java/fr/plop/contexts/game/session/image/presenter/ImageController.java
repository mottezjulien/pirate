package fr.plop.contexts.game.session.image.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.subs.image.ImageDetailsResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/sessions/{sessionId}/images")
public class ImageController {

    private final ConnectUseCase connectUseCase;
    private final GameConfigCache cache;
    private final GameEventOrchestrator eventOrchestrator;
    private final GameSessionSituationGetPort situationGetPort;

    public ImageController(ConnectUseCase connectUseCase, GameConfigCache cache, GameEventOrchestrator eventOrchestrator, GameSessionSituationGetPort situationGetPort) {
        this.connectUseCase = connectUseCase;
        this.cache = cache;
        this.eventOrchestrator = eventOrchestrator;
        this.situationGetPort = situationGetPort;
    }


    @GetMapping({"/{imageId}", "/{imageId}/"})
    public ResponseDTO getOne(@RequestHeader("Authorization") String rawToken,
                              @PathVariable("sessionId") String sessionIdStr,
                              @PathVariable("imageId") String imageIdStr) {

        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final ImageItem.Id imageId = new ImageItem.Id(imageIdStr);
        try {
            final ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            final GamePlayer.Id playerId = user.playerId().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found"));
            final ImageConfig imageConfig = cache.image(sessionId);
            final ImageItem item = imageConfig.byItemId(imageId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"));
            final GameSessionSituation situation = situationGetPort.get(new GameSessionContext(sessionId, playerId));
            return ResponseDTO.fromModel(item.select(situation));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record ResponseDTO(String id, ImageDetailsResponseDTO image) {
        public static ResponseDTO fromModel(ImageItem model) {
            return new ResponseDTO(model.id().value(), ImageDetailsResponseDTO.fromModel(model.generic()));
        }
    }

    @PostMapping({"/{imageId}/objects/{objectId}", "/{imageId}/objects/{objectId}/"})
    public void clickObject(
            @RequestHeader("Authorization") String rawToken,
            @PathVariable("sessionId") String sessionIdStr,
            @PathVariable("imageId") String imageIdStr,
            @PathVariable("objectId") String objectIdStr) {


        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final ImageItem.Id imageId = new ImageItem.Id(imageIdStr);
        final ImageObject.Id objectId = new ImageObject.Id(objectIdStr);
        try {
            final ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            final GamePlayer.Id playerId = user.playerId().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found"));
            eventOrchestrator.fire(new GameSessionContext(sessionId, playerId), new GameEvent.ImageObjectClick(objectId));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

}
