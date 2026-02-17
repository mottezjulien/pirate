package fr.plop.contexts.game.instance.image.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.contexts.game.instance.situation.domain.port.GameInstanceSituationGetPort;
import fr.plop.subs.image.ImageDetailsResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/instances/{instanceId}/images")
public class ImageController {

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;
    private final GameConfigCache cache;
    private final GameEventOrchestrator eventOrchestrator;
    private final GameInstanceSituationGetPort situationGetPort;

    public ImageController(ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameConfigCache cache, GameEventOrchestrator eventOrchestrator, GameInstanceSituationGetPort situationGetPort) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.cache = cache;
        this.eventOrchestrator = eventOrchestrator;
        this.situationGetPort = situationGetPort;
    }


    @GetMapping({"/{imageId}", "/{imageId}/"})
    public ResponseDTO getOne(@RequestHeader("Authorization") String rawInstanceToken,
                              @PathVariable("instanceId") String instanceIdStr,
                              @PathVariable("imageId") String imageIdStr) {

        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final ImageItem.Id imageId = new ImageItem.Id(imageIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase
                    .findContext(instanceId, new ConnectToken(rawInstanceToken));
            final ImageConfig imageConfig = cache.image(instanceId);
            final ImageItem item = imageConfig.byItemId(imageId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"));
            final GameInstanceSituation situation = situationGetPort.get(context);
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
            @RequestHeader("Authorization") String rawInstanceToken,
            @PathVariable("instanceId") String instanceIdStr,
            @PathVariable("imageId") String imageIdStr,
            @PathVariable("objectId") String objectIdStr) {


        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        //final ImageItem.Id imageId = new ImageItem.Id(imageIdStr);
        final ImageObject.Id objectId = new ImageObject.Id(objectIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase
                    .findContext(instanceId, new ConnectToken(rawInstanceToken));
            eventOrchestrator.fire(context, new GameEvent.ImageObjectClick(objectId));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

}
